package com.example.hipocrates.views

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hipocrates.model.AppointmentStatus
import com.example.hipocrates.viewmodel.AppViewModel
import com.example.hipocrates.views.components.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AppointmentHistoryScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filtrar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.padding(16.dp)
            )

            // Filtro activo
            AnimatedVisibility(
                visible = statusFilter != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FilterChip(
                    selected = true,
                    onClick = { viewModel.setStatusFilter(null) },
                    label = { Text("${statusFilter?.displayName}") },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Quitar filtro",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de citas
            AnimatedContent(
                targetState = uiState.filteredAppointments.isEmpty(),
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                    fadeOut(animationSpec = tween(300))
                },
                label = "history_animation"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyStateView(
                        icon = Icons.Filled.SearchOff,
                        message = if (searchQuery.isNotBlank() || statusFilter != null) {
                            "No se encontraron citas"
                        } else {
                            "No tiene citas registradas"
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.filteredAppointments,
                            key = { it.id }
                        ) { appointment ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                AppointmentCard(
                                    appointment = appointment,
                                    onClick = { onNavigateToDetail(appointment.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            currentFilter = statusFilter,
            onFilterSelected = { filter ->
                viewModel.setStatusFilter(filter)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {  },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                }
            }
        },
        singleLine = true
    )
}

@Composable
fun FilterDialog(
    currentFilter: AppointmentStatus?,
    onFilterSelected: (AppointmentStatus?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterOption(
                    label = "Todas las citas",
                    isSelected = currentFilter == null,
                    onClick = { onFilterSelected(null) }
                )

                AppointmentStatus.entries.forEach { status ->
                    FilterOption(
                        label = status.displayName,
                        isSelected = currentFilter == status,
                        onClick = { onFilterSelected(status) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun FilterOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    status: AppointmentStatus? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Text(label)
        }
        if (status != null) {
            StatusChip(status = status)
        }
    }
}
