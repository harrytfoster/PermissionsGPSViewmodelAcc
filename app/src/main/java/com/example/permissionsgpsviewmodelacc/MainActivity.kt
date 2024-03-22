package com.example.permissionsgpsviewmodelacc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.permissionsgpsviewmodelacc.ui.theme.PermissionsGPSViewmodelAccTheme
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint

data class LatLon(val lat: Double, val lon: Double) // our LatLon class

class LatLonViewModel: ViewModel() {
    var latLon = LatLon(51.05, -0.72)
        set(newValue) {
            field = newValue
            latLonLiveData.value = newValue
        }

    var latLonLiveData = MutableLiveData<LatLon>()
}



class MainActivity : ComponentActivity(), LocationListener { // LocationListener handles location updates
    val viewModel: LatLonViewModel by viewModels() // Access ViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        setContent {
            DisplayCoords(LatLonViewModel())
        }
    }

    fun checkPermissions() {
        val requiredPermission =
            Manifest.permission.ACCESS_FINE_LOCATION // Choose permission to use
        if (checkSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED) { // Checks is permission is already granted
            startGPS()
        } else { // If permission is not already granted
            val permissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> // Request Permission
                    if (isGranted) {
                        startGPS()
                    } else {
                        Toast.makeText(this, "GPS permission not granted", Toast.LENGTH_LONG)
                            .show() // Show this is permission is not granted
                    }
                }
            permissionLauncher.launch(requiredPermission) // Launches permission
        }
    }

    @SuppressLint("MissingPermission") // NEED THIS
    fun startGPS() {
        val mgr =
            getSystemService(LOCATION_SERVICE) as LocationManager // System obtains default location manager
        mgr.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            this
        ) // Update app with new location information
    }

    override fun onLocationChanged(location: Location) {
        Toast.makeText(
            this,
            "Latitude: ${location.latitude}, Longitude: ${location.longitude}",
            Toast.LENGTH_LONG
        ).show()
        viewModel.latLon = LatLon(location.latitude, location.longitude)
    }

    override fun onProviderEnabled(provider: String) {
        Toast.makeText(this, "GPS enabled", Toast.LENGTH_LONG).show()

    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(this, "GPS disabled", Toast.LENGTH_LONG).show()
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) { // NEED THIS TOO

    }
}


    @Composable
    fun DisplayCoords(latLonViewModel: LatLonViewModel) {
        var geoLocation by remember { mutableStateOf(GeoPoint(0.0, 0.0)) }
        val context = LocalContext.current
        latLonViewModel.latLonLiveData.observe(context as ComponentActivity){LatLon ->
            geoLocation = GeoPoint(LatLon.lat, LatLon.lon)
        }
        Text("Latitude: ${geoLocation.latitude} Longitude: ${geoLocation.longitude}")
    }

