package com.example.hipocrates

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.hipocrates.data.DataStoreManager
import com.example.hipocrates.navigation.AppNavigation
import com.example.hipocrates.navigation.Screen
import com.example.hipocrates.ui.theme.HipocratesTheme
import com.example.hipocrates.viewmodel.AppViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dataStoreManager = DataStoreManager(applicationContext)
        val viewModel = AppViewModel(dataStoreManager)

        setContent {
            HipocratesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HipocratesPlus(viewModel = viewModel, dataStoreManager = dataStoreManager)
                }
            }
        }
    }
}

@Composable
fun HipocratesPlus(
    viewModel: AppViewModel,
    dataStoreManager: DataStoreManager
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            val currentUser = dataStoreManager.getCurrentUser().first()
            startDestination = if (currentUser != null) {
                Screen.Home.route
            } else {
                Screen.Login.route
            }
        }
    }

    startDestination?.let { destination ->
        AppNavigation(
            navController = navController,
            viewModel = viewModel,
            startDestination = destination
        )
    }
}