package com.example.attentrack.data

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeoutOrNull

class AuthRepository(
    private val api: ApiService,
    private val store: SessionStore,
    private val gson: Gson,
) {
    val session: Flow<Session?> = store.session

    suspend fun login(userId: String, password: String): ApiResult<Unit> {
        return when (val r = safeApi(gson) { api.login(LoginRequest(userId, password)) }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> {
                val data = r.data ?: return ApiResult.Failure("The server sent an unexpected response.")
                store.save(data)
                ApiResult.Success(Unit, r.message)
            }
        }
    }

    suspend fun changePassword(current: String, newPassword: String): ApiResult<Unit> {
        return when (val r = safeApi(gson) { api.changePassword(ChangePasswordRequest(current, newPassword)) }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> {
                store.setMustChangePassword(false)
                ApiResult.Success(Unit, r.message)
            }
        }
    }

    /** Server e logout call best-effort (max 3 second), tarpor shobshomoy local session muche felo. */
    suspend fun logout() {
        withTimeoutOrNull(3000) { safeApi(gson) { api.logout() } }
        store.clear()
    }

    /** Session shesh (401) hole server call chhara-i local clear. */
    suspend fun clearSession() {
        store.clear()
    }
}
