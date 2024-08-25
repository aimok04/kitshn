package de.kitshn.android.ui.view.recipe.details

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.areStatusBarsVisible
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Reviews
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.api.tandoor.model.TandoorIngredient
import de.kitshn.android.api.tandoor.model.TandoorKeywordOverview
import de.kitshn.android.api.tandoor.model.TandoorStep
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.android.launchCustomTabs
import de.kitshn.android.ui.component.alert.FullSizeAlertPane
import de.kitshn.android.ui.component.buttons.BackButton
import de.kitshn.android.ui.component.buttons.WideActionChip
import de.kitshn.android.ui.component.buttons.WideActionChipType
import de.kitshn.android.ui.component.icons.FiveStarIconRow
import de.kitshn.android.ui.component.model.ingredient.IngredientsList
import de.kitshn.android.ui.component.model.recipe.RecipeInfoBlob
import de.kitshn.android.ui.component.model.recipe.step.RecipeStepCard
import de.kitshn.android.ui.component.model.servings.ServingsSelector
import de.kitshn.android.ui.dialog.common.CommonDeletionDialog
import de.kitshn.android.ui.dialog.common.rememberCommonDeletionDialogState
import de.kitshn.android.ui.dialog.mealplan.MealPlanCreationAndEditDefaultValues
import de.kitshn.android.ui.dialog.mealplan.MealPlanCreationAndEditDialog
import de.kitshn.android.ui.dialog.mealplan.rememberMealPlanCreationDialogState
import de.kitshn.android.ui.dialog.recipe.RecipeIngredientAllocationDialog
import de.kitshn.android.ui.dialog.recipe.RecipeLinkBottomSheet
import de.kitshn.android.ui.dialog.recipe.creationandedit.RecipeCreationAndEditDialog
import de.kitshn.android.ui.dialog.recipe.creationandedit.rememberRecipeEditDialogState
import de.kitshn.android.ui.dialog.recipe.rememberRecipeIngredientAllocationDialogState
import de.kitshn.android.ui.dialog.recipe.rememberRecipeLinkBottomSheetState
import de.kitshn.android.ui.dialog.select.SelectRecipeBookDialog
import de.kitshn.android.ui.dialog.select.rememberSelectRecipeBookDialogState
import de.kitshn.android.ui.layout.ResponsiveSideBySideLayout
import de.kitshn.android.ui.state.ErrorLoadingSuccessState
import de.kitshn.android.ui.state.rememberErrorLoadingSuccessState
import de.kitshn.android.ui.theme.Typography
import de.kitshn.android.ui.view.ViewParameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val RecipeServingsAmountSaveMap = mutableMapOf<Int, Int>()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewRecipeDetails(
    p: ViewParameters,

    client: TandoorClient?,
    recipeId: Int,
    shareToken: String? = null,

    navigationIcon: @Composable (() -> Unit)? = null,
    prependContent: @Composable () -> Unit = { },

    dialogMode: Boolean = false,
    ignoreWindowInsets: Boolean = dialogMode,
    hideStatusBarBackground: Boolean = dialogMode,
    hideBackButton: Boolean = dialogMode,

    onClickKeyword: (keyword: TandoorKeywordOverview) -> Unit = {},

    overrideServings: Int? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val coroutineScope = rememberCoroutineScope()

    var pageLoadingState by rememberErrorLoadingSuccessState()

    val recipeIngredientAllocationDialogState =
        rememberRecipeIngredientAllocationDialogState(key = "ViewRecipeDetails/recipeIngredientAllocationDialogState")
    val recipeEditDialogState =
        rememberRecipeEditDialogState(key = "ViewRecipeDetails/recipeEditDialogState")
    val mealPlanCreationDialogState =
        rememberMealPlanCreationDialogState(key = "ViewRecipeDetails/mealPlanCreationDialogState")
    val addRecipeToBookSelectDialogState = rememberSelectRecipeBookDialogState()

    val recipeDeleteDialogState = rememberCommonDeletionDialogState<TandoorRecipe>()

    val recipeOverview = client?.container?.recipeOverview?.get(recipeId)
    if(recipeOverview == null) {
        FullSizeAlertPane(
            imageVector = Icons.Rounded.SearchOff,
            contentDescription = stringResource(R.string.recipe_not_found),
            text = stringResource(R.string.recipe_not_found)
        )

        return
    }

    LaunchedEffect(overrideServings) {
        if(overrideServings == null) return@LaunchedEffect
        RecipeServingsAmountSaveMap[recipeOverview.id] = overrideServings
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())
    val appBarTitleAlphaAnim = remember { Animatable(0f) }
    var titleCheckpointX by remember { mutableFloatStateOf(0f) }

    // calculate top bar title visibility
    LaunchedEffect(recipeOverview) {
        while(true) {
            delay(50)

            val offset = (-scrollBehavior.state.contentOffset) - 200f
            if(offset > titleCheckpointX) {
                if(appBarTitleAlphaAnim.value == 0f) appBarTitleAlphaAnim.animateTo(1f, tween(500))
            } else {
                if(appBarTitleAlphaAnim.value == 1f) appBarTitleAlphaAnim.animateTo(0f, tween(500))
            }
        }
    }

    val recipe = p.vm.tandoorClient!!.container.recipe.getOrElse(recipeOverview.id) { null }
    LaunchedEffect(recipeOverview) {
        pageLoadingState = ErrorLoadingSuccessState.LOADING
        TandoorRequestState().wrapRequest {
            p.vm.tandoorClient?.recipe?.get(
                id = recipeOverview.id,
                share = shareToken
            )
        }
    }

    val recipeLinkBottomSheetState = rememberRecipeLinkBottomSheetState()

    // calculate ingredients list
    val sortedStepsList = remember { mutableStateListOf<TandoorStep>() }
    val sortedIngredientsList = remember { mutableStateListOf<TandoorIngredient>() }

    // sort steps and ingredients
    LaunchedEffect(recipe) {
        val beginLoading = System.currentTimeMillis()

        sortedIngredientsList.clear()
        if(recipe == null) return@LaunchedEffect

        sortedStepsList.clear()
        sortedStepsList.addAll(recipe.sortSteps())

        sortedStepsList.forEach {
            sortedIngredientsList.addAll(it.ingredients)
        }

        val delay = 400 - (System.currentTimeMillis() - beginLoading)
        if(delay > 0L) delay(delay)

        pageLoadingState = ErrorLoadingSuccessState.SUCCESS
    }

    var servingsValue by remember { mutableIntStateOf(1) }
    var servingsFactor by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(servingsValue) {
        servingsFactor = servingsValue.toDouble() / (recipe?.servings ?: 1).toDouble()
    }

    LaunchedEffect(recipe) {
        if(recipe == null) return@LaunchedEffect
        servingsValue = RecipeServingsAmountSaveMap[recipe.id] ?: recipe.servings
    }

    DisposableEffect(Unit) {
        onDispose {
            if(recipe == null) return@onDispose
            RecipeServingsAmountSaveMap[recipe.id] = servingsValue
        }
    }

    @Composable
    fun SourceButton() {
        var showSourceDialog by remember { mutableStateOf(false) }
        if(showSourceDialog) AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            icon = { Icon(Icons.Rounded.Link, stringResource(R.string.common_source)) },
            title = { Text(text = stringResource(R.string.common_source)) },
            text = { Text(text = recipe?.source_url ?: stringResource(R.string.common_unknown)) },
            confirmButton = {
                Button(
                    onClick = { showSourceDialog = false }
                ) {
                    Text(text = stringResource(id = R.string.common_okay))
                }
            }
        )

        Box(
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if(recipe?.source_url != null) OutlinedButton(
                onClick = {
                    try {
                        context.launchCustomTabs(recipe.source_url)
                    } catch(e: Exception) {
                        showSourceDialog = true
                    }
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    stringResource(R.string.action_open_source)
                )

                Spacer(Modifier.width(8.dp))

                Text(stringResource(R.string.action_open_original))
            }
        }
    }

    Scaffold(
        topBar = {
            if(!dialogMode) TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    if(navigationIcon != null) {
                        navigationIcon()
                    } else {
                        if(!hideBackButton) BackButton(p.back, true)
                    }
                },
                title = {
                    Text(
                        text = recipeOverview.name,
                        Modifier.alpha(appBarTitleAlphaAnim.value),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                actions = {
                    // hide if recipe is shared
                    if(shareToken != null) return@TopAppBar

                    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }

                    FilledIconButton(
                        onClick = {
                            isMenuExpanded = true
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Icon(Icons.Rounded.MoreVert, stringResource(R.string.action_more))
                    }

                    RecipeDetailsDropdown(
                        vm = p.vm,
                        recipeOverview = recipeOverview,
                        expanded = isMenuExpanded,
                        onEdit = { recipe?.let { recipeEditDialogState.open(it) } },
                        onDelete = { recipe?.let { recipeDeleteDialogState.open(it) } },
                        onAddToRecipeBook = { addRecipeToBookSelectDialogState.open() },
                        onAddToMealPlan = {
                            mealPlanCreationDialogState.open(
                                MealPlanCreationAndEditDefaultValues(
                                    recipeId = recipeOverview.id
                                )
                            )
                        },
                        onAllocateIngredients = {
                            recipe?.let {
                                recipeIngredientAllocationDialogState.open(
                                    it
                                )
                            }
                        }
                    ) {
                        isMenuExpanded = false
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                p.vm.navHostController?.navigate("recipe/${recipeOverview.id}/cook/${servingsValue}")
            }) {
                Icon(
                    imageVector = Icons.Rounded.LocalDining,
                    contentDescription = stringResource(R.string.action_start_cooking)
                )
            }
        },
        containerColor = if(dialogMode)
            MaterialTheme.colorScheme.surfaceContainerLow
        else
            MaterialTheme.colorScheme.background,
        contentWindowInsets = if(ignoreWindowInsets)
            WindowInsets(0.dp)
        else
            ScaffoldDefaults.contentWindowInsets
    ) { pv ->
        Column(
            Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(
                    bottom = pv.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            var notEnoughSpace by remember { mutableStateOf(false) }

            Box {
                AsyncImage(
                    model = recipeOverview.loadThumbnail(),
                    contentDescription = recipeOverview.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .run {
                            if(dialogMode) {
                                this
                                    .padding(bottom = 8.dp)
                                    .clip(RoundedCornerShape(24.dp))
                            } else {
                                this
                            }
                        }
                )

                // don't show if recipe is shared
                if(dialogMode && shareToken == null) {
                    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }

                    SmallFloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 24.dp, end = 16.dp),
                        onClick = { isMenuExpanded = !isMenuExpanded }
                    ) {
                        Icon(Icons.Rounded.MoreHoriz, stringResource(R.string.action_more))
                    }

                    if(isMenuExpanded) BasicAlertDialog(
                        onDismissRequest = { isMenuExpanded = false },
                        properties = DialogProperties(
                            usePlatformDefaultWidth = false
                        )
                    ) {
                        Surface(
                            shape = AlertDialogDefaults.shape,
                            color = AlertDialogDefaults.containerColor,
                            tonalElevation = AlertDialogDefaults.TonalElevation
                        ) {
                            Column(
                                Modifier.padding(16.dp)
                            ) {
                                RecipeDetailsDropdownContent(
                                    vm = p.vm,
                                    recipeOverview = recipeOverview,
                                    onEdit = { recipe?.let { recipeEditDialogState.open(it) } },
                                    onDelete = { recipe?.let { recipeDeleteDialogState.open(it) } },
                                    onAddToRecipeBook = { addRecipeToBookSelectDialogState.open() },
                                    onAddToMealPlan = {
                                        mealPlanCreationDialogState.open(
                                            MealPlanCreationAndEditDefaultValues(
                                                recipeId = recipeOverview.id
                                            )
                                        )
                                    },
                                    onAllocateIngredients = {
                                        recipe?.let {
                                            recipeIngredientAllocationDialogState.open(
                                                it
                                            )
                                        }
                                    }
                                ) {
                                    isMenuExpanded = false
                                }
                            }
                        }
                    }
                }
            }

            ResponsiveSideBySideLayout(
                rightMinWidth = 300.dp,
                rightMaxWidth = 500.dp,
                leftMinWidth = 300.dp,
                leftLayout = { enoughSpace ->
                    notEnoughSpace = !enoughSpace

                    Column(
                        Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 8.dp
                        )
                    ) {
                        Text(
                            text = recipeOverview.name,
                            Modifier.onGloballyPositioned { lc ->
                                titleCheckpointX = lc.boundsInRoot().bottom
                            },
                            style = Typography.titleLarge
                        )
                    }

                    LazyRow(
                        Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            Spacer(Modifier.width(16.dp))
                        }

                        items(
                            recipeOverview.keywords.size,
                            key = { recipeOverview.keywords[it].id }) { index ->
                            val keywordOverview = recipeOverview.keywords[index]

                            FilterChip(onClick = {
                                onClickKeyword(keywordOverview)
                            }, label = {
                                Text(keywordOverview.label)
                            }, selected = true)

                            Spacer(Modifier.width(16.dp))
                        }
                    }

                    prependContent()

                    FlowRow(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val workingTime = recipe?.working_time ?: 1
                        if(workingTime > 0) RecipeInfoBlob(
                            icon = Icons.Rounded.Person,
                            label = stringResource(id = R.string.common_time_work),
                            loadingState = pageLoadingState
                        ) {
                            Text(
                                text = "$workingTime ${stringResource(id = R.string.common_minute_min)}"
                            )
                        }

                        if(recipe == null || recipe.rating != null) RecipeInfoBlob(
                            icon = Icons.Rounded.Reviews,
                            label = stringResource(id = R.string.common_review),
                            loadingState = pageLoadingState
                        ) {
                            FiveStarIconRow(
                                rating = recipe?.rating ?: 5.0
                            )
                        }

                        val waitingTime = recipe?.waiting_time ?: 1
                        if(waitingTime > 0) RecipeInfoBlob(
                            icon = Icons.Rounded.Pause,
                            label = stringResource(id = R.string.common_time_wait),
                            loadingState = pageLoadingState
                        ) {
                            Text(
                                text = "$waitingTime ${stringResource(id = R.string.common_minute_min)}"
                            )
                        }
                    }

                    if(!recipeOverview.description.isNullOrBlank()) Text(
                        text = recipeOverview.description,
                        Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                    )

                    if(enoughSpace) SourceButton()
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ServingsSelector(
                        value = servingsValue,
                        label = recipe?.servings_text ?: "",
                        loadingState = pageLoadingState
                    ) { value ->
                        servingsValue = value
                    }

                    if(recipe?.showIngredientAllocationActionChip() == true) WideActionChip(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        type = WideActionChipType.INFO,
                        actionLabel = stringResource(id = R.string.error_unallocated_ingredients)
                    ) {
                        recipeIngredientAllocationDialogState.open(recipe)
                    }

                    Card(
                        Modifier.padding(
                            start = if(dialogMode) 0.dp else 16.dp,
                            end = if(dialogMode) 0.dp else 16.dp,
                            bottom = 16.dp
                        )
                    ) {
                        Box {
                            IngredientsList(
                                list = sortedIngredientsList,
                                factor = servingsFactor,
                                loadingState = pageLoadingState,
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                        }
                    }
                }
            }

            if(pageLoadingState == ErrorLoadingSuccessState.LOADING && sortedStepsList.size == 0) {
                repeat(3) {
                    RecipeStepCard(
                        Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            )
                            .fillMaxWidth(),
                        loadingState = pageLoadingState,
                        servingsFactor = servingsFactor
                    ) { }
                }
            } else {
                var index = 0
                sortedStepsList.forEach { step ->
                    RecipeStepCard(
                        Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            )
                            .fillMaxWidth(),
                        step = step,
                        stepIndex = index,
                        hideIngredients = step.ingredients.size == sortedIngredientsList.size,
                        servingsFactor = servingsFactor
                    ) { recipe ->
                        // show recipe link bottom sheet
                        recipeLinkBottomSheetState.open(recipe.toOverview())
                    }

                    index++
                }
            }

            if(notEnoughSpace && recipe?.source_url != null) SourceButton()
        }

        if(WindowInsets.areStatusBarsVisible && !hideStatusBarBackground) Box(
            Modifier
                .height(with(density) {
                    WindowInsets.statusBars
                        .getTop(density)
                        .toDp()
                })
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.35f))
        )
    }

    RecipeIngredientAllocationDialog(
        state = recipeIngredientAllocationDialogState
    ) {
        // refresh recipe after ingredient allocation
        coroutineScope.launch {
            TandoorRequestState().wrapRequest {
                client.recipe.get(recipeOverview.id).toOverview().let {
                    client.container.recipeOverview[it.id] = it
                }
            }
        }
    }

    // DIALOGS

    if(p.vm.tandoorClient != null) {
        RecipeCreationAndEditDialog(
            client = p.vm.tandoorClient!!,
            editState = recipeEditDialogState,
            onRefresh = {
                // refresh recipe after edit
                coroutineScope.launch {
                    TandoorRequestState().wrapRequest {
                        client.recipe.get(recipeOverview.id).toOverview().let {
                            client.container.recipeOverview[it.id] = it
                        }
                    }
                }
            }
        )

        CommonDeletionDialog(
            state = recipeDeleteDialogState,
            onConfirm = {
                coroutineScope.launch {
                    it.delete()
                    p.back?.let { it() }
                }
            }
        )

        MealPlanCreationAndEditDialog(
            client = p.vm.tandoorClient!!,
            creationState = mealPlanCreationDialogState
        ) {
            p.vm.mainSubNavHostController?.navigate("mealplan")
        }

        SelectRecipeBookDialog(
            client = p.vm.tandoorClient!!,
            favoritesRecipeBookId = p.vm.favorites.getFavoritesRecipeBookIdSync(),
            state = addRecipeToBookSelectDialogState
        ) {
            coroutineScope.launch { it.createEntry(recipeOverview.id) }
        }
    }

    RecipeLinkBottomSheet(
        p = p,
        state = recipeLinkBottomSheetState
    )
}