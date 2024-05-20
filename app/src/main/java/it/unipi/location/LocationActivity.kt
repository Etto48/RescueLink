package it.unipi.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
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
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import it.unipi.rescuelink.databinding.ActivityLocationBinding

class LocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationBinding
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private lateinit var task: Task<Void>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.text = ""

        fusedLocationClient = getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(LOCATION_INTERVAL)
            .setIntervalMillis(LOCATION_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()


        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val settingClient = LocationServices.getSettingsClient(this)
        settingClient.checkLocationSettings(locationSettingsRequest).addOnCompleteListener(
            fun(task: Task<LocationSettingsResponse>) {
                if (task.isSuccessful) {
                    Log.d(TAG, "Request Check Passed")
                } else {
                    Log.d(TAG, "Request Check Failed")
                }
            }
        )

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations){
                    // Update UI with location data
                    Log.d(TAG, location.toString())
                    binding.textView.append(location.toString()+'\n')
                    sendLocationBroadcast(location)
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
        outState?.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        if (requestingLocationUpdates) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        stopLocationUpdates()
    }

    

    private fun startLocationUpdates() {
        //requestingLocationUpdates = true
        Log.d(TAG, "startLocationUpdates")

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            task = fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            task.addOnSuccessListener { Log.d(TAG, "Successful registration") }
            task.addOnFailureListener { Log.d(TAG, "Failure in registration: " + it.message) }
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            Log.d(TAG, "Permission not granted")
        }
    }

    private fun stopLocationUpdates() {
        //requestingLocationUpdates = false
        Log.d(TAG, "stopLocationUpdates")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun sendLocationBroadcast(location: Location) {
        val intent = Intent(LOCATION_UPDATE_ACTION).apply {
            putExtra(EXTRA_LATITUDE, location.latitude)
            putExtra(EXTRA_LONGITUDE, location.longitude)
        }
        Log.d(TAG, "Sending broadcast")
        sendBroadcast(intent)
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
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val LOCATION_UPDATE_ACTION = "it.unipi.location.LOCATION_UPDATE"
    }




}