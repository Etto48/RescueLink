package it.unipi.rescuelink.adhocnet

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
class DeviceInfo(
    private var exactPosition: Position? = null,
    private var personalInfo: PersonalInfo? = null,
    private var deviceName: String? = null,
    private var isSAR: Boolean = false,
    var knownDistances: MutableList<DistanceInfo>? = null
) {
    private var timestamp: Long = OffsetDateTime.now().toEpochSecond()

    companion object {
        const val MAX_KNOWN_DISTANCES = 5
    }

    fun setExactPosition(value: LatLng?) {
        exactPosition = if (value == null) null else Position.fromLatLng(value)
        updateTimestamp()
    }

    fun setPersonalInfo(value: PersonalInfo?) {
        personalInfo = value
        updateTimestamp()
    }

    fun setDeviceName(value: String?) {
        deviceName = value
        updateTimestamp()
    }

    fun setIsSAR(value: Boolean) {
        isSAR = value
        updateTimestamp()
    }

    fun getExactPosition(): LatLng? {
        return exactPosition?.toLatLng()
    }

    fun getPersonalInfo(): PersonalInfo? {
        return personalInfo
    }

    fun getDeviceName(): String? {
        return deviceName
    }

    fun getIsSAR(): Boolean {
        return isSAR
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