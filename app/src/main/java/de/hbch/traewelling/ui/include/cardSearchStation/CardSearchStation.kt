package de.hbch.traewelling.ui.include.cardSearchStation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSearchStation(
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            var text by remember { mutableStateOf("") }
            Text(
                text = stringResource(id = R.string.where_are_you),
                style = AppTypography.headlineSmall
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = {
                    Text(
                        text = stringResource(id = R.string.input_stop)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            painterResource(id = R.drawable.ic_expand_more),
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButtonWithIconAndText(
                    stringId = R.string.locate,
                    drawableId = R.drawable.ic_locate,
                )
                ButtonWithIconAndText(
                    stringId = R.string.search,
                    drawableId = R.drawable.ic_search,
                    modifier = Modifier
                        .padding(start = 8.dp)
                )
            }
        }
    }
}

@Preview(locale="de")
@Composable
private fun CardSearchStationPreview() {
    MainTheme {
        CardSearchStation()
    }
}
