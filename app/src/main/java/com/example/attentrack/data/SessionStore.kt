package com.example.attentrack.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

data class Session(
    val token: String,
    val userId: Long,
    val name: String,
    val role: String,
    val phone: String?,
    val dept: String?,
    val batch: String?,
    val section: String?,
    val mustChangePassword: Boolean,
)

/** Login token + user info phone e save kore rakhe (DataStore). */
class SessionStore(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USER_ID = longPreferencesKey("user_id")
        val NAME = stringPreferencesKey("name")
        val ROLE = stringPreferencesKey("role")
        val PHONE = stringPreferencesKey("phone")
        val DEPT = stringPreferencesKey("dept")
        val BATCH = stringPreferencesKey("batch")
        val SECTION = stringPreferencesKey("section")
        val MUST_CHANGE = booleanPreferencesKey("must_change_password")
    }

    val session: Flow<Session?> = context.sessionDataStore.data.map { p ->
        val token = p[Keys.TOKEN]
        if (token == null) {
            null
        } else {
            Session(
                token = token,
                userId = p[Keys.USER_ID] ?: 0L,
                name = p[Keys.NAME] ?: "",
                role = p[Keys.ROLE] ?: "",
                phone = p[Keys.PHONE],
                dept = p[Keys.DEPT],
                batch = p[Keys.BATCH],
                section = p[Keys.SECTION],
                mustChangePassword = p[Keys.MUST_CHANGE] ?: false,
            )
        }
    }

    suspend fun token(): String? = context.sessionDataStore.data.first()[Keys.TOKEN]

    suspend fun save(data: LoginData) {
        context.sessionDataStore.edit { p ->
            p[Keys.TOKEN] = data.token
            p[Keys.USER_ID] = data.user.id
            p[Keys.NAME] = data.user.name
            p[Keys.ROLE] = data.user.role
            p.putOrRemove(Keys.PHONE, data.user.phone)
            p.putOrRemove(Keys.DEPT, data.user.dept)
            p.putOrRemove(Keys.BATCH, data.user.batch)
            p.putOrRemove(Keys.SECTION, data.user.section)
            p[Keys.MUST_CHANGE] = data.mustChangePassword
        }
    }

    suspend fun setMustChangePassword(value: Boolean) {
        context.sessionDataStore.edit { it[Keys.MUST_CHANGE] = value }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }

    private fun MutablePreferences.putOrRemove(key: Preferences.Key<String>, value: String?) {
        if (value != null) this[key] = value else remove(key)
    }
}
