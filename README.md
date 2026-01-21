![ic_launcher_hipocrates](https://github.com/user-attachments/assets/f01d7c87-464c-4b4c-848f-fc2e766668be)
# Hipócrates+
Hipócrates+ es una aplicación para Android desarrollada en Kotlin con Jetpack Compose para gestionar citas médicas para la clínica Hipócrates. La aplicación usa Material Design 3 con colores dinámicos y animaciones fluidas (Navigation Compose).

Se almacenan usuarios registrados, sesión actual y citas médicas mediante DataStore. Por ahora, la persistencia de datos es de forma local y pronto se traspasará a SQLite.

### LoginScreen - Pantalla de autenticación

<img width="270" height="600" alt="image" src="https://github.com/user-attachments/assets/f7f4cee8-2a0b-4457-9a02-f97a401e62b2" />

- Campos validados (email y contraseña)
- Validación en tiempo real con feedback visual
- Navegación a registro para nuevos usuarios
- Gestión de sesión persistente

### RegisterScreen - Registro de usuarios

<img width="270" height="600" alt="image" src="https://github.com/user-attachments/assets/71065797-66d7-4a1f-8a58-4e2fb77449ed" />

- Nombre completo
- RUT (XX.XXX.XXX-X, XXXXXXXXX)
- Número de teléfono (9XXXXXXXX, +569XXXXXXXX)
- Email (formato válido)
- Contraseña (mínimo 6 caracteres)
- Confirmación de contraseña
- Validaciones para el formulario

### HomeScreen - Dashboard

<img width="270" height="600" alt="image" src="https://github.com/user-attachments/assets/3ef77166-de2d-43eb-ae2a-d00a73c562e1" />

- Bienvenida personalizada con nombre del usuario
- Tarjetas de acceso rápido: Nueva cita y historial
- Lista de próximas citas ordenadas cronológicamente
- Botón para crear cita rápida
- Menú para ver perfil y cerrar sesión

### NewAppointmentScreen - Creación de citas

<img width="270" height="600" alt="image" src="https://github.com/user-attachments/assets/99b4070c-d5e1-4e6e-8af7-ce7d7ae3fe8f" />

- Selección de especialidad
- Selección de doctor
- Android Date Picker para seleccionar fecha
- Android Time Picker para seleccionar hora
- Campo de motivo consulta
- Campo de notas adicionales opcional
- Validaciones para el formulario
  
### AppointmentHistoryScreen - Historial de citas

<img width="270" height="600" alt="image" src="https://github.com/user-attachments/assets/5a0618b8-b5bb-4e10-b499-9e333c2a4269" />

- Lista completa de todas las citas
- Búsqueda en tiempo real por: doctor, especialidad o motivo
- Filtrado por estado: pendiente, confirmada, completada, cancelada o reprogramada.
  
### AppointmentDetailScreen - Detalle de cita

<img width="270" height="600" alt="image" src="https://github.com/user-attachments/assets/8a2ca973-6ae2-4fdf-a62b-76fa82a93835" />

- Información completa de la cita seleccionada
- Cambiar estado de la cita
- Exportar a PDF y compartir usando nativo
- Eliminación de cita con confirmación

### ProfileScreen - Perfil

<img width="270" height="600" alt="image" src="https://github.com/user-attachments/assets/d7a5b7d4-2851-4b5c-80c1-24b7fea4e16a" />

- Información del usuario
- Estadísticas de citas
- Cerrar la sesión
