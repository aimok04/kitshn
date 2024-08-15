package de.kitshn.android.ui.dialog.recipe.creationandedit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.android.model.form.KitshnForm
import de.kitshn.android.model.form.KitshnFormSection
import de.kitshn.android.model.form.item.KitshnFormImageUploadItem
import de.kitshn.android.model.form.item.field.KitshnFormIntegerFieldItem
import de.kitshn.android.model.form.item.field.KitshnFormTextFieldItem

@Composable
fun detailsPage(
    recipe: TandoorRecipe?,
    values: RecipeCreationAndEditDialogValue
): KitshnForm {
    val context = LocalContext.current

    val form = remember {
        KitshnForm(
            sections = listOf(
                KitshnFormSection(
                    listOf(
                        KitshnFormImageUploadItem(
                            currentImage = { recipe?.loadThumbnail() },

                            value = { values.imageUploadUri },
                            onValueChange = { values.imageUploadUri = it },

                            label = context.getString(R.string.common_title_image)
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormTextFieldItem(
                            value = { values.name },
                            onValueChange = {
                                values.name = it
                            },

                            label = { Text(stringResource(R.string.common_name)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Label,
                                    stringResource(R.string.common_name)
                                )
                            },

                            optional = false,
                            singleLine = true,

                            check = {
                                if(it.length > 128) {
                                    context.getString(R.string.form_error_name_max_128)
                                } else {
                                    null
                                }
                            }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormTextFieldItem(
                            value = { values.description ?: "" },
                            onValueChange = {
                                values.description = it
                            },

                            label = { Text(stringResource(R.string.common_description)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Notes,
                                    stringResource(R.string.common_description)
                                )
                            },

                            optional = true,

                            minLines = 1,
                            maxLines = 100,
                            singleLine = false,

                            check = {
                                if(it.length > 512) {
                                    context.getString(R.string.form_error_description_max_512)
                                } else {
                                    null
                                }
                            }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormIntegerFieldItem(
                            value = { values.servings },
                            onValueChange = { values.servings = it },

                            label = { Text(stringResource(id = R.string.common_portions)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Numbers,
                                    stringResource(id = R.string.common_portions)
                                )
                            },

                            min = { 1 },

                            optional = false,

                            check = { null }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormTextFieldItem(
                            value = { values.servingsText ?: "" },
                            onValueChange = {
                                values.servingsText = it
                            },

                            label = { Text(stringResource(R.string.common_portions_text)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Numbers,
                                    stringResource(R.string.common_portions_text)
                                )
                            },

                            optional = true,

                            singleLine = true,

                            check = {
                                if(it.length > 32) {
                                    context.getString(R.string.form_error_portions_text_max_32)
                                } else {
                                    null
                                }
                            }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormIntegerFieldItem(
                            value = { values.workingTime },
                            onValueChange = { values.workingTime = it },

                            label = { Text(stringResource(R.string.common_prepairing)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Timer,
                                    stringResource(R.string.common_prepairing)
                                )
                            },
                            suffix = { Text(stringResource(id = R.string.common_minute_min)) },

                            min = { 0 },

                            optional = true,

                            check = { null }
                        ),
                        KitshnFormIntegerFieldItem(
                            value = { values.waitingTime },
                            onValueChange = { values.waitingTime = it },

                            label = { Text(stringResource(R.string.common_time_wait)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Timer,
                                    stringResource(R.string.common_time_wait)
                                )
                            },
                            suffix = { Text(stringResource(id = R.string.common_minute_min)) },

                            min = { 0 },

                            optional = true,

                            check = { null }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormTextFieldItem(
                            value = { values.sourceUrl ?: "" },
                            onValueChange = {
                                values.sourceUrl = it
                            },

                            label = { Text(stringResource(R.string.common_source)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.AttachFile,
                                    stringResource(R.string.common_source)
                                )
                            },

                            optional = true,
                            singleLine = true,

                            check = {
                                if(it.length > 1024) {
                                    context.getString(R.string.form_error_source_max_1024)
                                } else {
                                    null
                                }
                            }
                        )
                    )
                )
            )
        )
    }

    form.Render()
    return form
}