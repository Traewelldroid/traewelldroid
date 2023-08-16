package de.hbch.traewelling.ui.composables

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T> FilterChipGroup(
    modifier: Modifier = Modifier,
    chips: Map<T, String>,
    preSelection: T?,
    onSelectionChanged: (T?) -> Unit = { },
    selectionRequired: Boolean = true
) {
    var selection by rememberSaveable { mutableStateOf(preSelection) }
    FlowRow(
        modifier = modifier
    ) {
        chips.forEach { (key, label) ->
            FilterChip(
                modifier = Modifier.padding(end = 4.dp),
                selected = selection == key,
                label = {
                    Text(
                        text = label
                    )
                },
                leadingIcon = {
                    if (selection == key) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null
                        )
                    }
                },
                onClick = {
                    val newSelection = if (selectionRequired || selection != key) key else null
                    selection = newSelection
                    onSelectionChanged(newSelection)
                }
            )
        }
    }
}
