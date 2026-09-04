package com.imagetovideo.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[KEY_TOKEN] = token }
    }

    fun getToken(): Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    fun getTokenSync(): String? = runBlocking {
        getToken().first()
    }

    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { it[KEY_USER_EMAIL] = email }
    }

    fun getUserEmail(): Flow<String?> = context.dataStore.data.map { it[KEY_USER_EMAIL] ?: "" }

    fun getUserEmailSync(): String? = runBlocking {
        getUserEmail().first()
    }

    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { it[KEY_USER_ROLE] = role }
    }

    fun getUserRole(): Flow<String> = context.dataStore.data.map { it[KEY_USER_ROLE] ?: "guest" }

    fun getUserRoleSync(): String = runBlocking {
        getUserRole().first()
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
