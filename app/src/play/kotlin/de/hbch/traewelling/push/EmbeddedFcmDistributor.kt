package de.hbch.traewelling.push

import android.content.Context
import de.hbch.traewelling.BuildConfig
import org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver

class EmbeddedFcmDistributor : EmbeddedDistributorReceiver() {
    override fun getEndpoint(context: Context, token: String, instance: String): String {
        return "${BuildConfig.UP_FCM_PROXY}?v2&instance=$instance&token=$token"
    }
}
