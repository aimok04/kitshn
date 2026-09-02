package de.kitshn.ui.dialog.space

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.PartialTandoorSpace
import de.kitshn.api.tandoor.model.TandoorSpace
import de.kitshn.parseTandoorDate
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.dialog.ChoosePhotoBottomSheet
import de.kitshn.ui.dialog.common.CommonDeletionDialog
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_create
import kitshn.shared.generated.resources.action_create_space
import kitshn.shared.generated.resources.action_delete
import kitshn.shared.generated.resources.action_edit_space
import kitshn.shared.generated.resources.action_reset
import kitshn.shared.generated.resources.action_save
import kitshn.shared.generated.resources.action_upload
import kitshn.shared.generated.resources.common_created
import kitshn.shared.generated.resources.common_creator
import kitshn.shared.generated.resources.common_image
import kitshn.shared.generated.resources.common_info
import kitshn.shared.generated.resources.common_member_count
import kitshn.shared.generated.resources.common_message
import kitshn.shared.generated.resources.common_name
import kitshn.shared.generated.resources.common_recipe_count
import kitshn.shared.generated.resources.common_storage
import kitshn.shared.generated.resources.common_storage_used
import kitshn.shared.generated.resources.form_error_message_max_500
import kitshn.shared.generated.resources.form_error_name_max_128
import kitshn.shared.generated.resources.space_creation_intro_description
import kitshn.shared.generated.resources.space_creation_intro_title
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Describes the user's edit to the existing space image.
 *
 *  - [Unchanged] – keep whatever the server already has.
 *  - [Replaced] – upload these bytes as the new image.
 *  - [Cleared] – remove the existing image.
 */
sealed interface SpaceImageEdit {
    data object Unchanged : SpaceImageEdit
    data class Replaced(val bytes: ByteArray) : SpaceImageEdit {
        override fun equals(other: Any?): Boolean =
            this === other || (other is Replaced && bytes.contentEquals(other.bytes))

        override fun hashCode(): Int = bytes.contentHashCode()
    }

    data object Cleared : SpaceImageEdit
}

data class SpaceCreationAndEditDialogState(
    val shown: Boolean = false,
    val editing: TandoorSpace? = null,
    val name: String = "",
    val message: String = "",
    val image: SpaceImageEdit = SpaceImageEdit.Unchanged,
    val deleteConfirmation: TandoorSpace? = null,
    val saveState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
    val deleteState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
) {
    val isEdit: Boolean get() = editing != null
    val isSaving: Boolean get() = saveState == TandoorRequestStateState.LOADING
    val isDeleting: Boolean get() = deleteState == TandoorRequestStateState.LOADING
    val isProcessing: Boolean get() = isSaving || isDeleting

    val nameError: NameError? = when {
        name.isBlank() -> NameError.Empty
        name.length > MAX_NAME_LENGTH -> NameError.TooLong
        else -> null
    }

    val messageError: MessageError? = when {
        message.length > MAX_MESSAGE_LENGTH -> MessageError.TooLong
        else -> null
    }

    val canSubmit: Boolean get() = !isProcessing && nameError == null && messageError == null

    enum class NameError { Empty, TooLong }
    enum class MessageError { TooLong }

    companion object {
        const val MAX_NAME_LENGTH = 128
        const val MAX_MESSAGE_LENGTH = 500
    }
}

sealed interface SpaceCreationAndEditDialogEvent {
    data class NameChange(val name: String) : SpaceCreationAndEditDialogEvent
    data class MessageChange(val message: String) : SpaceCreationAndEditDialogEvent
    data class ImageReplace(val bytes: ByteArray) : SpaceCreationAndEditDialogEvent
    data object ImageReset : SpaceCreationAndEditDialogEvent
    data class Save(
        val partial: PartialTandoorSpace,
        val image: SpaceImageEdit,
    ) : SpaceCreationAndEditDialogEvent

