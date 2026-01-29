package com.example.hipocrates.data

import com.example.hipocrates.model.dto.DoctorsApiResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * REST API interface for fetching doctors from GitHub
 */
interface GithubDoctorsAPI {

    /**
     * Get all doctors and specialties from GitHub JSON file
     * @return Response with doctors and specialties data
     */
    @GET("ro-mancilla/HipocratesPlus/refs/heads/master/doctors.json")
    suspend fun getDoctorsFromGithub(): Response<DoctorsApiResponse>
}
