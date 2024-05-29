package it.unipi.rescuelink.location

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task

class LocationUpdateService(private var locationInterval: Long = LOCATION_INTERVAL) : Service() {
    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Log.i(TAG, "Service created")
        locationRequest = LocationRequest.Builder(locationInterval)
            .setIntervalMillis(locationInterval)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        checkRequest()
        Log.i(TAG, "Request created")
    }

    private fun checkRequest(){
        Log.i(TAG, "Checking request")
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startLocationUpdates()
        Log.i(TAG, "Service started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        Log.i(TAG, "Service destroyed")
    }

    private fun startLocationUpdates(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing location permission")
            stopSelf()
            return
        }

        val task = fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )
        task.addOnSuccessListener { Log.d(TAG, "Successful registration") }
        task.addOnFailureListener { Log.d(TAG, "Failure in registration: " + it.message) }
    }

    private fun stopLocationUpdates(){
        Log.i(TAG, "Stopping location updates")
        fusedLocationClient.removeLocationUpdates(callback)
    }

    private val callback = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            Log.i(TAG, "Location received")
            for (location in locationResult.locations)
                Log.i(TAG, "Location: ${location.latitude} ${location.longitude}")

            locationResult.lastLocation?.let { sendLocationBroadcast(it) }
        }
    }

    private fun sendLocationBroadcast(location: Location) {
        Log.i(TAG, "Sending location broadcast")
        Intent(LOCATION_UPDATE_ACTION).also { intent ->
            intent.putExtra(LOCATION_LATITUDE, location.latitude)
            intent.putExtra(LOCATION_LONGITUDE, location.longitude)
            sendBroadcast(intent)
        }
    }

    inner class LocalBinder : Binder() {
        internal val service: LocationUpdateService
            get() = this@LocationUpdateService

        fun getService(): LocationUpdateService {
            return this@LocationUpdateService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    companion object {
        private const val TAG = "LocationService"
        private const val LOCATION_INTERVAL = 5000L
        const val LOCATION_LATITUDE = "latitude"
        const val LOCATION_LONGITUDE = "longitude"
        const val LOCATION_UPDATE_ACTION = "it.unipi.location.LOCATION_UPDATE"
    }
}