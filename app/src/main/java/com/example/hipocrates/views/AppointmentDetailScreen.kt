package com.example.hipocrates.views

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hipocrates.model.AppointmentStatus
import com.example.hipocrates.viewmodel.AppViewModel
import com.example.hipocrates.views.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    viewModel: AppViewModel,
    appointmentId: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val appointment = remember(uiState.appointments, appointmentId) {
        uiState.appointments.find { it.id == appointmentId }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    // Manejar mensajes
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    if (appointment == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle de Cita") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { paddingValues ->
            EmptyStateView(
                icon = Icons.Filled.ErrorOutline,
                message = "Cita no encontrada",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Cita") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
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
            // Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado de la cita",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(status = appointment.estado)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Especialidad
            DetailCard(
                icon = Icons.Filled.MedicalServices,
                title = "Especialidad",
                content = appointment.especialidad.displayName
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Médico
            DetailCard(
                icon = Icons.Filled.Person,
                title = "Médico",
                content = appointment.doctorNombre
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fecha y hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailCard(
                    icon = Icons.Filled.CalendarToday,
                    title = "Fecha",
                    content = try {
                        LocalDate.parse(appointment.fecha)
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    } catch (e: Exception) {
                        appointment.fecha
                    },
                    modifier = Modifier.weight(1f)
                )

                DetailCard(
                    icon = Icons.Filled.AccessTime,
                    title = "Hora",
                    content = appointment.hora,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Motivo
            DetailCard(
                icon = Icons.Filled.Description,
                title = "Motivo de consulta",
                content = appointment.motivo
            )

            if (appointment.notas.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                // Notas
                DetailCard(
                    icon = Icons.Filled.Note,
                    title = "Notas adicionales",
                    content = appointment.notas
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Acciones según el estado
            when (appointment.estado) {
                AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED -> {
                    Button(
                        onClick = { showStatusDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (appointment.estado == AppointmentStatus.PENDING) {
                                "Confirmar Cita"
                            } else {
                                "Cambiar Estado"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancelar Cita")
                    }
                }
                AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED -> {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar Cita")
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información de fechas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Información del sistema",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ID: ${appointment.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Diálogo de cancelación
    if (showCancelDialog) {
        ConfirmationDialog(
            title = "Cancelar Cita",
            message = "¿Estás seguro que deseas cancelar esta cita? Esta acción no se puede deshacer.",
            confirmText = "Sí, cancelar",
            dismissText = "No, mantener",
            onConfirm = {
                viewModel.cancelAppointment(appointmentId)
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false }
        )
    }

    // Diálogo de eliminación
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Eliminar Cita",
            message = "¿Estás seguro que deseas eliminar esta cita del historial? Esta acción no se puede deshacer.",
            confirmText = "Sí, eliminar",
            dismissText = "Cancelar",
            onConfirm = {
                viewModel.deleteAppointment(appointmentId)
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Diálogo de cambio de estado
    if (showStatusDialog) {
        StatusChangeDialog(
            currentStatus = appointment.estado,
            onStatusSelected = { newStatus ->
                viewModel.updateAppointmentStatus(appointmentId, newStatus)
                showStatusDialog = false
            },
            onDismiss = { showStatusDialog = false }
        )
    }
}

@Composable
fun DetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatusChangeDialog(
    currentStatus: AppointmentStatus,
    onStatusSelected: (AppointmentStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val availableStatuses = listOf(
        AppointmentStatus.PENDING,
        AppointmentStatus.CONFIRMED,
        AppointmentStatus.COMPLETED,
        AppointmentStatus.CANCELLED,
        AppointmentStatus.RESCHEDULED
    ).filter { it != currentStatus }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Estado") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Selecciona el nuevo estado de la cita:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                availableStatuses.forEach { status ->
                    OutlinedButton(
                        onClick = { onStatusSelected(status) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatusChip(status = status)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
