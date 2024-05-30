package it.unipi.rescuelink.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import it.unipi.rescuelink.RescueLink

class LocationUpdateService(appContext: Context, workerParameters: WorkerParameters) :
    Worker(appContext, workerParameters) {

    private val context = appContext
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationInterval: Long = LOCATION_INTERVAL

    private fun checkRequest() {
        Log.i(TAG, "Checking request")
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val settingClient = LocationServices.getSettingsClient(context)
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

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun startLocationUpdates() {
        val task = fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )
        task.addOnSuccessListener { Log.d(TAG, "Successful registration") }
        task.addOnFailureListener { Log.d(TAG, "Failure in registration: " + it.message) }
    }

    private fun stopLocationUpdates() {
        Log.i(TAG, "Stopping location updates")
        fusedLocationClient.removeLocationUpdates(callback)
    }

    private val callback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            Log.i(TAG, "Location received")
            for (location in locationResult.locations)
                Log.i(TAG, "Location: ${location.latitude} ${location.longitude}")

            locationResult.lastLocation?.let { updateCurrentLocation(it) }
        }
    }

    private fun updateCurrentLocation(location: Location) {
        RescueLink.info.thisDeviceInfo.exactPosition = LatLng(location.latitude, location.longitude)
        sendLocationBroadcast(location)
    }

    private fun sendLocationBroadcast(location: Location) {
        Log.i(TAG, "Sending location broadcast")
        Intent(LOCATION_UPDATE_ACTION).also { intent ->
            intent.putExtra(LOCATION_LATITUDE, location.latitude)
            intent.putExtra(LOCATION_LONGITUDE, location.longitude)
            context.sendBroadcast(intent)
        }
    }

    override fun doWork(): Result {
        var hasPermissions = false
        while (!hasPermissions)
        {
            hasPermissions = ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            Thread.sleep(1000)
        }
        start()
        return Result.success()
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun start() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        Log.i(TAG, "Service created")
        locationRequest = LocationRequest.Builder(locationInterval)
            .setIntervalMillis(locationInterval)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        checkRequest()
        Log.i(TAG, "Request created")

        startLocationUpdates()
    }

    // Delete me if the location stops working unexpectedly
    override fun onStopped() {
        super.onStopped()
        stopLocationUpdates()
    }

    companion object {
        private const val TAG = "LocationService"
        private const val LOCATION_INTERVAL = 15000L
        const val LOCATION_LATITUDE = "latitude"
        const val LOCATION_LONGITUDE = "longitude"
        const val LOCATION_UPDATE_ACTION = "it.unipi.location.LOCATION_UPDATE"
    }
}