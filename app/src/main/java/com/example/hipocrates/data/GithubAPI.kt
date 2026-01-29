package com.example.hipocrates.data

import com.example.hipocrates.model.dto.DoctorsApiResponse
import retrofit2.Response
import retrofit2.http.GET

interface GithubAPI {

    @GET("ro-mancilla/HipocratesPlus/refs/heads/master/doctors.json")
    suspend fun getDoctorsFromGithub(): Response<DoctorsApiResponse>
}
