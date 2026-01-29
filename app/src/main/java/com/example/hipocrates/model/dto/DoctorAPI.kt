package com.example.hipocrates.model.dto

import com.example.hipocrates.model.Doctor
import com.example.hipocrates.model.MedicalSpecialty

data class DoctorsApiResponse(
    val success: Boolean,
    val timestamp: String,
    val data: DoctorsData,
    val meta: MetaInfo
)

data class DoctorsData(
    val doctors: List<DoctorApiDTO>,
    val specialties: List<SpecialtyApiDTO>
)

data class DoctorApiDTO(
    val id: String,
    val nombre: String,
    val especialidad: String,
    val especialidadDisplayName: String,
    val horarioInicio: String,
    val horarioFin: String
)

data class SpecialtyApiDTO(
    val name: String,
    val displayName: String,
    val doctorCount: Int
)

data class MetaInfo(
    val totalDoctors: Int,
    val totalSpecialties: Int,
    val version: String
)


fun DoctorApiDTO.toDomainModel(): Doctor {
    return Doctor(
        id = this.id,
        nombre = this.nombre,
        especialidad = MedicalSpecialty.valueOf(this.especialidad),
        horarioInicio = this.horarioInicio,
        horarioFin = this.horarioFin
    )
}
