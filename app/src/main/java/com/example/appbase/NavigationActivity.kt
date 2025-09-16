package com.example.appbase

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.appbase.databinding.ActivityNavigationBinding
import com.example.appbase.utils.DirectionsManager
import com.example.appbase.utils.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNavigationBinding
    private var googleMap: GoogleMap? = null // Changed to nullable and renamed to avoid conflict
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var directionsManager: DirectionsManager
    private var currentLocation: LatLng? = null
    private var destinationMarker: Marker? = null
    private var currentLocationMarker: Marker? = null
    private var currentPolyline: Polyline? = null

    // Location permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted
                getCurrentLocation()
            }
            else -> {
                // No location access granted
                Toast.makeText(this, "Location permission is required for navigation", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Google Places API (New)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // Initialize utility classes
        locationManager = LocationManager(this)
        directionsManager = DirectionsManager(this, BuildConfig.MAPS_API_KEY)

        setupMapFragment()
        setupPlacesAutocomplete()
        setupUI()
        checkLocationPermission()
    }

    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupPlacesAutocomplete() {
        // Initialize the AutocompleteSupportFragment
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        // Set up a PlaceSelectionListener to handle the response
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i("NavigationActivity", "Place: ${place.name}, ${place.id}")
                place.latLng?.let { latLng ->
                    setDestination(latLng, place.name ?: "Selected Place")
                }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Log.i("NavigationActivity", "An error occurred: $status")
                Toast.makeText(this@NavigationActivity, "Error selecting place: $status", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupUI() {
        binding.btnGetCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.btnClearRoute.setOnClickListener {
            clearRoute()
        }

        binding.btnNavigate.setOnClickListener {
            currentLocation?.let { current ->
                destinationMarker?.position?.let { destination ->
                    getDirections(current, destination)
                } ?: Toast.makeText(this, "Please select a destination first", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // Show rationale and request permission
                requestLocationPermission()
            }
            else -> {
                // Request permission
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (locationManager.hasLocationPermission()) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val location = locationManager.getCurrentLocation()
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        updateCurrentLocationMarker()
                        moveCameraToCurrentLocation()
                    } ?: run {
                        Toast.makeText(this@NavigationActivity, "Unable to get current location", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("NavigationActivity", "Error getting current location: ${e.message}")
                    Toast.makeText(this@NavigationActivity, "Error getting current location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateCurrentLocationMarker() {
        currentLocation?.let { location ->
            currentLocationMarker?.remove()
            currentLocationMarker = googleMap?.addMarker( // Use googleMap?.
                MarkerOptions()
                    .position(location)
                    .title("Current Location")
            )
        }
    }

    private fun moveCameraToCurrentLocation() {
        currentLocation?.let { location ->
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f)) // Use googleMap?.
        }
    }


    private fun setDestination(latLng: LatLng, title: String) {
        destinationMarker?.remove()
        destinationMarker = googleMap?.addMarker( // Use googleMap?.
            MarkerOptions()
                .position(latLng)
                .title(title)
        )
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng)) // Use googleMap?.
    }

    private fun clearRoute() {
        currentPolyline?.remove()
        destinationMarker?.remove()
        currentLocation?.let { location ->
            currentLocationMarker = googleMap?.addMarker( // Use googleMap?.
                MarkerOptions()
                    .position(location)
                    .title("Current Location")
            )
        }
        binding.tvRouteInfo.text = ""
        binding.btnNavigate.visibility = View.GONE
    }

    private fun getDirections(origin: LatLng, destination: LatLng) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = directionsManager.getDirections(origin, destination, "driving")
                result.fold(
                    onSuccess = { routeInfo ->
                        displayRoute(routeInfo)
                    },
                    onFailure = { exception ->
                        Log.e("NavigationActivity", "Error getting directions: ${exception.message}")
                        Toast.makeText(this@NavigationActivity, "Error getting directions: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Log.e("NavigationActivity", "Error getting directions: ${e.message}")
                Toast.makeText(this@NavigationActivity, "Error getting directions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayRoute(routeInfo: com.example.appbase.utils.RouteInfo) {
        // Remove existing polyline
        currentPolyline?.remove()
        
        // Add new polyline
        currentPolyline = googleMap?.addPolyline(routeInfo.polylineOptions) // Use googleMap?.

        // Display route information
        binding.tvRouteInfo.text = "Distance: ${routeInfo.distance}\nDuration: ${routeInfo.duration}"
        binding.tvRouteInfo.visibility = View.VISIBLE
        binding.btnNavigate.visibility = View.VISIBLE
    }

    override fun onMapReady(map: GoogleMap) { // Renamed parameter to 'map'
        googleMap = map // Assign to googleMap

        // Enable current location button
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true // Use googleMap?.
        }

        // Set map type
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL // Use googleMap?.

        // Set default location (San Francisco) if current location is not available
        if (currentLocation == null) {
            val defaultLocation = LatLng(37.7749, -122.4194)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f)) // Use googleMap?.
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.stopLocationUpdates()
        directionsManager.shutdown()
    }
}
