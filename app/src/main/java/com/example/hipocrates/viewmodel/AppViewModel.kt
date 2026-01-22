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
    val rut: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nombreError: String? = null,
    val telefonoError: String? = null,
    val rutError: String? = null
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

        val emailError = validarEmail(form.email)
        val passwordError = validarPassword(form.password)

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
                            successMessage = "Iniciada la sesión."
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

    fun updateRegisterrut(rut: String) {
        _registerForm.update { it.copy(rut = rut, rutError = null) }
    }

    fun register(onSuccess: () -> Unit) {
        val form = _registerForm.value

        // Validar formulario
        val emailError = validarEmail(form.email)
        val passwordError = validarPassword(form.password)
        val confirmPasswordError = if (form.password != form.confirmPassword) {
            "Las contraseñas no coinciden"
        } else null
        val nombreError = if (form.nombre.isBlank()) "Ingrese su nombre completo" else null
        val telefonoError = validarTelefono(form.telefono)
        val rutError = validarRUT(form.rut)

        if (emailError != null || passwordError != null || confirmPasswordError != null ||
            nombreError != null || telefonoError != null || rutError != null) {
            _registerForm.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    nombreError = nombreError,
                    telefonoError = telefonoError,
                    rutError = rutError
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
                rut = form.rut
            )

            users.add(newUser)
            dataStoreManager.saveUsers(users)

            _uiState.update {
                it.copy(
                    isLoading = false,
                )
            }
            _registerForm.value = RegisterFormState()
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

        val fechaError = validarFecha(form.fecha)
        val horaError = validarHora(form.hora)
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

    fun cancelarAppointment(appointmentId: String) {
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


    private fun validarEmail(email: String): String? {
        return when {
            email.isBlank() -> "Ingrese un email"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ingrese un email válido"
            else -> null
        }
    }

    private fun validarPassword(password: String): String? {
        return when {
            password.isBlank() -> "Ingrese una contraseña"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }

    private fun validarTelefono(telefono: String): String? {
        if (telefono.isBlank()) {
            return "Ingrese un número de teléfono válido"
        }

        // Remover espacios y caracteres especiales
        val cleanTelefono = telefono.replace(" ", "").replace("-", "").replace("+56", "").trim()

        // Verificar que tenga exactamente 9 dígitos
        if (cleanTelefono.length != 9) {
            return "Ingrese un número de teléfono válido"
        }

        // Verificar que solo contenga dígitos
        if (!cleanTelefono.all { it.isDigit() }) {
            return "Ingrese un número de teléfono válido"
        }

        // Verificar que comience con 9
        if (!cleanTelefono.startsWith("9")) {
            return "Ingrese un número de teléfono válido"
        }

        return null
    }

    private fun validarRUT(rut: String): String? {
        if (rut.isBlank()) {
            return "Ingrese su RUT"
        }

        // Remover puntos y guiones si los hay
        val cleanRut = rut.replace(".", "").replace("-", "").trim()

        // Verificar longitud mínima (7 dígitos + 1 dígito verificador)
        if (cleanRut.length < 8) {
            return "Ingrese un válido"
        }

        // Separar número y dígito verificador
        val rutNumber = cleanRut.dropLast(1)
        val verifierDigit = cleanRut.last().uppercaseChar()

        // Verificar que la parte numérica contenga solo dígitos
        if (!rutNumber.all { it.isDigit() }) {
            return "Ingrese un RUT válido"
        }

        // Calcular dígito verificador
        var sum = 0
        var multiplier = 2
        
        for (i in rutNumber.length - 1 downTo 0) {
            sum += rutNumber[i].digitToInt() * multiplier
            multiplier = if (multiplier == 7) 2 else multiplier + 1
        }

        val remainder = sum % 11
        val calculatedVerifier = when (11 - remainder) {
            11 -> '0'
            10 -> 'K'
            else -> (11 - remainder).toString()[0]
        }

        if (verifierDigit != calculatedVerifier) {
            return "Ingrese un RUT válido"
        }

        return null
    }

    private fun validarFecha(fecha: String): String? {
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

    private fun validarHora(hora: String): String? {
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
