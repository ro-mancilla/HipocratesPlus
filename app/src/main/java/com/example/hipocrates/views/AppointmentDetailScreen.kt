package com.example.hipocrates.views

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.hipocrates.model.Appointment
import com.example.hipocrates.model.AppointmentStatus
import com.example.hipocrates.utils.PdfGenerator
import com.example.hipocrates.viewmodel.AppViewModel
import com.example.hipocrates.views.components.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    viewModel: AppViewModel,
    appointmentId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
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
                    title = { Text("Información de cita") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                title = { Text("Información de cita") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        shareAppointmentAsPdf(context, appointment, uiState.currentUser?.nombre ?: "Paciente", snackbarHostState)
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Compartir")
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
                    text = "Estado",
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

            DetailCard(
                icon = Icons.Filled.Description,
                title = "Motivo de consulta",
                content = appointment.motivo
            )

            if (appointment.notas.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                DetailCard(
                    icon = Icons.Filled.Note,
                    title = "Notas adicionales",
                    content = appointment.notas
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

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
                                "Confirmar cita"
                            } else {
                                "Cambiar estado"
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
                        Text("Cancelar cita")
                    }
                }
                AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED, AppointmentStatus.RESCHEDULED -> {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar cita")
                    }
                }
                else -> {}
            }
        }
    }

    if (showCancelDialog) {
        ConfirmationDialog(
            title = "Cancelar cita",
            message = "¿Está seguro que desea cancelar su cita? Esta acción es irreversible.",
            confirmText = "Sí",
            dismissText = "No",
            onConfirm = {
                viewModel.cancelarAppointment(appointmentId)
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Eliminar cita",
            message = "¿Está seguro que desea eliminar su cita del historial? Esta acción es irreversible.",
            confirmText = "Sí",
            dismissText = "No",
            onConfirm = {
                viewModel.deleteAppointment(appointmentId)
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

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
        title = { Text("Cambiar estado") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Seleccione el nuevo estado de su cita:",
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

private fun shareAppointmentAsPdf(
    context: Context,
    appointment: Appointment,
    userName: String,
    snackbarHostState: SnackbarHostState
) {
    try {
        // Generate PDF
        val pdfFile = PdfGenerator.generateAppointmentPdf(context, appointment, userName)

        if (pdfFile == null) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                snackbarHostState.showSnackbar("Error al generar el PDF")
            }
            return
        }

        // Create content URI using FileProvider
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        // Create share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Cita Médica - ${appointment.especialidad.displayName}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Información de cita médica de ${appointment.especialidad.displayName} con ${appointment.doctorNombre}"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Start share sheet
        context.startActivity(Intent.createChooser(shareIntent, "Compartir cita médica"))

    } catch (e: Exception) {
        e.printStackTrace()
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            snackbarHostState.showSnackbar("Error al compartir: ${e.message}")
        }
    }
}

