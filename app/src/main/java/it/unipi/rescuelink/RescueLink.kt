package it.unipi.rescuelink

import android.app.Application
import com.google.gson.Gson
import it.unipi.rescuelink.adhocnet.DeviceInfo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray


class RescueLink : Application() {

    @Serializable
    data class Info (
        /** A map of nearby devices and their information, indexed by their bluetooth MAC address **/
        var nearbyDevicesInfo: MutableMap<String, DeviceInfo> = mutableMapOf(),
        /** The device information of this device **/
        var thisDeviceInfo: DeviceInfo = DeviceInfo()
    ) {
        /** Serialize the object to a JSON string **/
        fun toJSON(): String {
            return Gson().toJson(
                Info(
                    nearbyDevicesInfo = this.nearbyDevicesInfo,
                    thisDeviceInfo = this.thisDeviceInfo
                )
            )
        }

        fun toBinary(): ByteArray {
            return toRandomSampledBinary()
        }


        @Serializable
        data class SmallInfo(
            val thisDeviceInfo: DeviceInfo,
            val nearbyDevicesInfo: Pair<String,DeviceInfo>?
        )
        {
            fun toInfo(): Info {
                val nearbyDevicesInfoMap =
                    if (nearbyDevicesInfo != null)
                        mutableMapOf(nearbyDevicesInfo)
                    else
                        mutableMapOf()
                return Info(nearbyDevicesInfoMap, thisDeviceInfo)
            }

            companion object {
                fun fromInfoWithRandomSampling(info: Info): SmallInfo {
                    val thisDeviceInfo = info.thisDeviceInfo
                    val nearbyDevicesInfo = info.nearbyDevicesInfo.toList()

                    val randomNearbyDevicesInfo = try {
                        val deviceInfo = nearbyDevicesInfo.random()
                        deviceInfo.second.knownDistances = deviceInfo.second.knownDistances
                            ?.take(1)
                            ?.toMutableList()
                        deviceInfo
                    } catch (e: NoSuchElementException) {
                        null
                    }

                    return SmallInfo(thisDeviceInfo, randomNearbyDevicesInfo)
                }
            }
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun toRandomSampledBinary(): ByteArray {
            SmallInfo.fromInfoWithRandomSampling(this).let {
                return Cbor.encodeToByteArray(it)
            }
        }

        @Synchronized
        fun merge(srcAddress: String, info: Info) {
            for ((address, deviceInfo) in info.nearbyDevicesInfo) {
                val currentDeviceInfo = nearbyDevicesInfo[address]
                val newDeviceInfo = if (currentDeviceInfo == null) {
                    deviceInfo
                } else {
                    deviceInfo.merge(currentDeviceInfo)
                }
                nearbyDevicesInfo[address] = newDeviceInfo
            }

            if (nearbyDevicesInfo[srcAddress] == null) {
                nearbyDevicesInfo[srcAddress] = info.thisDeviceInfo
            } else {
                nearbyDevicesInfo[srcAddress] = info.thisDeviceInfo.merge(nearbyDevicesInfo[srcAddress]!!)
            }
        }
        companion object {

            /** Deserialize the object from a JSON string **/
            fun fromJSON(json: String): Info {
                val info = Gson().fromJson(json, Info::class.java)
                return info
            }

            fun fromBinary(bytes: ByteArray): Info {
                return fromRandomSampledBinary(bytes)
            }

            @OptIn(ExperimentalSerializationApi::class)
            fun fromRandomSampledBinary(bytes: ByteArray): Info {
                val smallInfo = Cbor.decodeFromByteArray<SmallInfo>(bytes)
                return smallInfo.toInfo()
            }
        }
    }
    companion object {
        /** This object stores the information about nearby devices and this device **/
        var info = Info()
        const val UPDATE_INTERVAL_MS = 10000L
        const val TYPICAL_TX_POWER = -59
    }
}