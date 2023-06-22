package de.hbch.traewelling.ui.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.statistics.PersonalStatistics
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.FilterChipGroup
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText

@Composable
fun Statistics(
    modifier: Modifier = Modifier
) {
    var statistics by remember { mutableStateOf<PersonalStatistics?>(null) }

    Column(
        modifier = modifier
    ) {
        OutlinedButtonWithIconAndText(
            modifier = Modifier.fillMaxWidth(),
            text = "",
            drawableId = R.drawable.ic_calendar,
            onClick = {}
        )
        val unitMap = mutableMapOf<StatisticsUnit, String>()
        unitMap.putAll(
            StatisticsUnit.values().map {
                Pair(it, stringResource(id = it.getStringId()))
            }
        )
        val typeMap = mutableMapOf<StatisticsType, String>()
        typeMap.putAll(
            StatisticsType.values().map {
                Pair(it, stringResource(id = it.getStringId()))
            }
        )
        FilterChipGroup(chips = unitMap, preSelection = StatisticsUnit.CHECK_IN_COUNT)
        FilterChipGroup(chips = typeMap, preSelection = StatisticsType.TRANSPORT_TYPES)
    }
}

enum class StatisticsUnit {
    CHECK_IN_COUNT {
        override fun getStringId() = R.string.check_in_count
    },
    TRAVEL_TIME {
        override fun getStringId() = R.string.travel_time
    };

    abstract fun getStringId(): Int
}

enum class StatisticsType {
    TRANSPORT_TYPES {
        override fun getStringId() = R.string.transport_types
    },
    OPERATORS {
        override fun getStringId() = R.string.operators
    },
    TRAVEL_PURPOSE {
        override fun getStringId() = R.string.travel_purpose
    };

    abstract fun getStringId(): Int
}

@Preview
@Composable
private fun StatisticsPreview() {
    MainTheme {
        Statistics()
    }
}
