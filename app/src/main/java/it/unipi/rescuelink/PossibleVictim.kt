package it.unipi.rescuelink

import com.google.android.gms.maps.model.LatLng

data class PossibleVictim(
    val id: String,
    val name: String?,
    val position: LatLng,
    val age: Int?,
    val weight: Int?,
    val heartRate: Int?
)

