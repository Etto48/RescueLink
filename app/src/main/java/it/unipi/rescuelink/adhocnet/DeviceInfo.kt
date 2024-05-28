package it.unipi.rescuelink.adhocnet

import com.google.android.gms.maps.model.LatLng
import java.time.OffsetDateTime

class DeviceInfo(
    exactPosition: LatLng? = null,
    personalInfo: PersonalInfo? = null,
    var knownDistances: MutableList<DistanceInfo>? = null
) {
    private var timestamp: Long = OffsetDateTime.now().toEpochSecond()

    var exactPosition: LatLng? = exactPosition
        set(value) {
            field = value
            timestamp = OffsetDateTime.now().toEpochSecond()
        }

    var personalInfo: PersonalInfo? = personalInfo
        set(value) {
            field = value
            timestamp = OffsetDateTime.now().toEpochSecond()
        }

    fun merge(other: DeviceInfo): DeviceInfo {
        val maxKnownDistances = 5

        val ret = DeviceInfo()
        // get the most recent exact position and personal info
        if (timestamp > other.timestamp)
        {
            ret.exactPosition = exactPosition
            ret.personalInfo = personalInfo
            ret.timestamp = timestamp
        }
        else
        {
            ret.exactPosition = other.exactPosition
            ret.personalInfo = other.personalInfo
            ret.timestamp = other.timestamp
        }

        // exact position was not available, try to populate known distances
        if (ret.exactPosition == null)
        {
            val fullList = knownDistances.orEmpty().union(other.knownDistances.orEmpty()).toMutableList()
            fullList.sortByDescending { it.timestamp }
            ret.knownDistances = fullList.take(maxKnownDistances).toMutableList()
        }

        return ret
    }
}