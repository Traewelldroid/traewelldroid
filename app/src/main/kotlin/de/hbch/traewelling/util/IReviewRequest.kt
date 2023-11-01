package de.hbch.traewelling.util

import android.content.Context
import de.hbch.traewelling.logging.ILogger

interface IReviewRequest {
    fun request(
        context: Context,
        logger: ILogger,
        onContinue: () -> Unit
    )
}
