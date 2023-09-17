package com.example.androidsensors

import android.hardware.Sensor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOutBounce
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.androidsensors.ui.theme.AndroidSensorsTheme
import com.mutualmobile.composesensors.SensorDelay
import com.mutualmobile.composesensors.rememberAccelerometerSensorState
import com.mutualmobile.composesensors.rememberGravitySensorState
import com.mutualmobile.composesensors.rememberLightSensorState
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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        topBar = {
            LargeTopAppBar(title = {
                Text(text = "Android Sensors")
            }, scrollBehavior = scrollBehavior)
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
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
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .weight(7f)
            ) {
                Text(
                    text = sensor?.name ?: "Unknown❌",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Type - ${sensor?.stringType?.split(".")?.last()}",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.extraLarge
                    )
                    .padding(16.dp)
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
                    animationSpec = tween(500, easing = EaseInOutBounce)
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
                Text(text = "${sensor?.stringType?.split(".")?.last()?.lowercase()} sensor")
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (sensor?.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val accelerometer = rememberAccelerometerSensorState(
                        sensorDelay = SensorDelay.UI
                    )
                    if (accelerometer.isAvailable) {
                        Text(text = "X ${accelerometer.xForce}")
                        Text(text = "Y ${accelerometer.yForce}")
                        Text(text = "Z ${accelerometer.zForce}")
                        BoxWithConstraints(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            val width = constraints.maxWidth
                            val height = constraints.maxHeight
                            Text(
                                "Content",
                                modifier.offset(
                                    x = accelerometer.xForce.toInt().dp * width / 30f,
                                    y = accelerometer.yForce.toInt().dp * height / 30f
                                ),
                                style = MaterialTheme.typography.headlineLarge,
                            )
                        }
                    }
                }

                Sensor.TYPE_GRAVITY -> {
                    val gravity = rememberGravitySensorState()
                    if (gravity.isAvailable) {
                        Text(text = "X ${gravity.xForce}")
                        Text(text = "Y ${gravity.yForce}")
                        Text(text = "Z ${gravity.zForce}")
                    }
                }

                Sensor.TYPE_LIGHT -> {
                    val light = rememberLightSensorState()
                    if (light.isAvailable) {
                        Text(
                            text = "${light.illuminance}",
                            fontSize = light.illuminance.toInt().sp
                        )
                    }
                }
            }
        }
    }
}