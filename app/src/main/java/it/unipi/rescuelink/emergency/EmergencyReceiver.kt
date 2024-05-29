package it.unipi.rescuelink.emergency

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class EmergencyReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "evento ricevuto")
    }

    companion object {
        private const val TAG = "EmergencyReceiver"
    }
}