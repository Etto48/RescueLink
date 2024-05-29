package it.unipi.rescuelink.maps

import com.google.android.gms.maps.model.LatLng

data class PossibleVictimTag(
    val id: String,
    val name: String?,
    val position: LatLng,
    val age: Int?,
    val weight: Double?,
    val heartRate: Double?,
    val simpleTag: Boolean = false
){
    constructor(id: String, position: LatLng) : this(id,null,position,null,null,null, true)
}

