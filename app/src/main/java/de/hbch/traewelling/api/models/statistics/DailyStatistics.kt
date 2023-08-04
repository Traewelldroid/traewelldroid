package de.hbch.traewelling.api.models.statistics

import de.hbch.traewelling.api.models.status.Status

data class DailyStatistics(
    val statuses: List<Status>,
    val count: Int,
    val distance: Int,
    val duration: Int,
    val points: Int
)
