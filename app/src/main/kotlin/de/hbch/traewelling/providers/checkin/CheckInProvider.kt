package de.hbch.traewelling.providers.checkin

import androidx.compose.ui.graphics.Color
import de.hbch.traewelling.R
import de.hbch.traewelling.api.interceptors.LogInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

abstract class CheckInProvider<ResponseType> {
    protected abstract val client: OkHttpClient
    protected abstract val retrofit: Retrofit

    abstract suspend fun checkIn(request: CheckInRequest): CheckInResponse<ResponseType>
    abstract suspend fun update(request: CheckInUpdateRequest): CheckInResponse<ResponseType>

    protected val httpClientBuilder get() = OkHttpClient
        .Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(LogInterceptor())
}

abstract class CheckInRequest
abstract class CheckInUpdateRequest
class CheckInResponse<DataType>(
    val data: DataType?,
    val result: CheckInResult
)

enum class CheckInResult {
    SUCCESSFUL {
        override fun getIcon() = R.drawable.ic_check_in
        override fun getString() = R.string.check_in_successful
        override fun getColor() = Color.Green
    },
    CONFLICTED {
        override fun getIcon() = R.drawable.ic_error
        override fun getString() = R.string.check_in_conflict
        override fun getColor() = Color.Red
    },
    ERROR {
        override fun getIcon() = R.drawable.ic_error
        override fun getString() = R.string.check_in_failure
        override fun getColor() = Color.Red
    };

    abstract fun getIcon(): Int
    abstract fun getString(): Int
    abstract fun getColor(): Color
}
