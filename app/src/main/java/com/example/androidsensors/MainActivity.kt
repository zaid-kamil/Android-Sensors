package com.example.androidsensors

import android.hardware.Sensor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.androidsensors.ui.theme.AndroidSensorsTheme
import dev.ricknout.composesensors.getSensorManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSensorsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SensorScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorScreen(modifier: Modifier = Modifier) {

    val sensorManager = getSensorManager()
    val allSensors = sensorManager?.getSensorList(Sensor.TYPE_ALL)
    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(allSensors?.size ?: 0) { index ->
                    val sensor = allSensors?.get(index)
                    SensorItem(sensor = sensor)
                }
            }
        }

    }
}

@Composable
fun SensorItem(
    sensor: Sensor?,
    modifier: Modifier = Modifier
) {
    Card {
        Column {
            Text(text = sensor?.name ?: "Unknown❌")
        }
    }
}

// SensorUiState
data class SensorUIState(
    val sensor: Sensor? = null
)

// SensorViewModel
class SensorViewModel : ViewModel() {

}

// Sensor Screens
enum class SensorDestination {
    Home,
    Detail
}

// sensor NavGraph

class Navigation() {
//    val navControlller = rememberNavController()
}

@Composable
fun `DetailScreen`(
    modifier: Modifier = Modifier
) {
}