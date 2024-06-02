package it.unipi.rescuelink.adhocnet

import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.math.pow

@Serializable
class DistanceInfo(
    // The estimated distance between two devices in meters
    var estimatedDistance: Double,
    var measurementPosition: Position,
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
            val distance = 10.0.pow((txPower - rssi)/(10 * pathLossExponent))
            // clamp between 0 and 200 meters
            return distance.coerceIn(0.0, 200.0)
        }
    }
}