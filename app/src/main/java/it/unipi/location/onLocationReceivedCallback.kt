package it.unipi.location

import com.google.android.gms.maps.model.LatLng

interface onLocationReceivedCallback {
    fun onLocationReceived(location: LatLng)
}