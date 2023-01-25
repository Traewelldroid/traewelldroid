package de.hbch.traewelling.ui.include.status

import de.hbch.traewelling.api.models.status.Status

interface CheckInListViewModel {
    abstract fun loadCheckIns(
        page: Int,
        successCallback: (List<Status>) -> Unit,
        failureCallback: (Throwable) -> Unit
    )
}