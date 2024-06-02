package it.unipi.rescuelink.adhocnet

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable

@Serializable
data class Position(var latitude: Double, var longitude: Double) {
    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

    companion object {
        fun fromLatLng(latLng: LatLng): Position {
            return Position(latLng.latitude, latLng.longitude)
        }
    }
}


