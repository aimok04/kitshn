package de.kitshn.ui.dialog.recipe.import

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeFromSource
import de.kitshn.ui.component.settings.SettingsListItemPosition
import de.kitshn.ui.component.settings.SettingsSwitchListItem
import de.kitshn.ui.dialog.PhotoPickerDialog
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_click_to_upload_image
import kitshn.composeapp.generated.resources.common_selected
import kitshn.composeapp.generated.resources.common_steps
import kitshn.composeapp.generated.resources.common_tags
import kitshn.composeapp.generated.resources.common_title_image
import kitshn.composeapp.generated.resources.recipe_import_auto_sort_ingredients
import kitshn.composeapp.generated.resources.recipe_import_auto_sort_ingredients_description
import kitshn.composeapp.generated.resources.recipe_import_divide_steps
import kitshn.composeapp.generated.resources.recipe_import_divide_steps_description
import org.jetbrains.compose.resources.stringResource

// ensure recipe gets imported only once (issue #29)
val IMPORT_URI_BLACKLIST = mutableListOf<String>()

class RecipeImportCommonStateData {

    var url by mutableStateOf("")
    var recipeFromSource by mutableStateOf<TandoorRecipeFromSource?>(null)

    val availableImageUrls = mutableStateListOf<String>()
    var selectedImageUrl by mutableStateOf("")
    var uploadImage by mutableStateOf<ByteArray?>(null)

    val selectedKeywords = mutableStateListOf<String>()

    var splitSteps by mutableStateOf(true)
    var autoSortIngredients by mutableStateOf(true)

    fun populate() {
        availableImageUrls.clear()
        recipeFromSource!!.recipe!!.imageUrl.ifBlank { null }?.let {
            availableImageUrls.add(it)
        }
        availableImageUrls.addAll(recipeFromSource!!.images)

        selectedImageUrl = availableImageUrls.getOrNull(0) ?: ""

        selectedKeywords.clear()
        selectedKeywords.addAll(recipeFromSource!!.recipe!!.keywords.map { it.name ?: "" })
    }

    suspend fun import(
        onViewRecipe: (recipe: TandoorRecipe) -> Unit,
        onDismiss: () -> Unit
    ) {
        if(url.isNotBlank()) {
            // ensure recipe gets imported only once (issue #29)
            if(IMPORT_URI_BLACKLIST.contains(url)) return
            IMPORT_URI_BLACKLIST.add(url)
        }

        val recipe = recipeFromSource!!.create(
            imageUrl = selectedImageUrl,
            keywords = selectedKeywords,
            splitSteps = splitSteps,
            autoSortIngredients = autoSortIngredients
        )

        if(uploadImage != null) {
            recipe.uploadImage(uploadImage!!)
        } else {
            recipe.setImageUrl(selectedImageUrl)
        }

        onDismiss()
        onViewRecipe(recipe)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
fun LazyListScope.RecipeImportCommon(
    fetchRequestState: TandoorRequestState,
    data: RecipeImportCommonStateData,

    containerSize: Dp,
    displayDivider: Boolean = true
) {
    if(fetchRequestState.state == TandoorRequestStateState.SUCCESS && data.recipeFromSource != null) {
        item {
            if(displayDivider) HorizontalDivider(
                Modifier.padding(start = 16.dp, end = 16.dp)
            )
        }

        item {
            Text(
                text = stringResource(Res.string.common_title_image),
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                style = Typography().titleLarge
            )
        }

        item {
            val context = LocalPlatformContext.current
            val imageLoader = remember { ImageLoader(context) }

            var showPhotoPicker by remember { mutableStateOf(false) }

            HorizontalMultiBrowseCarousel(
                state = rememberCarouselState { data.availableImageUrls.size + 1 },
                preferredItemWidth = 250.dp,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp
                )
            ) {
                if(it == data.availableImageUrls.size) {
                    Box(
                        modifier = Modifier
                            .height(containerSize)
                    ) {
                        if(data.uploadImage != null) {
                            AsyncImage(
                                modifier = Modifier
                                    .height(containerSize)
                                    .maskClip(MaterialTheme.shapes.extraLarge)
                                    .clickable {
                                        showPhotoPicker = true
                                    },
                                model = data.uploadImage,
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                imageLoader = imageLoader
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(containerSize)
                                    .maskClip(MaterialTheme.shapes.extraLarge)
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .clickable {
                                        showPhotoPicker = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = stringResource(Res.string.common_selected),
                                    tint = Color.White
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(containerSize)
                                    .maskClip(MaterialTheme.shapes.extraLarge)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable {
                                        showPhotoPicker = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Upload,
                                    contentDescription = stringResource(Res.string.action_click_to_upload_image),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                } else {
                    val url = data.availableImageUrls[it]

                    Box(
                        modifier = Modifier
                            .height(containerSize)
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .height(containerSize)
                                .maskClip(MaterialTheme.shapes.extraLarge)
                                .clickable {
                                    data.uploadImage = null
                                    data.selectedImageUrl = url
                                },
                            model = url,
                            contentDescription = url,
                            contentScale = ContentScale.Crop,
                            imageLoader = imageLoader
                        )

                        if(data.selectedImageUrl == url) Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(containerSize)
                                .maskClip(MaterialTheme.shapes.extraLarge)
                                .background(Color.Black.copy(alpha = 0.3f))
                                .clickable {
                                    data.uploadImage = null
                                    data.selectedImageUrl = url
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(Res.string.common_selected),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            PhotoPickerDialog(
                shown = showPhotoPicker,
                onSelect = {
                    data.uploadImage = it
                },
                onDismiss = {
                    showPhotoPicker = false
                }
            )
        }

        item {
            Text(
                text = stringResource(Res.string.common_tags),
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                style = Typography().titleLarge
            )
        }

        item {
            LazyColumn(
                Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .height(containerSize)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                val keywords = data.recipeFromSource!!.recipe!!.keywords

                items(keywords.size) {
                    val keyword = keywords[it]
                    val checked = data.selectedKeywords.contains(keyword.name)

                    fun toggle() {
                        if(checked) {
                            data.selectedKeywords.remove(keyword.name)
                        } else {
                            data.selectedKeywords.add(keyword.name ?: "")
                        }
                    }

                    ListItem(
                        modifier = Modifier
                            .clickable { toggle() },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        leadingContent = {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { toggle() }
                            )
                        },
                        headlineContent = {
                            Text(keyword.label ?: "")
                        }
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(Res.string.common_steps),
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                style = Typography().titleLarge
            )
        }

        item {
            SettingsSwitchListItem(
                position = SettingsListItemPosition.TOP,
                label = { Text(text = stringResource(Res.string.recipe_import_divide_steps)) },
                description = { Text(text = stringResource(Res.string.recipe_import_divide_steps_description)) },
                icon = Icons.Rounded.Compress,
                contentDescription = stringResource(Res.string.recipe_import_divide_steps),
                checked = data.splitSteps
            ) {
                data.splitSteps = it
            }
        }

        item {
            SettingsSwitchListItem(
                position = SettingsListItemPosition.BOTTOM,
                label = { Text(text = stringResource(Res.string.recipe_import_auto_sort_ingredients)) },
                description = { Text(text = stringResource(Res.string.recipe_import_auto_sort_ingredients_description)) },
                icon = Icons.AutoMirrored.Rounded.Sort,
                contentDescription = stringResource(Res.string.recipe_import_auto_sort_ingredients),
                checked = data.autoSortIngredients
            ) {
                data.autoSortIngredients = it
            }
        }
    }
}