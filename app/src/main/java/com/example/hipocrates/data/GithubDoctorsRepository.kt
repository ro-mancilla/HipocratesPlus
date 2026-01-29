package com.example.hipocrates.data

import com.example.hipocrates.model.Doctor
import com.example.hipocrates.model.MedicalSpecialty
import com.example.hipocrates.model.dto.toDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GithubDoctorsRepository {

    private val api: GithubDoctorsAPI
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(GithubDoctorsAPI::class.java)
    }

    suspend fun getAllDoctors(): Result<List<Doctor>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDoctorsFromGithub()

            if (response.isSuccessful && response.body() != null) {
                val doctors = response.body()!!.data.doctors.map { it.toDomainModel() }
                Result.success(doctors)
            } else {
                Result.failure(Exception("Error al cargar médicos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getDoctorsBySpecialty(specialty: MedicalSpecialty): Result<List<Doctor>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDoctorsFromGithub()

            if (response.isSuccessful && response.body() != null) {
                val doctors = response.body()!!.data.doctors
                    .filter { it.especialidad == specialty.name }
                    .map { it.toDomainModel() }
                Result.success(doctors)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    /**
     * Find a doctor by ID
     * @param id The doctor ID
     * @return Result with doctor or error
     */
    suspend fun getDoctorById(id: String): Result<Doctor?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDoctorsFromGithub()

            if (response.isSuccessful && response.body() != null) {
                val doctor = response.body()!!.data.doctors
                    .find { it.id == id }
                    ?.toDomainModel()
                Result.success(doctor)
            } else {
                Result.failure(Exception("Error al cargar médico: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: GithubDoctorsRepository? = null

        fun getInstance(): GithubDoctorsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GithubDoctorsRepository().also { INSTANCE = it }
            }
        }
    }
}
