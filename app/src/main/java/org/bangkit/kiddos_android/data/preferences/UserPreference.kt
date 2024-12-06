package org.bangkit.kiddos_android.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    private val TOKEN_KEY = stringPreferencesKey("user_token")
    private val NAME_KEY = stringPreferencesKey("user_name")
    private val EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    fun getToken(): Flow<String> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY] ?: ""
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            Log.d("UserPreferences", "Saving user info: $token")
        }
    }

    fun getName(): Flow<String> = dataStore.data.map { preferences ->
        preferences[NAME_KEY] ?: ""
    }

    fun getEmail(): Flow<String> = dataStore.data.map { preferences ->
        preferences[EMAIL_KEY] ?: ""
    }

    fun getUserId(): Flow<String> = dataStore.data.map { preferences -> // Added getUserId function
        preferences[USER_ID_KEY] ?: ""
    }

    suspend fun saveName(name: String) {
        dataStore.edit { preferences ->
            preferences[NAME_KEY] = name
            Log.d("UserPreferences", "Saving user info: Name - $name")
        }
    }

    suspend fun saveUserInfo(email: String, userId: String) {
        dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
            preferences[USER_ID_KEY] = userId
            Log.d("UserPreferences", "Saving user info: Email - $email, User ID - $userId")
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

        fun getInstance(context: Context): UserPreference {
            return INSTANCE ?: synchronized(this) {
                UserPreference(context.dataStore).also { INSTANCE = it }
            }
        }
    }
}
