package de.kitshn.ui.dialog.recipe.creationandedit

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.kitshn.TestTagRepository
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.model.TandoorKeyword
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.formEquals
import de.kitshn.formEqualsInt
import de.kitshn.handlePagerState
import de.kitshn.handleTandoorRequestState
import de.kitshn.model.form.KitshnForm
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.component.model.SectionStepIndicator
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.selectionMode.component.SelectionModeTopAppBar
import de.kitshn.ui.selectionMode.rememberSelectionModeState
import de.kitshn.ui.state.foreverRememberNotSavable
import de.kitshn.ui.state.foreverRememberPagerState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_abort
import kitshn.composeapp.generated.resources.action_combine
import kitshn.composeapp.generated.resources.action_continue
import kitshn.composeapp.generated.resources.action_create
import kitshn.composeapp.generated.resources.action_create_recipe
import kitshn.composeapp.generated.resources.action_edit_recipe
import kitshn.composeapp.generated.resources.action_save
import kitshn.composeapp.generated.resources.action_saving
import kitshn.composeapp.generated.resources.common_details
import kitshn.composeapp.generated.resources.common_keywords
import kitshn.composeapp.generated.resources.common_steps
import kitshn.composeapp.generated.resources.common_unsaved_changes
import kitshn.composeapp.generated.resources.common_unsaved_changes_description
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource

data class RecipeCreationAndEditDefaultValues(
    var rememberKey: String = "",

    val currentImageUrl: String? = null,
    val name: String = "",
    val description: String? = null,

    val servings: Int? = null,
    val servingsText: String? = null,

    val workingTime: Int? = null,
    val waitingTime: Int? = null,

    val sourceUrl: String? = null,

    val keywords: List<TandoorKeyword> = listOf(),
    val stepsOrder: List<Int> = listOf()
)

@Composable
fun rememberRecipeEditDialogState(
    key: String
): RecipeEditDialogState {
    val value by foreverRememberNotSavable(
        key = key,
        initialValue = RecipeEditDialogState()
    )

    value.defaultValues.rememberKey = key

    return value
}

class RecipeEditDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    var defaultValues = RecipeCreationAndEditDefaultValues()
    var recipe by mutableStateOf<TandoorRecipe?>(null)

    fun open(recipe: TandoorRecipe) {
        this.recipe = recipe

        this.defaultValues = RecipeCreationAndEditDefaultValues(
            currentImageUrl = recipe.image,
            name = recipe.name,
            description = recipe.description,
            servings = recipe.servings,
            servingsText = recipe.servings_text,
            workingTime = recipe.working_time,
            waitingTime = recipe.waiting_time,
            sourceUrl = recipe.source_url,
            keywords = recipe.keywords.toMutableList(),
            stepsOrder = recipe.steps.map { it.id }
        )

        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun rememberRecipeCreationDialogState(
    key: String
): RecipeCreationDialogState {
    val value by foreverRememberNotSavable(
        key = key,
        initialValue = RecipeCreationDialogState()
    )

    return value
}

class RecipeCreationDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    var defaultValues = RecipeCreationAndEditDefaultValues()
    var recipe by mutableStateOf<TandoorRecipe?>(null)

    fun open() {
        this.defaultValues = RecipeCreationAndEditDefaultValues()
        this.shown.value = true
    }

    fun open(values: RecipeCreationAndEditDefaultValues) {
        this.defaultValues = values
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

class RecipeCreationAndEditDialogValue(
    defaultValues: RecipeCreationAndEditDefaultValues
) {
    var name by mutableStateOf(defaultValues.name)
    var description by mutableStateOf(defaultValues.description)

    var servings by mutableStateOf(defaultValues.servings)
    var servingsText by mutableStateOf(defaultValues.servingsText)

    var workingTime by mutableStateOf(defaultValues.workingTime)
    var waitingTime by mutableStateOf(defaultValues.waitingTime)

    var sourceUrl by mutableStateOf(defaultValues.sourceUrl)

    val keywords = mutableStateListOf<TandoorKeyword>()

    var imageUploadByteArray by mutableStateOf<ByteArray?>(null)

    var stepsOrder = mutableStateListOf<Int>()

    var _stepsUpdate by mutableLongStateOf(0)
    fun updateSteps() {
        _stepsUpdate = Clock.System.now().toEpochMilliseconds()
    }

    init {
        this.keywords.addAll(defaultValues.keywords)
        this.stepsOrder.addAll(defaultValues.stepsOrder)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecipeCreationAndEditDialog(
    client: TandoorClient,
    creationState: RecipeCreationDialogState? = null,
    editState: RecipeEditDialogState? = null,
    showFractionalValues: Boolean,
    onRefresh: () -> Unit
) {
    if(creationState?.shown?.value != true && editState?.shown?.value != true) return

    val coroutineScope = rememberCoroutineScope()

    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    val defaultValues =
        if(creationState?.shown?.value == true) creationState.defaultValues else editState?.defaultValues
    if(defaultValues == null) return

    val isEditDialog = editState?.shown?.value == true
    val recipe: TandoorRecipe? = if(isEditDialog) {
        editState?.recipe
    } else {
        creationState?.recipe
    }

    val stepsSelectionModeState = rememberSelectionModeState<Int>()

    val rememberKeyBegin =
        "${defaultValues.rememberKey}/${if(isEditDialog) editState?.recipe?.id else "creation"}"

    var rememberKeyId by foreverRememberNotSavable(
        key = "${defaultValues.rememberKey}/rememberKeyId",
        initialValue = Clock.System.now().toEpochMilliseconds()
    )
    val rememberKey = "$rememberKeyBegin/$rememberKeyId"

    var detailsPageForm by remember { mutableStateOf<KitshnForm?>(null) }

    val values by foreverRememberNotSavable(
        key = "$rememberKey/values",
        initialValue = RecipeCreationAndEditDialogValue(defaultValues)
    )
    val pages = listOf<@Composable () -> Unit>(
        { detailsPageForm = detailsPage(recipe = editState?.recipe, values = values) },
        {
            StepsPage(
                selectionState = stepsSelectionModeState,
                recipe = editState?.recipe ?: creationState?.recipe,
                values = values,
                showFractionalValues = showFractionalValues
            )
        },
        { KeywordsPage(client = client, values = values) }
    )

    // create (temporary) recipe for creation dialog
    LaunchedEffect(creationState, creationState?.shown?.value) {
        if(creationState == null) return@LaunchedEffect
        if(creationState.recipe != null && !creationState.recipe!!.destroyed) return@LaunchedEffect

        try {
            creationState.recipe = client.recipe.create()
        } catch(e: TandoorRequestsError) {
            creationState.dismiss()
        }
    }

    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    fun areThereUnsavedChanges(): Boolean {
        // always show in creation dialog
        if(!isEditDialog) return true

        return !(
                defaultValues.name.formEquals(values.name)
                        && defaultValues.description.formEquals(values.description)
                        && defaultValues.servings == values.servings
                        && defaultValues.servingsText.formEquals(values.servingsText)
                        && defaultValues.workingTime == values.workingTime
                        && defaultValues.waitingTime == values.waitingTime
                        && defaultValues.keywords.formEquals(values.keywords)
                        && defaultValues.sourceUrl.formEquals(values.sourceUrl)
                        && defaultValues.stepsOrder.formEqualsInt(values.stepsOrder)
                        && values.imageUploadByteArray == null
                )
    }

    val pagerState = foreverRememberPagerState(key = "${rememberKey}/pagerState") { pages.size }
    hapticFeedback.handlePagerState(pagerState)

    val requestRecipeState = rememberTandoorRequestState()

    fun dismiss() {
        coroutineScope.launch {
            // deleting (temporary) recipe
            if(creationState?.recipe != null) creationState.recipe!!.delete()

            onRefresh()

            creationState?.dismiss()
            editState?.dismiss()

            rememberKeyId = Clock.System.now().toEpochMilliseconds()
        }
    }

    val combineStepsRequestState = rememberTandoorRequestState()

    AdaptiveFullscreenDialog(
        onDismiss = {
            dismiss()
        },
        onPreDismiss = {
            !(areThereUnsavedChanges().also {
                if(it) showUnsavedChangesDialog = true
            })
        },
        topBarWrapper = {
            SelectionModeTopAppBar(
                topAppBar = it,
                actions = {
                    if(stepsSelectionModeState.selectedItems.size >= 2) IconButton(
                        onClick = {
                            coroutineScope.launch {
                                combineStepsRequestState.wrapRequest {
                                    val sortedSteps =
                                        stepsSelectionModeState.selectedItems.sortedBy { id ->
                                            values.stepsOrder.indexOf(id)
                                        }
                                    val steps =
                                        sortedSteps.map { id -> recipe!!.steps.find { step -> step.id == id }!! }

                                    recipe?.combineSteps(steps)
                                    values.updateSteps()

                                    repeat(steps.size) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        delay(25)
                                    }

                                    stepsSelectionModeState.disable()
                                }
                            }
                        }
                    ) {
                        IconWithState(
                            imageVector = Icons.Rounded.Compress,
                            contentDescription = stringResource(Res.string.action_combine),
                            state = combineStepsRequestState.state.toIconWithState()
                        )
                    }
                },
                state = stepsSelectionModeState
            )
        },
        title = {
            Text(
                text = if(isEditDialog) {
                    stringResource(Res.string.action_edit_recipe)
                } else {
                    stringResource(Res.string.action_create_recipe)
                }
            )
        },
        topAppBarActions = {
            FilledIconButton(
                onClick = {
                    coroutineScope.launch {
                        if(detailsPageForm?.checkSubmit() != true) return@launch

                        coroutineScope.launch {
                            requestRecipeState.wrapRequest {
                                if(isEditDialog) {
                                    recipe?.partialUpdate(
                                        name = if(defaultValues.name.formEquals(values.name)) null else values.name,
                                        description = if(defaultValues.description.formEquals(values.description)) null else values.description,
                                        keywords = if(defaultValues.keywords.formEquals(values.keywords)) null else values.keywords,
                                        working_time = if(defaultValues.workingTime == values.workingTime) null else values.workingTime,
                                        waiting_time = if(defaultValues.waitingTime == values.waitingTime) null else values.waitingTime,
                                        source_url = if(defaultValues.sourceUrl.formEquals(values.sourceUrl)) null else values.sourceUrl,
                                        servings = if(defaultValues.servings == values.servings) null else values.servings,
                                        servings_text = if(defaultValues.servingsText.formEquals(
                                                values.servingsText
                                            )
                                        ) null else values.servingsText
                                    )
                                } else {
                                    recipe?.partialUpdate(
                                        name = values.name,
                                        description = values.description ?: "",
                                        keywords = values.keywords,
                                        working_time = values.workingTime,
                                        waiting_time = values.waitingTime,
                                        source_url = values.sourceUrl,
                                        servings = values.servings,
                                        servings_text = values.servingsText
                                    )
                                }

                                if(!defaultValues.stepsOrder.formEqualsInt(values.stepsOrder)) recipe?.steps?.forEach {
                                    val index = values.stepsOrder.indexOf(it.id)
                                    it.partialUpdate(order = index)
                                }

                                if(values.imageUploadByteArray != null) {
                                    recipe?.uploadImage(values.imageUploadByteArray!!)
                                }

                                onRefresh()

                                editState?.dismiss()
                                creationState?.dismiss()
                            }

                            hapticFeedback.handleTandoorRequestState(requestRecipeState)
                        }
                    }
                }
            ) {
                IconWithState(
                    progressIndicatorTint = LocalContentColor.current,
                    imageVector = when(isEditDialog) {
                        true -> Icons.Rounded.Save
                        else -> Icons.Rounded.Add
                    },
                    contentDescription = when(isEditDialog) {
                        true -> stringResource(Res.string.action_save)
                        else -> stringResource(Res.string.action_create)
                    },
                    state = requestRecipeState.state.toIconWithState()
                )
            }
        },
        bottomBar = {
            SectionStepIndicator(
                items = listOf(
                    stringResource(Res.string.common_details),
                    stringResource(Res.string.common_steps),
                    stringResource(Res.string.common_keywords)
                ),
                selected = pagerState.currentPage,
                bottomPadding = if(it) with(density) {
                    WindowInsets.navigationBars.getBottom(density).toDp()
                } else 0.dp
            ) {
                coroutineScope.launch {
                    pagerState.scrollToPage(it)
                }
            }
        }
    ) { nsc, _, _ ->
        HorizontalPager(
            state = pagerState,
            pageNestedScrollConnection = nsc
        ) { index ->
            pages[index]()
        }
    }

    if(showUnsavedChangesDialog) AlertDialog(
        onDismissRequest = { showUnsavedChangesDialog = false },
        icon = { Icon(Icons.Rounded.Warning, stringResource(Res.string.common_unsaved_changes)) },
        title = { Text(text = stringResource(Res.string.common_unsaved_changes)) },
        text = { Text(text = stringResource(Res.string.common_unsaved_changes_description)) },
        dismissButton = {
            FilledTonalButton(
                modifier = Modifier.testTag(TestTagRepository.ACTION_ABORT.name),
                onClick = {
                    showUnsavedChangesDialog = false
                }
            ) {
                Text(text = stringResource(Res.string.action_abort))
            }
        },
        confirmButton = {
            Button(
                modifier = Modifier.testTag(TestTagRepository.ACTION_CONTINUE.name),
                onClick = {
                    showUnsavedChangesDialog = false
                    dismiss()
                }
            ) {
                Text(text = stringResource(Res.string.action_continue))
            }
        }
    )

    if(combineStepsRequestState.state == TandoorRequestStateState.LOADING) AlertDialog(
        onDismissRequest = { },
        confirmButton = { },
        icon = { Icon(Icons.Rounded.Save, stringResource(Res.string.action_save)) },
        title = { Text(text = stringResource(Res.string.action_saving)) },
        text = {
            LinearWavyProgressIndicator()
        }
    )

    TandoorRequestErrorHandler(state = requestRecipeState)
}