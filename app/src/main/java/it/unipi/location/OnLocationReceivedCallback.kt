package it.unipi.location

import com.google.android.gms.maps.model.LatLng

interface OnLocationReceivedCallback {
    fun onLocationReceived(location: LatLng)
}