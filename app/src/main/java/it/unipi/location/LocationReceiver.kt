package it.unipi.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.maps.model.LatLng

class LocationReceiver(val callback: onLocationReceivedCallback) : BroadcastReceiver() {
    init {
        Log.d(TAG, "init LocationReceiver")
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive "+ intent?.action)
        Log.d(TAG, "onReceive "+ context?.javaClass)
        if (intent?.action == LocationUpdateService.LOCATION_UPDATE_ACTION) {
            val latitude = intent.getDoubleExtra(LocationUpdateService.LOCATION_LATITUDE, 0.0)
            val longitude = intent.getDoubleExtra(LocationUpdateService.LOCATION_LONGITUDE, 0.0)
            Log.d(TAG, "Lat: $latitude, Lon: $longitude")
            callback.onLocationReceived(LatLng(latitude, longitude))
        }
    }

    companion object {
        private const val TAG = "LocationReceiver"
    }
}