    data object RequestDelete : SpaceCreationAndEditDialogEvent
    data class ConfirmDelete(val space: TandoorSpace) : SpaceCreationAndEditDialogEvent
    data object CancelDelete : SpaceCreationAndEditDialogEvent
    data object Dismiss : SpaceCreationAndEditDialogEvent
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SpaceCreationAndEditDialog(
    state: SpaceCreationAndEditDialogState,
    onEvent: (SpaceCreationAndEditDialogEvent) -> Unit,
    currentImage: @Composable (TandoorSpace) -> ImageRequest? = { null },
) {
    if (!state.shown) return

    AdaptiveFullscreenDialog(
        onDismiss = {
            if (!state.isProcessing) onEvent(SpaceCreationAndEditDialogEvent.Dismiss)
        },
        title = {
            Text(
                text = if (state.isEdit) {
                    stringResource(Res.string.action_edit_space)
                } else {
                    stringResource(Res.string.action_create_space)
                }
            )
        },
        topAppBarActions = {
            if (state.isEdit) {
                IconButton(
                    onClick = { onEvent(SpaceCreationAndEditDialogEvent.RequestDelete) },
                    enabled = !state.isProcessing,
                ) {
                    if (state.isDeleting) {
                        CircularWavyProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Rounded.Delete, stringResource(Res.string.action_delete))
                    }
                }
            }
        },
        actions = {
            Button(
                onClick = {
                    onEvent(
                        SpaceCreationAndEditDialogEvent.Save(
                            partial = PartialTandoorSpace(
                                name = state.name,
                                message = state.message.takeIf { it.isNotBlank() },
                            ),
                            image = state.image,
                        )
                    )
                },
                enabled = state.canSubmit,
            ) {
                if (state.isSaving) {
                    CircularWavyProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = if (state.isEdit) {
                            stringResource(Res.string.action_save)
                        } else {
                            stringResource(Res.string.action_create)
                        }
                    )
                }
            }
        }
    ) { scrollConnection, _, _ ->
        SpaceCreationAndEditDialogContent(
            state = state,
            onEvent = onEvent,
            currentImage = currentImage,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollConnection)
                .verticalScroll(rememberScrollState()),
        )
    }

    CommonDeletionDialog(
        model = state.deleteConfirmation,
        onConfirm = { onEvent(SpaceCreationAndEditDialogEvent.ConfirmDelete(it)) },
        onDismiss = { onEvent(SpaceCreationAndEditDialogEvent.CancelDelete) },
    )
}

