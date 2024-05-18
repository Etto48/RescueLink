package it.unipi.location

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationUpdateService : Service() {
    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myRequest: LocationRequest

    var location : Location? = null

    fun getLocation(): Location? {
        try {
            val locTask = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token,
            )

            locTask.addOnSuccessListener {
                location = it
                it?.let {
                    sendLocationBroadcast(it)
                }
                Log.i(TAG, " - Current Location: ${location?.latitude} ${location?.longitude}")
            }

            return location
        }
        catch (e: SecurityException){
            Log.e(TAG, "Security Exception")
            e.printStackTrace()
            return null
        }
    }

    private fun sendLocationBroadcast(it: Location) {
        val intent = Intent(LOCATION_UPDATE_ACTION)
        intent.putExtra(LOCATION_LATITUDE, it.latitude)
        intent.putExtra(LOCATION_LONGITUDE, it.longitude)
        sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        internal val service: LocationUpdateService
            get() = this@LocationUpdateService

        fun getService(): LocationUpdateService {
            return this@LocationUpdateService
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Log.i(TAG, "Service created")
        myRequest = LocationRequest.Builder(LOCATION_INTERVAL)
            .setDurationMillis(Long.MAX_VALUE)
            .setIntervalMillis(LOCATION_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        Log.i(TAG, "Request created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //return super.onStartCommand(intent, flags, startId)
        startLocationUpdates()
        Log.i(TAG, "Service started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Service destroyed")
    }

    fun stopLocationUpdates(){
        Log.i(TAG, "Stopping location updates")
        fusedLocationClient.removeLocationUpdates(callback)
    }

    fun startLocationUpdates(){
        Log.i(TAG, "Starting location updates")
        try {
            val t = fusedLocationClient.requestLocationUpdates(myRequest, callback, Looper.getMainLooper())
            t.addOnSuccessListener { Log.d("LocationService", "Location updates started") }
            t.addOnFailureListener { Log.d("LocationService", "Location updates Failed") }
        }
        catch (e: SecurityException){
            Log.e(TAG, "Security Exception")
            e.printStackTrace()
        }
    }

    private val callback = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            Log.i("LocationService", "Location received")
            for (location in locationResult.locations)
                Log.i(TAG, "Location: ${location.latitude} ${location.longitude}")

            //this@LocationUpdateService.location = locationResult.lastLocation
                //"""Location: ${location.latitude} ${location.longitude}\n"""
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    companion object {
        private const val TAG = "LocationService"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val LOCATION_INTERVAL = 1000L
        private const val LOCATION_FASTEST_INTERVAL = 5000L
        private const val LOCATION_MAX_AGE = 1000L
        private const val LOCATION_LATITUDE = "latitude"
        private const val LOCATION_LONGITUDE = "longitude"
        private const val LOCATION_UPDATE_ACTION = "it.unipi.location.LOCATION_UPDATE"


    }
}