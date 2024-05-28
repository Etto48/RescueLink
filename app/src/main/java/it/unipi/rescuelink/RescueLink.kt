package it.unipi.rescuelink

import android.app.Application
import it.unipi.rescuelink.adhocnet.DeviceInfo

class RescueLink : Application() {
    companion object {
        // A map of nearby devices and their information, indexed by their bluetooth MAC address
        var nearbyDevicesInfo = mutableMapOf<String, DeviceInfo>()
        // The device information of this device
        var thisDeviceInfo = DeviceInfo()
    }
}