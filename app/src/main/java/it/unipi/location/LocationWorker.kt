package it.unipi.location

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationWorker(context: Context, params: WorkerParameters) : Worker(context, params){

    var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    override fun doWork(): Result {
        Log.d(LOCATION_WORK_TAG, "Worker Avviato")
        val locationRequest = LocationRequest.Builder(LocationWorker.LOCATION_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        //val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, applicationContext.mainLooper)
        }
        catch(e: SecurityException)
        {
            Log.e(LOCATION_WORK_TAG, "Error: ${e.message}")
            return Result.failure()
        }
        return Result.success()
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult ?: return
            val location = locationResult.lastLocation
            fusedLocationProviderClient.removeLocationUpdates(this)
            Log.d(LOCATION_WORK_TAG, "Location: $location")
        }
    }

    companion object {
        const val LOCATION_WORK = "LOCATION_WORK"
        const val LOCATION_WORK_TAG = "LocationWorker"

        const val LOCATION_INTERVAL = 1000L
    }
}