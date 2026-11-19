package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "future_forge_session")

class SessionPreferences(private val context: Context) {
    private val activeUserIdKey = stringPreferencesKey("active_user_id")

    val activeUserIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[activeUserIdKey]
    }

    suspend fun setActiveUserId(userId: String?) {
        context.dataStore.edit { prefs ->
            if (userId == null) {
                prefs.remove(activeUserIdKey)
            } else {
                prefs[activeUserIdKey] = userId
            }
        }
    }
}
