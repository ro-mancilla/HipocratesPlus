package com.example.hipocrates.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hipocrates.data.DataStoreManager
import com.example.hipocrates.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class UiState(
    val isLoading: Boolean = false,
    val currentUserEmail: String? = null,
    val currentUser: Usuario? = null,
    val appointments: List<Appointment> = emptyList(),
    val filteredAppointments: List<Appointment> = emptyList(),
    val selectedAppointment: Appointment? = null,
    val error: String? = null,
    val successMessage: String? = null
)

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null
)

data class RegisterFormState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val identificacion: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nombreError: String? = null,
    val telefonoError: String? = null,
    val identificacionError: String? = null
)

data class AppointmentFormState(
    val fecha: String = "",
    val hora: String = "",
    val especialidad: MedicalSpecialty? = null,
    val doctorId: String = "",
    val motivo: String = "",
    val notas: String = "",
    val fechaError: String? = null,
    val horaError: String? = null,
    val especialidadError: String? = null,
    val doctorError: String? = null,
    val motivoError: String? = null
)

class AppViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _loginForm = MutableStateFlow(LoginFormState())
    val loginForm: StateFlow<LoginFormState> = _loginForm.asStateFlow()

    private val _registerForm = MutableStateFlow(RegisterFormState())
    val registerForm: StateFlow<RegisterFormState> = _registerForm.asStateFlow()

    private val _appointmentForm = MutableStateFlow(AppointmentFormState())
    val appointmentForm: StateFlow<AppointmentFormState> = _appointmentForm.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<AppointmentStatus?>(null)
    val statusFilter: StateFlow<AppointmentStatus?> = _statusFilter.asStateFlow()

    init {
        loadCurrentUser()
        observeAppointments()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            dataStoreManager.getCurrentUser().collect { email ->
                _uiState.update { it.copy(currentUserEmail = email) }
                if (email != null) {
                    loadUserProfile(email)
                }
            }
        }
    }

    private fun loadUserProfile(email: String) {
        viewModelScope.launch {
            dataStoreManager.getUsers().collect { users ->
                val user = users.find { it.email == email }
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    private fun observeAppointments() {
        viewModelScope.launch {
            combine(
                dataStoreManager.getAppointments(),
                _uiState.map { it.currentUserEmail },
                _searchQuery,
                _statusFilter
            ) { appointments, userEmail, query, status ->
                val userAppointments = if (userEmail != null) {
                    appointments.filter { it.userEmail == userEmail }
                } else {
                    emptyList()
                }
                filterAppointments(userAppointments, query, status)
            }.collect { filtered ->
                _uiState.update {
                    it.copy(
                        appointments = filtered,
                        filteredAppointments = filtered
                    )
                }
            }
        }
    }

    private fun filterAppointments(
        appointments: List<Appointment>,
        query: String,
        status: AppointmentStatus?
    ): List<Appointment> {
        var filtered = appointments

        if (status != null) {
            filtered = filtered.filter { it.estado == status }
        }

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.doctorNombre.contains(query, ignoreCase = true) ||
                it.especialidad.displayName.contains(query, ignoreCase = true) ||
                it.motivo.contains(query, ignoreCase = true)
            }
        }

        return filtered.sortedByDescending { it.fecha + it.hora }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: AppointmentStatus?) {
        _statusFilter.value = status
    }


    fun updateLoginEmail(email: String) {
        _loginForm.update { it.copy(email = email, emailError = null) }
    }

    fun updateLoginPassword(password: String) {
        _loginForm.update { it.copy(password = password, passwordError = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val form = _loginForm.value

        val emailError = validateEmail(form.email)
        val passwordError = validatePassword(form.password)

        if (emailError != null || passwordError != null) {
            _loginForm.update {
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            dataStoreManager.getUsers().first().let { users ->
                val user = users.find {
                    it.email == form.email && it.password == form.password
                }

                if (user != null) {
                    dataStoreManager.saveCurrentUser(user.email)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUserEmail = user.email,
                            currentUser = user,
                            successMessage = "Bienvenido/a, ${user.nombre.ifEmpty { user.email }}"
                        )
                    }
                    _loginForm.value = LoginFormState() // Limpiar formulario
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Email o contraseña incorrectos"
                        )
                    }
                }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            dataStoreManager.saveCurrentUser(null)
            _uiState.value = UiState()
            _loginForm.value = LoginFormState()
            onSuccess()
        }
    }

    fun updateRegisterEmail(email: String) {
        _registerForm.update { it.copy(email = email, emailError = null) }
    }

    fun updateRegisterPassword(password: String) {
        _registerForm.update { it.copy(password = password, passwordError = null) }
    }

    fun updateRegisterConfirmPassword(confirmPassword: String) {
        _registerForm.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun updateRegisterNombre(nombre: String) {
        _registerForm.update { it.copy(nombre = nombre, nombreError = null) }
    }

    fun updateRegisterTelefono(telefono: String) {
        _registerForm.update { it.copy(telefono = telefono, telefonoError = null) }
    }

    fun updateRegisterIdentificacion(identificacion: String) {
        _registerForm.update { it.copy(identificacion = identificacion, identificacionError = null) }
    }

    fun register(onSuccess: () -> Unit) {
        val form = _registerForm.value

        // Validar formulario
        val emailError = validateEmail(form.email)
        val passwordError = validatePassword(form.password)
        val confirmPasswordError = if (form.password != form.confirmPassword) {
            "Las contraseñas no coinciden"
        } else null
        val nombreError = if (form.nombre.isBlank()) "Ingrese su nombre completo" else null
        val telefonoError = if (form.telefono.isBlank()) "Ingrese su número de teléfono" else null
        val identificacionError = if (form.identificacion.isBlank()) "Ingrese su RUT" else null

        if (emailError != null || passwordError != null || confirmPasswordError != null ||
            nombreError != null || telefonoError != null || identificacionError != null) {
            _registerForm.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    nombreError = nombreError,
                    telefonoError = telefonoError,
                    identificacionError = identificacionError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val users = dataStoreManager.getUsers().first().toMutableList()

            // Verificar si el email ya existe
            if (users.any { it.email == form.email }) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "El email ya está registrado"
                    )
                }
                return@launch
            }

            // Crear nuevo usuario
            val newUser = Usuario(
                email = form.email,
                password = form.password,
                nombre = form.nombre,
                telefono = form.telefono,
                identificacion = form.identificacion
            )

            users.add(newUser)
            dataStoreManager.saveUsers(users)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    successMessage = "Registro exitoso. Ahora puedes iniciar sesión"
                )
            }
            _registerForm.value = RegisterFormState() // Limpiar formulario
            onSuccess()
        }
    }

    fun updateAppointmentFecha(fecha: String) {
        _appointmentForm.update { it.copy(fecha = fecha, fechaError = null) }
    }

    fun updateAppointmentHora(hora: String) {
        _appointmentForm.update { it.copy(hora = hora, horaError = null) }
    }

    fun updateAppointmentEspecialidad(especialidad: MedicalSpecialty?) {
        _appointmentForm.update { it.copy(especialidad = especialidad, especialidadError = null, doctorId = "") }
    }

    fun updateAppointmentDoctor(doctorId: String) {
        _appointmentForm.update { it.copy(doctorId = doctorId, doctorError = null) }
    }

    fun updateAppointmentMotivo(motivo: String) {
        _appointmentForm.update { it.copy(motivo = motivo, motivoError = null) }
    }

    fun updateAppointmentNotas(notas: String) {
        _appointmentForm.update { it.copy(notas = notas) }
    }

    fun createAppointment(onSuccess: () -> Unit) {
        val form = _appointmentForm.value
        val userEmail = _uiState.value.currentUserEmail

        if (userEmail == null) {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }

        val fechaError = validateFecha(form.fecha)
        val horaError = validateHora(form.hora)
        val especialidadError = if (form.especialidad == null) "Seleccione una especialidad" else null
        val doctorError = if (form.doctorId.isBlank()) "Seleccione un médico" else null
        val motivoError = if (form.motivo.isBlank()) "El motivo es requerido" else null

        if (fechaError != null || horaError != null || especialidadError != null ||
            doctorError != null || motivoError != null) {
            _appointmentForm.update {
                it.copy(
                    fechaError = fechaError,
                    horaError = horaError,
                    especialidadError = especialidadError,
                    doctorError = doctorError,
                    motivoError = motivoError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val doctor = DoctorsRepository.getDoctorById(form.doctorId)

            if (doctor == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Médico no encontrado"
                    )
                }
                return@launch
            }

            val newAppointment = Appointment(
                id = UUID.randomUUID().toString(),
                userEmail = userEmail,
                fecha = form.fecha,
                hora = form.hora,
                especialidad = form.especialidad!!,
                doctorId = form.doctorId,
                doctorNombre = doctor.nombre,
                motivo = form.motivo,
                notas = form.notas,
                estado = AppointmentStatus.PENDING
            )

            dataStoreManager.addAppointment(newAppointment)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    successMessage = "Cita agendada exitosamente."
                )
            }
            _appointmentForm.value = AppointmentFormState() // Limpiar formulario
            onSuccess()
        }
    }

    fun selectAppointment(appointment: Appointment) {
        _uiState.update { it.copy(selectedAppointment = appointment) }
    }

    fun updateAppointmentStatus(appointmentId: String, newStatus: AppointmentStatus) {
        viewModelScope.launch {
            val appointment = _uiState.value.appointments.find { it.id == appointmentId }
            if (appointment != null) {
                val updated = appointment.copy(
                    estado = newStatus,
                    fechaModificacion = System.currentTimeMillis()
                )
                dataStoreManager.updateAppointment(updated)
                _uiState.update {
                    it.copy(successMessage = "Cita actualizada exitosamente.")
                }
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        updateAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED)
    }

    fun deleteAppointment(appointmentId: String) {
        viewModelScope.launch {
            dataStoreManager.deleteAppointment(appointmentId)
            _uiState.update {
                it.copy(successMessage = "Cita eliminada exitosamente.")
            }
        }
    }


    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Ingrese un email"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email inválido"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Ingrese una contraseña"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }

    private fun validateFecha(fecha: String): String? {
        return try {
            if (fecha.isBlank()) {
                "Ingrese una fecha"
            } else {
                val date = LocalDate.parse(fecha)
                if (date.isBefore(LocalDate.now())) {
                    "La fecha no puede ser anterior a hoy"
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            "Fecha inválida"
        }
    }

    private fun validateHora(hora: String): String? {
        return try {
            if (hora.isBlank()) {
                "Ingrese una hora"
            } else {
                LocalTime.parse(hora)
                val time = LocalTime.parse(hora)
                if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(18, 0))) {
                    "El horario de atención es de 8:00 a 18:00"
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            "Hora inválida"
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun getDoctorsBySpecialty(specialty: MedicalSpecialty?): List<Doctor> {
        return if (specialty != null) {
            DoctorsRepository.getDoctorsBySpecialty(specialty)
        } else {
            emptyList()
        }
    }

    fun getUpcomingAppointments(): List<Appointment> {
        val now = LocalDate.now()
        return _uiState.value.appointments.filter { appointment ->
            try {
                val appointmentDate = LocalDate.parse(appointment.fecha)
                !appointmentDate.isBefore(now) &&
                (appointment.estado == AppointmentStatus.PENDING ||
                 appointment.estado == AppointmentStatus.CONFIRMED)
            } catch (e: Exception) {
                false
            }
        }.sortedBy { it.fecha + it.hora }
    }
}
