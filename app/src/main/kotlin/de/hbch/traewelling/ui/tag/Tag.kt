package de.hbch.traewelling.ui.tag

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.status.Tag
import de.hbch.traewelling.api.models.status.TagType
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.Dialog
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusTags(
    statusId: Int,
    modifier: Modifier = Modifier,
    isOwnStatus: Boolean = false,
    defaultVisibility: StatusVisibility = StatusVisibility.PUBLIC
) {
    val tags = remember { mutableStateListOf<Tag>() }
    val tagViewModel: TagViewModel = viewModel()
    var tagsRequested by remember { mutableStateOf(false) }
    var tagFormVisible by remember { mutableStateOf(false) }
    var tagFormData by remember { mutableStateOf<Tag?>(null) }

    LaunchedEffect(tagsRequested) {
        if (!tagsRequested) {
            tags.clear()
            tagsRequested = true
            tagViewModel.getTagsForStatus(
                statusId,
                {
                    tags.addAll(it)
                },
                { }
            )
        }
    }

    if (tagFormVisible) {
        Dialog(
            onDismissRequest = { tagFormVisible = false }
        ) {
            Box(
                modifier = Modifier.padding(16.dp)
            ) {
                TagForm(
                    tagData = tagFormData,
                    tagViewModel = tagViewModel,
                    statusId = statusId,
                    alreadyAddedTags = tags.map { it.safeKey },
                    onSaveSucceeded = {
                        tagsRequested = false
                        tagFormVisible = false
                    },
                    defaultVisibility = defaultVisibility
                )
            }
        }
    }

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isOwnStatus) {
            val addTagIcon = R.drawable.ic_add_tag
            val addTagTitle = R.string.add_tag
            val onAddTagClick: () -> Unit = {
                tagFormData = null
                tagFormVisible = true
            }
            if (tags.isEmpty()) {
                OutlinedButtonWithIconAndText(
                    stringId = addTagTitle,
                    drawableId = addTagIcon,
                    onClick = onAddTagClick
                )
            } else {
                OutlinedIconButton(
                    onClick = onAddTagClick,
                    content = {
                        Icon(
                            painter = painterResource(id = addTagIcon),
                            contentDescription = stringResource(id = addTagTitle)
                        )
                    }
                )
            }
        }
        tags.forEach {
            StatusTag(
                tag = it,
                isOwnTag = isOwnStatus,
                onClick = {
                    tagFormData = it
                    tagFormVisible = true
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusTag(
    tag: Tag,
    modifier: Modifier = Modifier,
    isOwnTag: Boolean = false,
    onClick: () -> Unit = { }
) {
    val tooltipState = remember { PlainTooltipState() }
    val scope = rememberCoroutineScope()

    PlainTooltipBox(
        tooltip = {
            Text(
                text = stringResource(id = tag.safeKey.title)
            )
        },
        tooltipState = tooltipState
    ) {
        AssistChip(
            onClick = {
                if (isOwnTag && tag.safeKey != TagType.UNKNOWN) {
                    onClick()
                }  else {
                    scope.launch {
                        tooltipState.show()
                    }
                }
            },
            label = {
                Text(
                    text = tag.value
                )
            },
            modifier = modifier.tooltipAnchor(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = tag.safeKey.icon),
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
fun TagForm(
    tagData: Tag?,
    statusId: Int,
    tagViewModel: TagViewModel,
    alreadyAddedTags: List<TagType>,
    modifier: Modifier = Modifier,
    onSaveSucceeded: () -> Unit = { },
    defaultVisibility: StatusVisibility = StatusVisibility.PUBLIC
) {
    val isCreationMode = tagData == null
    val availableTagsToAdd = TagType
        .values()
        .filter { !alreadyAddedTags.contains(it) }
        .filter { it.selectable }
    var tagTypeSelectionVisible by remember { mutableStateOf(false) }
    var tagType by remember { mutableStateOf(tagData?.safeKey) }
    val type = tagType
    var tagValue by remember { mutableStateOf(tagData?.value) }
    val value = tagValue
    var tagVisibilitySelectionVisible by remember { mutableStateOf(false) }
    var tagVisibility by remember { mutableStateOf(tagData?.visibility ?: defaultVisibility) }
    var saving by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val title = if (isCreationMode) R.string.add_tag else R.string.edit_tag
        Text(
            text = stringResource(id = title),
            style = AppTypography.titleLarge
        )
        if (isCreationMode && availableTagsToAdd.isEmpty()) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                text = stringResource(R.string.all_tags_already_added),
                textAlign = TextAlign.Center
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    if (type == null) {
                        OutlinedButtonWithIconAndText(
                            stringId = R.string.select_tag_type,
                            drawableId = R.drawable.ic_add_tag,
                            onClick = { tagTypeSelectionVisible = true }
                        )
                    } else {
                        OutlinedIconButton(
                            onClick = { tagTypeSelectionVisible = true },
                            enabled = !(saving || deleting || !isCreationMode)
                        ) {
                            Icon(
                                painter = painterResource(id = type.icon),
                                contentDescription = stringResource(id = type.title)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = tagTypeSelectionVisible,
                        onDismissRequest = { tagTypeSelectionVisible = false }
                    ) {
                        availableTagsToAdd.forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(id = it.title)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = it.icon),
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    tagType = it
                                    tagTypeSelectionVisible = false
                                }
                            )
                        }
                    }
                }
                AnimatedVisibility(type != null) {
                    OutlinedTextField(
                        value = tagValue ?: "",
                        onValueChange = { tagValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = stringResource(id = type!!.title)
                            )
                        },
                        placeholder = {
                            Text(
                                text = stringResource(id = type!!.example),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        maxLines = 1,
                        singleLine = true,
                        enabled = !(saving || deleting),
                        trailingIcon = {
                            Box {
                                IconButton(
                                    onClick = { tagVisibilitySelectionVisible = true },
                                    enabled = !(saving || deleting)
                                ) {
                                    Icon(
                                        painter = painterResource(id = tagVisibility.icon),
                                        contentDescription = stringResource(id = tagVisibility.title)
                                    )
                                }
                                DropdownMenu(
                                    expanded = tagVisibilitySelectionVisible,
                                    onDismissRequest = { tagVisibilitySelectionVisible = false }
                                ) {
                                    StatusVisibility.values().forEach {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(id = it.title)
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = it.icon),
                                                    contentDescription = null
                                                )
                                            },
                                            onClick = {
                                                tagVisibility = it
                                                tagVisibilitySelectionVisible = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
            AnimatedVisibility(tagType != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isCreationMode) {
                        Box { }
                    } else {
                        OutlinedButtonWithIconAndText(
                            stringId = R.string.delete,
                            drawableId = R.drawable.ic_delete,
                            isLoading = deleting,
                            onClick = {
                                deleting = true
                                tagViewModel.deleteTag(
                                    statusId,
                                    tagData!!,
                                    { onSaveSucceeded() },
                                    { }
                                )
                            }
                        )
                    }
                    ButtonWithIconAndText(
                        stringId = R.string.save,
                        drawableId = R.drawable.ic_check_in,
                        onClick = {
                            saving = true
                            val tag = Tag(
                                key = tagType ?: TagType.UNKNOWN,
                                value = tagValue ?: "",
                                visibility = tagVisibility
                            )
                            if (isCreationMode) {
                                tagViewModel.createTag(
                                    statusId = statusId,
                                    tag = tag,
                                    successfulCallback = { onSaveSucceeded() },
                                    failureCallback = { }
                                )
                            } else {
                                tagViewModel.updateTag(
                                    statusId = statusId,
                                    tag = tag,
                                    successfulCallback = { onSaveSucceeded() },
                                    failureCallback = { }
                                )
                            }
                        },
                        isLoading = saving,
                        isEnabled = (
                                type != null &&
                                        value?.isNotEmpty() == true &&
                                        !deleting
                                )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun TagFormPreview() {
    val tagViewModel = TagViewModel()
    MainTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val modifier = Modifier.fillMaxWidth()
            TagForm(
                tagData = null,
                statusId = 42,
                tagViewModel = tagViewModel,
                alreadyAddedTags = listOf(),
                modifier = modifier
            )
            TagForm(
                tagData = Tag(
                    key = TagType.TICKET,
                    value = "D-Ticket",
                    visibility = StatusVisibility.PRIVATE
                ),
                statusId = 42,
                tagViewModel = tagViewModel,
                alreadyAddedTags = listOf(),
                modifier = modifier
            )
        }
    }
}
