package com.example.attentrack.data

import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T?, val message: String?) : ApiResult<T>
    data class Failure(val message: String, val code: Int? = null) : ApiResult<Nothing>
}

/**
 * API call ke ApiResult e convert kore. Server er {"success":false,"message":"..."} error
 * message ta Failure.message e thake, network error hole bujhano message dey.
 */
suspend fun <T> safeApi(
    gson: Gson,
    call: suspend () -> Response<ApiEnvelope<T>>,
): ApiResult<T> {
    return try {
        val res = call()
        val body = res.body()
        if (res.isSuccessful && body != null) {
            if (body.success) {
                ApiResult.Success(body.data, body.message)
            } else {
                ApiResult.Failure(body.message ?: "Something went wrong.", res.code())
            }
        } else {
            val raw = res.errorBody()?.string()
            val msg = try {
                gson.fromJson(raw, ApiEnvelope::class.java)?.message
            } catch (e: JsonParseException) {
                null
            }
            ApiResult.Failure(msg ?: "Server error (${res.code()}).", res.code())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        ApiResult.Failure("Cannot connect to the server. Check your internet connection and the server address.")
    } catch (e: JsonParseException) {
        ApiResult.Failure("The server sent an unexpected response.")
    }
}
