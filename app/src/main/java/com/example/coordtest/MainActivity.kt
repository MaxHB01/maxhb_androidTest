package com.example.coordtest

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.coordtest.ui.theme.CoordTestTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            CoordTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationScreen(fusedLocationClient)
                }
            }
        }
    }
}

@Composable
fun LocationScreen(fusedLocationClient: FusedLocationProviderClient) {
    var locationText by remember { mutableStateOf("Coordinates will be shown here") }
    val context = LocalContext.current
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getLastKnownLocation(context, fusedLocationClient) { location ->
                val speedKmph = location.speed * 3.6
                locationText = "Latitude: ${location.latitude}, Longitude: ${location.longitude}, Accuracy: ${location.accuracy} meters, Speed: %.2f km/h".format(speedKmph)
            }
        } else {
            locationText = "Permission denied"
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    getLastKnownLocation(context, fusedLocationClient) { location ->
                        val speedKmph = location.speed * 3.6
                        locationText = "Latitude: ${location.latitude}, Longitude: ${location.longitude}, Accuracy: ${location.accuracy} meters, Speed: %.2f km/h".format(speedKmph)
                    }
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as ComponentActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) -> {
                    // Show rationale dialog and request permission
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                else -> {
                    // Directly request for permission
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }) {
            Text(text = "Get Coordinates")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = locationText)
    }
}

private fun getLastKnownLocation(
    context: android.content.Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationAvailable: (Location) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            location?.let {
                onLocationAvailable(it)
            } ?: run {
                // Handle location null case
            }
        }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CoordTestTheme {
        // Preview doesn't require FusedLocationProviderClient
        LocationScreenPreview()
    }
}

@Composable
fun LocationScreenPreview() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = {}) {
            Text(text = "Get Coordinates")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Coordinates will be shown here")
    }
}
