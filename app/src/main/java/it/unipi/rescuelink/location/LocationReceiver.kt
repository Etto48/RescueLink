package it.unipi.rescuelink.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.maps.model.LatLng

class LocationReceiver(private val callback: OnLocationReceivedCallback) : BroadcastReceiver() {
    init {
        Log.d(TAG, "init LocationReceiver")
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive "+ intent?.action)
        Log.d(TAG, "onReceive "+ context?.javaClass)
        if (intent?.action == LocationUpdateWorker.LOCATION_UPDATE_ACTION) {
            val latitude = intent.getDoubleExtra(LocationUpdateWorker.LOCATION_LATITUDE, 0.0)
            val longitude = intent.getDoubleExtra(LocationUpdateWorker.LOCATION_LONGITUDE, 0.0)
            Log.d(TAG, "Lat: $latitude, Lon: $longitude")
            callback.onLocationReceived(LatLng(latitude, longitude))
        }
    }

    companion object {
        private const val TAG = "LocationReceiver"
    }
}