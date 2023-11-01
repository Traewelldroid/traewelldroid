package de.hbch.traewelling.util

import android.content.Context
import de.hbch.traewelling.logging.ILogger

class ReviewRequest : IReviewRequest {
    override fun request(context: Context, logger: ILogger, onContinue: () -> Unit) {
        onContinue()
    }
}
