package it.unipi.rescuelink.adhocnet

import com.google.android.gms.maps.model.LatLng

class DistanceInfo(
    // The estimated distance between two devices in meters
    var estimatedDistance: Double,
    var measurementPosition: LatLng
)