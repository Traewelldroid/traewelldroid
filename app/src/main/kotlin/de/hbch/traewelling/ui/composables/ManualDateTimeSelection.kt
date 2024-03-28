package de.hbch.traewelling.ui.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.util.getLocalDateTimeString
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeSelection(
    initDate: ZonedDateTime?,
    plannedDate: ZonedDateTime?,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    dateSelected: (ZonedDateTime?) -> Unit = { }
) {
    val initDateTime = initDate ?: plannedDate ?: ZonedDateTime.now()

    var dateTime by remember { mutableStateOf(initDate) }
    val dateTimeText = dateTime?.let { getLocalDateTimeString(it) } ?: ""

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val textFieldPressed by interactionSource.collectIsPressedAsState()

    var datePickerVisible by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initDateTime.toInstant().toEpochMilli()
    )
    var timePickerVisible by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = initDateTime.hour,
        initialMinute = initDateTime.minute
    )

    if (datePickerVisible) {
        DatePickerDialog(
            onDismissRequest = { datePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerVisible = false
                        timePickerVisible = true
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text(
                        text = stringResource(id = R.string.ok)
                    )
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (timePickerVisible) {
        DatePickerDialog(
            onDismissRequest = { timePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val zonedDateTime = ZonedDateTime
                            .ofInstant(
                                Instant.ofEpochMilli(datePickerState.selectedDateMillis!!),
                                ZoneId.systemDefault()
                            )
                            .withHour(timePickerState.hour)
                            .withMinute(timePickerState.minute)

                        dateTime = zonedDateTime
                        dateSelected(zonedDateTime)

                        timePickerVisible = false
                        focusManager.clearFocus(true)
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.ok)
                    )
                }
            }
        ) {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    if (textFieldPressed) {
        datePickerVisible = true
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        OutlinedTextField(
            value = dateTimeText,
            onValueChange = { },
            modifier = modifier.clickable(interactionSource, null) { },
            readOnly = true,
            label = {
                Text(
                    text = stringResource(id = label),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_time),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (dateTime != null) {
                    IconButton(onClick = {
                        dateTime = null
                        dateSelected(null)
                        focusManager.clearFocus(true)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = null
                        )
                    }
                }
            },
            maxLines = 1,
            interactionSource = interactionSource
        )
        Text(
            text = stringResource(id = R.string.planned, getLocalDateTimeString(plannedDate!!)),
            style = AppTypography.bodySmall
        )
    }
}

@Preview
@Composable
private fun DateTimeSelectionPreview() {
    MainTheme {
        DateTimeSelection(
            initDate = null,
            plannedDate = ZonedDateTime.now(),
            label = R.string.manual_departure
        )
    }
}
