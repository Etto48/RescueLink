package it.unipi.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class LocationReceiver() : BroadcastReceiver() {
    init {
        Log.d(TAG, "LocationReceiver()")
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == LocationActivity.LOCATION_UPDATE_ACTION) {
            val latitude = intent.getDoubleExtra(LocationActivity.EXTRA_LATITUDE, 0.0)
            val longitude = intent.getDoubleExtra(LocationActivity.EXTRA_LONGITUDE, 0.0)
            Log.d(TAG, "Lat: $latitude, Lon: $longitude")
        }
    }

    companion object {
        private const val TAG = "LocationReceiver"
    }
}