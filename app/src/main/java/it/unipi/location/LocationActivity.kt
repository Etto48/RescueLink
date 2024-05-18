package it.unipi.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import it.unipi.rescuelink.databinding.ActivityLocationBinding

class LocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationBinding
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(LOCATION_INTERVAL).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations){
                    // Update UI with location data
                    Log.d(TAG, location.toString())
                }
            }
        }
        updateValuesFromBundle(savedInstanceState)

        binding.startUpdatesButton.setOnClickListener {
            requestingLocationUpdates = true
            startLocationUpdates()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        //outState?.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }


    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    

    private fun startLocationUpdates() {
        //requestingLocationUpdates = true
        Log.d(TAG, "startLocationUpdates")

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val t = fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
            t.addOnSuccessListener { Log.d(TAG, "Successful registration") }
            t.addOnFailureListener { Log.d(TAG, "Failure in registration: " + it.message) }
        }
    }

    private fun stopLocationUpdates() {
        //requestingLocationUpdates = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return

        // Update the value of requestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(
                REQUESTING_LOCATION_UPDATES_KEY)
        }

        // Update UI to match restored state
    }

    companion object{
        private const val REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key"
        private const val TAG = "MyLocationActivity"
        private const val LOCATION_INTERVAL = 1000L
    }




}