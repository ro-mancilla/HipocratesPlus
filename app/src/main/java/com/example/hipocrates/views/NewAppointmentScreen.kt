package com.example.hipocrates.views

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hipocrates.model.Doctor
import com.example.hipocrates.model.MedicalSpecialty
import com.example.hipocrates.viewmodel.AppViewModel
import com.example.hipocrates.views.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    onAppointmentCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val appointmentForm by viewModel.appointmentForm.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val availableDoctors = remember(appointmentForm.especialidad) {
        viewModel.getDoctorsBySpecialty(appointmentForm.especialidad)
    }

    // Manejar mensajes
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Icono
            Icon(
                imageVector = Icons.AutoMirrored.Filled.EventNote,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Nueva cita",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = "Complete los datos para agendar su cita:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Fecha
            DatePickerField(
                selectedDate = appointmentForm.fecha,
                onDateSelected = { viewModel.updateAppointmentFecha(it) },
                label = "Fecha",
                error = appointmentForm.fechaError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hora
            TimePickerField(
                selectedTime = appointmentForm.hora,
                onTimeSelected = { viewModel.updateAppointmentHora(it) },
                label = "Hora",
                error = appointmentForm.horaError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Especialidad
            SpecialtyDropdown(
                selectedSpecialty = appointmentForm.especialidad,
                onSpecialtySelected = { viewModel.updateAppointmentEspecialidad(it) },
                error = appointmentForm.especialidadError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Médico
            AnimatedVisibility(
                visible = appointmentForm.especialidad != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    DoctorDropdown(
                        doctors = availableDoctors,
                        selectedDoctorId = appointmentForm.doctorId,
                        onDoctorSelected = { viewModel.updateAppointmentDoctor(it) },
                        error = appointmentForm.doctorError
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Motivo
            ValidatedTextField(
                value = appointmentForm.motivo,
                onValueChange = { viewModel.updateAppointmentMotivo(it) },
                label = "Motivo de la consulta",
                error = appointmentForm.motivoError,
                leadingIcon = Icons.Filled.Description,
                maxLines = 3,
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notas adicionales (opcional)
            ValidatedTextField(
                value = appointmentForm.notas,
                onValueChange = { viewModel.updateAppointmentNotas(it) },
                label = "Notas adicionales (opcional)",
                leadingIcon = Icons.AutoMirrored.Filled.Note,
                maxLines = 4,
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )

            // Mensaje de error general
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Información de horario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Nuestros horarios de atención son:\nLunes a Viernes de 8:00 a 18:00.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.createAppointment(onAppointmentCreated) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Agendar cita",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialtyDropdown(
    selectedSpecialty: MedicalSpecialty?,
    onSpecialtySelected: (MedicalSpecialty) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedSpecialty?.displayName ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Especialidad") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = error != null,
                leadingIcon = {
                    Icon(Icons.Filled.MedicalServices, contentDescription = null)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                MedicalSpecialty.entries.forEach { specialty ->
                    DropdownMenuItem(
                        text = { Text(specialty.displayName) },
                        onClick = {
                            onSpecialtySelected(specialty)
                            expanded = false
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDropdown(
    doctors: List<Doctor>,
    selectedDoctorId: String,
    onDoctorSelected: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedDoctor = doctors.find { it.id == selectedDoctorId }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedDoctor?.nombre ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Médico") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = error != null,
                leadingIcon = {
                    Icon(Icons.Filled.Person, contentDescription = null)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                doctors.forEach { doctor ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(doctor.nombre)
                                Text(
                                    text = "Horario: ${doctor.horarioInicio} - ${doctor.horarioFin}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onDoctorSelected(doctor.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
