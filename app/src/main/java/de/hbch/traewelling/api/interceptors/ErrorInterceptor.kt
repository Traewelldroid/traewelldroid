package de.hbch.traewelling.api.interceptors

import de.hbch.traewelling.events.UnauthorizedEvent
import okhttp3.Interceptor
import okhttp3.Response
import org.greenrobot.eventbus.EventBus

class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401
            /*TODO
            * Following statement can be removed after https://github.com/Traewelling/traewelling/issues/1175
            * is resolved.
            * */
            || (response.code == 200 && response.request.url.toUrl().path.contains("login")))
            EventBus.getDefault().post(UnauthorizedEvent())

        return response
    }
}
