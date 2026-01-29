package com.example.hipocrates.model

data class Usuario(
    val email: String,
    val password: String,
    val nombre: String = "",
    val telefono: String = "",
    val rut: String = ""
)

enum class MedicalSpecialty(val displayName: String) {
    GENERAL("Medicina General"),
    CARDIOLOGY("Cardiología"),
    DERMATOLOGY("Dermatología"),
    PEDIATRICS("Pediatría"),
    GYNECOLOGY("Ginecología"),
    TRAUMATOLOGY("Traumatología"),
    OPHTHALMOLOGY("Oftalmología"),
    PSYCHIATRY("Psiquiatría"),
    DENTISTRY("Odontología"),
    NEUROLOGY("Neurología")
}

data class Doctor(
    val id: String,
    val nombre: String,
    val especialidad: MedicalSpecialty,
    val horarioInicio: String = "08:00",
    val horarioFin: String = "18:00"
)

enum class AppointmentStatus(val displayName: String, val color: String) {
    PENDING("Pendiente", "#FFCF44"),
    CONFIRMED("Confirmada", "#1EB980"),
    COMPLETED("Completada", "#72DEFF"),
    CANCELLED("Cancelada", "#FF6859"),
    RESCHEDULED("Reprogramada", "#B15DFF")
}

data class Appointment(
    val id: String,
    val userEmail: String,
    val fecha: String,
    val hora: String,
    val especialidad: MedicalSpecialty,
    val doctorId: String,
    val doctorNombre: String,
    val motivo: String,
    val estado: AppointmentStatus = AppointmentStatus.PENDING,
    val notas: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaModificacion: Long = System.currentTimeMillis()
)

object DoctorsRepository {
    val doctors = listOf(
        Doctor("1", "Dr. Juan Pérez", MedicalSpecialty.GENERAL),
        Doctor("2", "Dra. María González", MedicalSpecialty.GENERAL),
        Doctor("3", "Dr. Carlos Ramírez", MedicalSpecialty.CARDIOLOGY),
        Doctor("4", "Dra. Ana Martínez", MedicalSpecialty.CARDIOLOGY),
        Doctor("5", "Dr. Luis Fernández", MedicalSpecialty.DERMATOLOGY),
        Doctor("6", "Dra. Patricia López", MedicalSpecialty.DERMATOLOGY),
        Doctor("7", "Dr. Roberto Sánchez", MedicalSpecialty.PEDIATRICS),
        Doctor("8", "Dra. Carmen Torres", MedicalSpecialty.PEDIATRICS),
        Doctor("9", "Dra. Elena Ruiz", MedicalSpecialty.GYNECOLOGY),
        Doctor("10", "Dra. Isabel Moreno", MedicalSpecialty.GYNECOLOGY),
        Doctor("11", "Dr. Miguel Díaz", MedicalSpecialty.TRAUMATOLOGY),
        Doctor("12", "Dr. Francisco Romero", MedicalSpecialty.TRAUMATOLOGY),
        Doctor("13", "Dra. Laura Jiménez", MedicalSpecialty.OPHTHALMOLOGY),
        Doctor("14", "Dr. Javier Navarro", MedicalSpecialty.OPHTHALMOLOGY),
        Doctor("15", "Dr. Antonio Herrera", MedicalSpecialty.PSYCHIATRY),
        Doctor("16", "Dra. Rosa Domínguez", MedicalSpecialty.PSYCHIATRY),
        Doctor("17", "Dr. Pedro Gil", MedicalSpecialty.DENTISTRY),
        Doctor("18", "Dra. Marta Vega", MedicalSpecialty.DENTISTRY),
        Doctor("19", "Dr. Alberto Serrano", MedicalSpecialty.NEUROLOGY),
        Doctor("20", "Dra. Cristina Blanco", MedicalSpecialty.NEUROLOGY)
    )

    fun getDoctorsBySpecialty(specialty: MedicalSpecialty): List<Doctor> {
        return doctors.filter { it.especialidad == specialty }
    }

    fun getDoctorById(id: String): Doctor? {
        return doctors.find { it.id == id }
    }
}

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather
)

data class CurrentWeather(
    val temperature_2m: Double,
    val weathercode: Int
)

data class WeatherData(
    val temperature: Double,
    val weatherCode: Int,
    val weatherDescription: String
)

fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "Despejado"
        1, 2, 3 -> "Parcialmente nublado"
        45, 48 -> "Niebla"
        51, 53, 55 -> "Llovizna"
        61, 63, 65 -> "Lluvia"
        71, 73, 75 -> "Nieve"
        77 -> "Nieve granulada"
        80, 81, 82 -> "Chubascos"
        85, 86 -> "Chubascos de nieve"
        95 -> "Tormenta"
        96, 99 -> "Tormenta con granizo"
        else -> "Desconocido"
    }
}

