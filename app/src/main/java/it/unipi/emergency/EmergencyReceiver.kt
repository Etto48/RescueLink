package it.unipi.emergency

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "EmergencyReceiver"

class EmergencyReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "evento ricevuto")
    }
}