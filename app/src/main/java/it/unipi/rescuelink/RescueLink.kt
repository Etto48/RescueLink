package it.unipi.rescuelink

import android.app.Application
import com.google.gson.Gson
import it.unipi.rescuelink.adhocnet.DeviceInfo

class RescueLink : Application() {

    class Info (
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
        companion object {

            /** Deserialize the object from a JSON string **/
            fun fromJSON(json: String): Info {
                val info = Gson().fromJson(json, Info::class.java)
                return info
            }
        }
    }
    companion object {
        /** This object stores the information about nearby devices and this device **/
        var info = Info()
    }
}