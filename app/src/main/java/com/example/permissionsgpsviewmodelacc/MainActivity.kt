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
import org.osmdroid.config.Configuration
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.preference.PreferenceManager
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
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.unit.dp
import org.osmdroid.views.overlay.Marker

data class LatLon(val lat: Double, val lon: Double) // our LatLon class

class LatLonViewModel: ViewModel() { // Create View Model
    var latLon = LatLon(51.05, -0.72) // Set original Coords
        set(newValue) {
            field = newValue
            latLonLiveData.value = newValue
        }

    var latLonLiveData = MutableLiveData<LatLon>() // Make it live data
}



class MainActivity : ComponentActivity(), LocationListener { // LocationListener handles location updates
    val viewModel: LatLonViewModel by viewModels() // Access ViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
        checkPermissions()
        setContent {
            //DisplayCoords(viewModel)
            //MapToLocation(viewModel)
            AddMarker()
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
        )
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
    fun DisplayCoords(latLonViewModel: LatLonViewModel) { // Takes import of the ViewModel
        var geoLocation by remember { mutableStateOf(GeoPoint(0.0, 0.0)) } // Set the geopoint
        val context = LocalContext.current
        latLonViewModel.latLonLiveData.observe(context as ComponentActivity){LatLon -> // Observe current lat and lon
            geoLocation = GeoPoint(LatLon.lat, LatLon.lon) // Set the geopoint to the lat lon
        }
        Text("Latitude: ${geoLocation.latitude} Longitude: ${geoLocation.longitude}")
    }

    @Composable
    fun MapToLocation(latLonViewModel: LatLonViewModel){ // Moving map to the location of user
        var geoLocation by remember { mutableStateOf(GeoPoint(0.0, 0.0)) }
        val context = LocalContext.current
        latLonViewModel.latLonLiveData.observe(context as ComponentActivity){LatLon ->
            geoLocation = GeoPoint(LatLon.lat, LatLon.lon)
        }
        AndroidView(factory = {
                context ->
            MapView(context).apply()
            {
                controller.setCenter(geoLocation)
                controller.setZoom(14.0)
                setTileSource(TileSourceFactory.MAPNIK)
                isClickable =true
            }},
            update = {view -> view.controller.setCenter(geoLocation)})
    }

    @Composable
    fun AddMarker(){
        AndroidView (

            factory =  { ctx ->
                Configuration.getInstance()
                    .load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

                val map1 = MapView(ctx).apply {// Creating Map
                    setMultiTouchControls(true)
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(14.0)
                    controller.setCenter(GeoPoint(51.05, -0.72))
                }
                val marker = Marker(map1) // Making marker and passing map into it
                marker.apply {
                    position = GeoPoint(51.05, -0.72) // Setting location of marker
                    title = "Fernhurst, village in West Sussex" // Setting Name of marker
                }
                map1.overlays.add(marker) // Adding the marker to the map
                map1 // last statement is return value of lambda
            }
        )
    }
