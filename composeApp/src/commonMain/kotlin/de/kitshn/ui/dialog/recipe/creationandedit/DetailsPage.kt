package de.kitshn.ui.dialog.recipe.creationandedit

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
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.model.form.KitshnForm
import de.kitshn.model.form.KitshnFormSection
import de.kitshn.model.form.item.KitshnFormImageUploadItem
import de.kitshn.model.form.item.field.KitshnFormIntegerFieldItem
import de.kitshn.model.form.item.field.KitshnFormTextFieldItem
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_description
import kitshn.composeapp.generated.resources.common_minute_min
import kitshn.composeapp.generated.resources.common_name
import kitshn.composeapp.generated.resources.common_portions
import kitshn.composeapp.generated.resources.common_portions_text
import kitshn.composeapp.generated.resources.common_prepairing
import kitshn.composeapp.generated.resources.common_source
import kitshn.composeapp.generated.resources.common_time_wait
import kitshn.composeapp.generated.resources.common_title_image
import kitshn.composeapp.generated.resources.form_error_description_max_512
import kitshn.composeapp.generated.resources.form_error_name_max_128
import kitshn.composeapp.generated.resources.form_error_portions_text_max_32
import kitshn.composeapp.generated.resources.form_error_source_max_1024
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun detailsPage(
    recipe: TandoorRecipe?,
    values: RecipeCreationAndEditDialogValue
): KitshnForm {
    val form = remember {
        KitshnForm(
            sections = listOf(
                KitshnFormSection(
                    listOf(
                        KitshnFormImageUploadItem(
                            currentImage = { recipe?.loadThumbnail() },

                            value = { values.imageUploadByteArray },
                            onValueChange = { values.imageUploadByteArray = it },
                            label = { stringResource(Res.string.common_title_image) }
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

                            label = { Text(stringResource(Res.string.common_name)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Label,
                                    stringResource(Res.string.common_name)
                                )
                            },

                            optional = false,
                            singleLine = true,

                            check = {
                                if(it.length > 128) {
                                    getString(Res.string.form_error_name_max_128)
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

                            label = { Text(stringResource(Res.string.common_description)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Notes,
                                    stringResource(Res.string.common_description)
                                )
                            },

                            optional = true,

                            minLines = 1,
                            maxLines = 100,
                            singleLine = false,

                            check = {
                                if(it.length > 512) {
                                    getString(Res.string.form_error_description_max_512)
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

                            label = { Text(stringResource(Res.string.common_portions)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Numbers,
                                    stringResource(Res.string.common_portions)
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

                            label = { Text(stringResource(Res.string.common_portions_text)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Numbers,
                                    stringResource(Res.string.common_portions_text)
                                )
                            },

                            optional = true,

                            singleLine = true,

                            check = {
                                if(it.length > 32) {
                                    getString(Res.string.form_error_portions_text_max_32)
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

                            label = { Text(stringResource(Res.string.common_prepairing)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Timer,
                                    stringResource(Res.string.common_prepairing)
                                )
                            },
                            suffix = { Text(stringResource(Res.string.common_minute_min)) },

                            min = { 0 },

                            optional = true,

                            check = { null }
                        ),
                        KitshnFormIntegerFieldItem(
                            value = { values.waitingTime },
                            onValueChange = { values.waitingTime = it },

                            label = { Text(stringResource(Res.string.common_time_wait)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Timer,
                                    stringResource(Res.string.common_time_wait)
                                )
                            },
                            suffix = { Text(stringResource(Res.string.common_minute_min)) },

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

                            label = { Text(stringResource(Res.string.common_source)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.AttachFile,
                                    stringResource(Res.string.common_source)
                                )
                            },

                            optional = true,
                            singleLine = true,

                            check = {
                                if(it.length > 1024) {
                                    getString(Res.string.form_error_source_max_1024)
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