package it.unipi.rescuelink.adhocnet

import com.google.android.gms.maps.model.LatLng
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
        val smallerDistanceInfo = object {
            val sEstimatedDistance = estimatedDistance
            val sMeasurementPosition = measurementPosition
        }
        return smallerDistanceInfo.hashCode()
    }
}