@Composable
private fun SpaceCreationAndEditDialogContent(
    state: SpaceCreationAndEditDialogState,
    onEvent: (SpaceCreationAndEditDialogEvent) -> Unit,
    currentImage: @Composable (TandoorSpace) -> ImageRequest?,
    modifier: Modifier = Modifier,
) {
    var photoSheetVisible by remember { mutableStateOf(false) }
    ChoosePhotoBottomSheet(
        shown = photoSheetVisible,
        onSelect = { onEvent(SpaceCreationAndEditDialogEvent.ImageReplace(it)) },
        onDismiss = { photoSheetVisible = false },
    )

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ProfileHeader(
            state = state,
            onEvent = onEvent,
            currentImage = currentImage,
            onPickImage = { photoSheetVisible = true },
        )

        if (state.isEdit) {
            state.editing?.let { space ->
                HorizontalDivider()
                InfoSection(space = space)
            }
        } else {
            IntroCard()
        }
    }
}

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ViewCarousel,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(Res.string.space_creation_intro_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(Res.string.space_creation_intro_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    state: SpaceCreationAndEditDialogState,
    onEvent: (SpaceCreationAndEditDialogEvent) -> Unit,
    currentImage: @Composable (TandoorSpace) -> ImageRequest?,
    onPickImage: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfilePictureBox(
            state = state,
            currentImage = currentImage,
            onPickImage = onPickImage,
            onResetImage = { onEvent(SpaceCreationAndEditDialogEvent.ImageReset) },
            modifier = Modifier.size(112.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextField(
                value = state.name,
                onValueChange = { onEvent(SpaceCreationAndEditDialogEvent.NameChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.common_name)) },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Rounded.Label, null)
                },
                singleLine = true,
                enabled = !state.isProcessing,
                isError = state.nameError != null && state.name.isNotEmpty(),
                supportingText = state.nameError
                    ?.takeIf { state.name.isNotEmpty() }
                    ?.let { error ->
                        {
                            Text(
                                text = when (error) {
                                    SpaceCreationAndEditDialogState.NameError.TooLong ->
                                        stringResource(Res.string.form_error_name_max_128)

                                    SpaceCreationAndEditDialogState.NameError.Empty -> ""
                                },
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                shape = TextFieldDefaults.shape,
            )

            TextField(
                value = state.message,
                onValueChange = { onEvent(SpaceCreationAndEditDialogEvent.MessageChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.common_message)) },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Rounded.Notes, null)
                },
                minLines = 1,
                maxLines = 4,
                enabled = !state.isProcessing,
                isError = state.messageError != null,
                supportingText = state.messageError?.let { error ->
                    {
                        Text(
                            text = when (error) {
                                SpaceCreationAndEditDialogState.MessageError.TooLong ->
                                    stringResource(Res.string.form_error_message_max_500)
                            },
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                shape = TextFieldDefaults.shape,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfilePictureBox(
    state: SpaceCreationAndEditDialogState,
    currentImage: @Composable (TandoorSpace) -> ImageRequest?,
    onPickImage: () -> Unit,
    onResetImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val replaced = state.image as? SpaceImageEdit.Replaced
    val cleared = state.image is SpaceImageEdit.Cleared
    val showResetButton = state.image !is SpaceImageEdit.Unchanged

    val existingImage = if (state.editing != null) currentImage(state.editing) else null
    val displayedImage: Any? = when {
        replaced != null -> replaced.bytes
        cleared -> null
        else -> existingImage
    }

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedCard(
            shape = CircleShape,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable(enabled = !state.isProcessing, onClick = onPickImage),
                contentAlignment = Alignment.Center,
            ) {
                if (displayedImage != null) {
                    AsyncImage(
                        model = displayedImage,
                        contentDescription = stringResource(Res.string.common_image),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = stringResource(Res.string.common_image),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 4.dp, y = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showResetButton) {
                SmallFloatingActionButton(
                    onClick = onResetImage,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Icon(Icons.Rounded.Restore, stringResource(Res.string.action_reset))
                }
            }
            SmallFloatingActionButton(
                onClick = onPickImage,
            ) {
                Icon(Icons.Rounded.Upload, stringResource(Res.string.action_upload))
            }
        }
    }
}

@Composable
private fun InfoSection(space: TandoorSpace) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(Res.string.common_info),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        )

        OutlinedCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            space.created_by?.let { creator ->
                val name = creator.display_name.ifBlank { creator.username }
                if (name.isNotBlank()) {
                    InfoRow(
                        icon = Icons.Rounded.AccountCircle,
                        label = stringResource(Res.string.common_creator),
                        value = name,
                    )
                }
            }

            space.created_at?.let { createdAt ->
                val formatted = runCatching { createdAt.parseTandoorDate().toString() }.getOrNull()
                if (formatted != null) {
                    InfoRow(
                        icon = Icons.Rounded.CalendarToday,
                        label = stringResource(Res.string.common_created),
                        value = formatted,
                    )
                }
            }

            InfoRow(
                icon = Icons.Rounded.MenuBook,
                label = pluralStringResource(
                    Res.plurals.common_recipe_count,
                    space.recipe_count,
                    space.recipe_count,
                ),
                value = null,
            )

            InfoRow(
                icon = Icons.Rounded.Group,
                label = pluralStringResource(
                    Res.plurals.common_member_count,
                    space.user_count,
                    space.user_count,
                ),
                value = null,
            )

            InfoRow(
                icon = Icons.Rounded.Storage,
                label = stringResource(Res.string.common_storage),
                value = if (space.max_file_storage_mb > 0) {
                    stringResource(
                        Res.string.common_storage_used,
                        space.file_size_mb.toFormattedMb(),
                        space.max_file_storage_mb.toString(),
                    )
                } else {
                    "${space.file_size_mb.toFormattedMb()} MB"
                },
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String?,
) {
    ListItem(
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        headlineContent = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = value?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

private fun Double.toFormattedMb(): String {
    val rounded = (this * 10).toLong() / 10.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
