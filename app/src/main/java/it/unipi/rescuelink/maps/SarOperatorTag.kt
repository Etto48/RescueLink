package it.unipi.rescuelink.maps

import com.google.android.gms.maps.model.LatLng

class SarOperatorTag(
    val id: String,
    val deviceName: String?,
    val name: String?,
    val position: LatLng,
) {
}