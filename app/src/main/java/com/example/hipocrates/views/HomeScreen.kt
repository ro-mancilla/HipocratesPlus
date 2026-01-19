package com.example.hipocrates.views

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hipocrates.viewmodel.AppViewModel
import com.example.hipocrates.views.components.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToNewAppointment: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val upcomingAppointments = remember(uiState.appointments) {
        viewModel.getUpcomingAppointments()
    }
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

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
                title = {
                    Column {
                        Text(
                            text = "Hipócrates",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bienvenido/a, ${uiState.currentUser?.nombre ?: "Usuario"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mi Perfil") },
                            onClick = {
                                showMenu = false
                                onNavigateToProfile()
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión") },
                            onClick = {
                                showMenu = false
                                showLogoutDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Logout, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewAppointment,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Nueva cita"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tarjetas de acceso rápido
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickAccessCard(
                    title = "Nueva Cita",
                    icon = Icons.Filled.Add,
                    onClick = onNavigateToNewAppointment,
                    modifier = Modifier.weight(1f)
                )
                QuickAccessCard(
                    title = "Historial",
                    icon = Icons.Filled.History,
                    onClick = onNavigateToHistory,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // Sección de próximas citas
            Text(
                text = "Próximas Citas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            AnimatedContent(
                targetState = upcomingAppointments.isEmpty(),
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                    fadeOut(animationSpec = tween(300))
                },
                label = "appointments_animation"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyStateView(
                        icon = Icons.Filled.EventNote,
                        message = "No tienes citas próximas.\n¡Agenda una nueva!",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = upcomingAppointments,
                            key = { it.id }
                        ) { appointment ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                AppointmentCard(
                                    appointment = appointment,
                                    onClick = { onNavigateToDetail(appointment.id) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de logout
    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Cerrar Sesión",
            message = "¿Estás seguro que deseas cerrar sesión?",
            confirmText = "Sí, cerrar sesión",
            dismissText = "Cancelar",
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout(onLogout)
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAccessCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
