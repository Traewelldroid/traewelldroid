package de.hbch.traewelling.util

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import de.hbch.traewelling.logging.ILogger

class ReviewRequest : IReviewRequest {
    override fun request(
        context: Context,
        logger: ILogger,
        onContinue: () -> Unit
    ) {
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(context as Activity, reviewInfo)
                flow.addOnCompleteListener {
                    onContinue()
                }
            } else {
                logger.captureException(task.exception as ReviewException)
            }
        }
    }
}
