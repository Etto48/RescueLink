package it.unipi.rescuelink.adhocnet

import com.google.android.gms.maps.model.LatLng
import java.time.OffsetDateTime

class DeviceInfo(
    exactPosition: LatLng? = null,
    personalInfo: PersonalInfo? = null,
    deviceName: String? = null,
    var isSAR: Boolean = false,
    var knownDistances: MutableList<DistanceInfo>? = null
) {
    private var timestamp: Long = OffsetDateTime.now().toEpochSecond()

    companion object {
        const val MAX_KNOWN_DISTANCES = 5
    }

    var exactPosition: LatLng? = exactPosition
        set(value) {
            field = value
            updateTimestamp()
        }

    var personalInfo: PersonalInfo? = personalInfo
        set(value) {
            field = value
            updateTimestamp()
        }

    var deviceName: String? = deviceName
        set(value) {
            field = value
            updateTimestamp()
        }

    fun addDistanceInfo(distanceInfo: DistanceInfo) {
        if (knownDistances == null)
        {
            knownDistances = mutableListOf()
        }
        knownDistances!!.add(distanceInfo)
        knownDistances?.sortByDescending { it.timestamp }
        knownDistances = knownDistances?.take(MAX_KNOWN_DISTANCES)?.toMutableList()
    }

    private fun updateTimestamp() {
        timestamp = OffsetDateTime.now().toEpochSecond()
    }

    fun merge(other: DeviceInfo): DeviceInfo {
        val ret = DeviceInfo()
        // get the most recent exact position and personal info
        if (timestamp > other.timestamp)
        {
            ret.exactPosition = exactPosition
            ret.personalInfo = personalInfo
            ret.deviceName = deviceName
            ret.isSAR = isSAR
            ret.timestamp = timestamp
        }
        else
        {
            ret.exactPosition = other.exactPosition
            ret.personalInfo = other.personalInfo
            ret.deviceName = other.deviceName
            ret.isSAR = other.isSAR
            ret.timestamp = other.timestamp
        }

        // exact position was not available, try to populate known distances
        if (ret.exactPosition == null)
        {
            val fullList = knownDistances.orEmpty().union(other.knownDistances.orEmpty()).toMutableList()
            fullList.sortByDescending { it.timestamp }
            ret.knownDistances = fullList.take(MAX_KNOWN_DISTANCES).toMutableList()
        }

        return ret
    }
}