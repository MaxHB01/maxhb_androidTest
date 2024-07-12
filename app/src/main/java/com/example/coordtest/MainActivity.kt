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
import com.panasonic.toughpad.android.api.ToughpadApi
import com.panasonic.toughpad.android.api.ToughpadApiListener
import com.panasonic.toughpad.android.api.barcode.BarcodeData
import com.panasonic.toughpad.android.api.barcode.BarcodeListener
import com.panasonic.toughpad.android.api.barcode.BarcodeReader

abstract class MainActivity : ComponentActivity(), ToughpadApiListener, BarcodeListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var barcodeReader: BarcodeReader? = null
    private var barcodeData by mutableStateOf("No barcode scanned")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        ToughpadApi.initialize(this, this)

        setContent {
            CoordTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(fusedLocationClient, barcodeData) {
                        startBarcodeScan()
                    }
                }
            }
        }
    }

    private fun startBarcodeScan() {
        barcodeReader?.let {
            it.addBarcodeListener(this)
            it.pressSoftwareTrigger()
        }
    }

    override fun onApiConnected() {
        val readers = BarcodeReader.getBarcodeReaders()
        if (readers.isNotEmpty()) {
            barcodeReader = readers[0]
        }
    }

    override fun onApiDisconnected() {
        barcodeReader?.removeBarcodeListener(this)
    }

    override fun onRead(data: BarcodeData?) {
        barcodeData = data?.text ?: "Failed to read barcode"
    }

    override fun onDestroy() {
        super.onDestroy()
        ToughpadApi.destroy()
    }
}

@Composable
fun MainScreen(
    fusedLocationClient: FusedLocationProviderClient,
    barcodeData: String,
    onScanBarcode: () -> Unit
) {
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
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }) {
            Text(text = "Get Coordinates")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = locationText)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onScanBarcode) {
            Text(text = "Scan Barcode")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = barcodeData)
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
        MainScreenPreview()
    }
}

@Composable
fun MainScreenPreview() {
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
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {}) {
            Text(text = "Scan Barcode")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "No barcode scanned")
    }
}
