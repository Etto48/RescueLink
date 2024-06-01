package it.unipi.rescuelink.adhocnet

import com.google.android.gms.maps.model.LatLng
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.math.pow

class DistanceInfo(
    // The estimated distance between two devices in meters
    var estimatedDistance: Double,
    var measurementPosition: LatLng,
) {
    val timestamp: Long = OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond()

    override fun equals(other: Any?): Boolean {
        val otherAsDistanceInfo = other as? DistanceInfo
        return estimatedDistance == otherAsDistanceInfo?.estimatedDistance &&
            measurementPosition == otherAsDistanceInfo.measurementPosition
    }

    override fun hashCode(): Int {
        return estimatedDistance.hashCode() xor measurementPosition.hashCode()
    }

    companion object {
        fun estimateDistance(rssi: Int, txPower: Int, indoor: Boolean = false): Double {
            val pathLossExponent = if (indoor) 3.0 else 2.0
            return 10.0.pow((txPower - rssi)/(10 * pathLossExponent))
        }
    }
}