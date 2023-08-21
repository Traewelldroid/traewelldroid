package de.hbch.traewelling.ui.composables

import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RichTooltipBox
import androidx.compose.material3.RichTooltipState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.hbch.traewelling.api.models.status.Tag
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusTag(
    tag: Tag,
    modifier: Modifier = Modifier
) {
    val tooltipState = remember { RichTooltipState() }
    val scope = rememberCoroutineScope()

    RichTooltipBox(
        text = {
            Text(
                text = stringResource(id = tag.example)
            )
        },
        title = {
            Text(
                text = stringResource(id = tag.title)
            )
        },
        tooltipState = tooltipState
    ) {
        AssistChip(
            onClick = { scope.launch {
                tooltipState.show()
            } },
            label = {
                Text(
                    text = tag.value
                )
            },
            modifier = modifier.tooltipAnchor(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = tag.icon),
                    contentDescription = null
                )
            }
        )
    }
}
