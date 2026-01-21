package com.example.hipocrates.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hipocrates.model.Appointment
import com.example.hipocrates.model.Usuario
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "hipocrates_preferences")

class DataStoreManager(private val context: Context) {
    private val gson = Gson()

    private val USER_KEY = stringPreferencesKey("usuarios")
    private val CURRENT_USER_KEY = stringPreferencesKey("current_user")
    private val APPOINTMENTS_KEY = stringPreferencesKey("appointments")


    suspend fun saveUsers(users: List<Usuario>) {
        val json = gson.toJson(users)
        context.dataStore.edit { prefs ->
            prefs[USER_KEY] = json
        }
    }

    fun getUsers(): Flow<List<Usuario>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[USER_KEY] ?: "[]"
            val type = object : TypeToken<List<Usuario>>() {}.type
            gson.fromJson(json, type)
        }
    }

    suspend fun saveCurrentUser(email: String?) {
        context.dataStore.edit { prefs ->
            if (email != null) {
                prefs[CURRENT_USER_KEY] = email
            } else {
                prefs.remove(CURRENT_USER_KEY)
            }
        }
    }

    fun getCurrentUser(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[CURRENT_USER_KEY]
        }
    }

    suspend fun saveAppointments(appointments: List<Appointment>) {
        val json = gson.toJson(appointments)
        context.dataStore.edit { prefs ->
            prefs[APPOINTMENTS_KEY] = json
        }
    }

    fun getAppointments(): Flow<List<Appointment>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[APPOINTMENTS_KEY] ?: "[]"
            val type = object : TypeToken<List<Appointment>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    suspend fun addAppointment(appointment: Appointment) {
        context.dataStore.edit { prefs ->
            val json = prefs[APPOINTMENTS_KEY] ?: "[]"
            val type = object : TypeToken<List<Appointment>>() {}.type
            val appointments: MutableList<Appointment> = gson.fromJson(json, type) ?: mutableListOf()
            appointments.add(appointment)
            prefs[APPOINTMENTS_KEY] = gson.toJson(appointments)
        }
    }

    suspend fun updateAppointment(updatedAppointment: Appointment) {
        context.dataStore.edit { prefs ->
            val json = prefs[APPOINTMENTS_KEY] ?: "[]"
            val type = object : TypeToken<List<Appointment>>() {}.type
            val appointments: MutableList<Appointment> = gson.fromJson(json, type) ?: mutableListOf()
            val index = appointments.indexOfFirst { it.id == updatedAppointment.id }
            if (index != -1) {
                appointments[index] = updatedAppointment
                prefs[APPOINTMENTS_KEY] = gson.toJson(appointments)
            }
        }
    }

    suspend fun eliminarAppointment(appointmentId: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[APPOINTMENTS_KEY] ?: "[]"
            val type = object : TypeToken<List<Appointment>>() {}.type
            val appointments: MutableList<Appointment> = gson.fromJson(json, type) ?: mutableListOf()
            appointments.removeAll { it.id == appointmentId }
            prefs[APPOINTMENTS_KEY] = gson.toJson(appointments)
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}