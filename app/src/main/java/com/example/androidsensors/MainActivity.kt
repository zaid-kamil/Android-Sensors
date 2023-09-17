package com.example.androidsensors

import android.hardware.Sensor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOutBounce
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.androidsensors.ui.theme.AndroidSensorsTheme
import dev.ricknout.composesensors.getSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSensorsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sensorList: List<Sensor>?,
    onSelect: (Sensor) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text(text = "Android Sensors")
            })
        },
        modifier = modifier.padding(16.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(sensorList?.size ?: 0) { index ->
                    val sensor = sensorList?.get(index)
                    SensorItem(sensor = sensor, onSelect = onSelect)
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorItem(
    sensor: Sensor?,
    onSelect: (Sensor) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { sensor?.let { onSelect(it) } },
        shape = MaterialTheme.shapes.extraSmall,
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .weight(6f)
            ) {
                Text(
                    text = sensor?.name ?: "Unknown❌",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Type - ${sensor?.stringType?.split(".")?.last()}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            )
        }
    }
}

// SensorUiState
data class SensorUIState(
    var sensor: Sensor? = null
)

// SensorViewModel
class SensorViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<SensorUIState> = MutableStateFlow(SensorUIState())
    val uiState: StateFlow<SensorUIState> = _uiState.asStateFlow()

    fun setSensor(sensor: Sensor?) {
        _uiState.value = _uiState.value.copy(sensor = sensor)
    }
}

// Sensor Screens
enum class SensorDestination {
    Home,
    Detail
}

// sensor NavGraph
@Composable
fun AppNavigation() {
    val viewModel: SensorViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SensorDestination.Home.name,
    ) {
        composable(SensorDestination.Home.name) {
            val sensorManager = getSensorManager()
            val allSensors = sensorManager?.getSensorList(Sensor.TYPE_ALL)
            HomeScreen(sensorList = allSensors, onSelect = {
                viewModel.setSensor(it)
                navController.navigate(SensorDestination.Detail.name)
            })
        }
        composable(
            SensorDestination.Detail.name,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(200, easing = EaseInOutBounce)
//                    times
                )
            }
            ) {
            DetailScreen(uiState.value)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: SensorUIState,
    modifier: Modifier = Modifier
) {
    val sensor = state.sensor
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text(text = sensor?.stringType?.split(".")?.last()?.uppercase() ?: "Unknown❌")
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(text = sensor?.stringType ?: "Unknown❌",
                style = MaterialTheme.typography.titleMedium)
//            text style
        }
    }
}