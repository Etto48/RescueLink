package it.unipi.rescuelink.adhocnet

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.JsonSyntaxException
import it.unipi.rescuelink.RescueLink
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.UUID

class AdHocNetWorker(appContext: Context, workerParameters: WorkerParameters) :
    Worker(appContext, workerParameters) {

    private val infoCharacteristic = BluetoothGattCharacteristic(
        UUID.fromString("9578d7f6-3fe4-404c-b2cc-841747b6163f"),
        BluetoothGattCharacteristic.PROPERTY_READ,
        BluetoothGattCharacteristic.PERMISSION_READ
    )
    private val infoService = BluetoothGattService(
        UUID.fromString("2c376359-addd-4cdf-acec-4fefe531a59d"),
        BluetoothGattService.SERVICE_TYPE_PRIMARY
    )

    private var context = appContext

    private var connectedGattSet = mutableMapOf<String,BluetoothGatt>()

    private var bluetoothGattServer: BluetoothGattServer? = null

    private val bluetoothGattServerCallback = object : BluetoothGattServerCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            val infoJSON = RescueLink.info.toJSON()
            if (device != null && characteristic != null && characteristic.uuid == infoCharacteristic.uuid) {
                if (bluetoothGattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    infoJSON.toByteArray()) != true
                ) {
                    Log.e("AdHocNet", "Failed to respond to read request")
                }
            } else {
                Log.e("AdHocNet", "Failed to respond to read characteristic")
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(
            gatt: BluetoothGatt?,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(gatt, status, newState)
            if (gatt == null || status != BluetoothGatt.GATT_SUCCESS) {
                return
            }
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d("AdHocNet", "Connected to device \"${gatt.device.name}\"")
                // connectedGattSet[gatt.device.address] = gatt
                gatt.discoverServices()
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d("AdHocNet", "Disconnected from device \"${gatt.device.name}\"")
                connectedGattSet.remove(gatt.device.address)
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (gatt != null && status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("AdHocNet", "Discovered services for device \"${gatt.device.name}\"")
                connectedGattSet[gatt.device.address] = gatt
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.e("AdHocNet", "Write characteristic not implemented")
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        private fun onReadSuccess(gatt: BluetoothGatt, value: ByteArray) {
            try {
                val info = RescueLink.Info.fromJSON(String(value))
                // TODO: check if gatt.device.address is consistent,
                //  previously, it gave some errors during testing
                RescueLink.info.merge(gatt.device.address, info)
                Log.d("AdHocNet", "Data received from \"${gatt.device?.name}\": ${String(value)}")
            } catch (e: JsonSyntaxException)
            {
                Log.e("AdHocNet", "Failed to parse received JSON: $e")
            }
        }

        @Deprecated("This method is only called on API < 33", ReplaceWith(
            "super.onCharacteristicRead(gatt, characteristic, status)",
            "android.bluetooth.BluetoothGattCallback"))
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (gatt != null && characteristic != null && characteristic.uuid == infoCharacteristic.uuid && status == BluetoothGatt.GATT_SUCCESS) {
                onReadSuccess(gatt, characteristic.value)
            }
        }

        // THIS *** IS ONLY CALLED ON API >= 33
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            if (characteristic.uuid == infoCharacteristic.uuid && status == BluetoothGatt.GATT_SUCCESS) {
                onReadSuccess(gatt, value)
            }
        }
    }

    private val scanCallback = object : ScanCallback() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result != null) {
                if (!connectedGattSet.containsKey(result.device.address)) {
                    result.device.connectGatt(context, false, gattCallback)
                    if (RescueLink.info.thisDeviceInfo.exactPosition != null) {
                        val distance = DistanceInfo.estimateDistance(result.rssi, result.txPower)
                        val distanceInfo = DistanceInfo(distance, RescueLink.info.thisDeviceInfo.exactPosition!!)
                        if (RescueLink.info.nearbyDevicesInfo[result.device.address] == null) {
                            val newInfo = DeviceInfo()
                            newInfo.addDistanceInfo(distanceInfo)
                            RescueLink.info.nearbyDevicesInfo[result.device.address] = newInfo
                        } else {
                            RescueLink.info.nearbyDevicesInfo[result.device.address]!!.addDistanceInfo(distanceInfo)
                        }
                    }
                }
            }
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("AdHocNet", "BLE advertising started")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e("AdHocNet", "BLE advertising failed with error code $errorCode")
        }
    }

    override fun doWork(): Result {
        val bluetoothManager : BluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled) {
                try {
                    start(bluetoothManager, bluetoothAdapter)
                } catch (e: SecurityException) {
                    Log.e("AdHocNet", "Missing permission")
                    return Result.failure()
                }
            } else {
                return Result.failure()
            }

            while(true) {
                if (bluetoothAdapter.isEnabled) {
                    try {
                        loop()
                    } catch (e: SecurityException) {
                        Log.e("AdHocNet", "Missing permission")
                        return Result.failure()
                    }
                } else {
                    Log.e("AdHocNet", "Bluetooth disabled")
                }
                runBlocking {
                    delay(15000)
                }
            }
        }
        else {
            Log.e("AdHocNet", "This device does not support bluetooth")
            return Result.failure()
        }
    }

    @RequiresPermission(allOf = [
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE])
    private fun start(bluetoothManager: BluetoothManager, bluetoothAdapter: BluetoothAdapter) {
        Log.d("AdHocNet", "Starting GATT server")
        bluetoothGattServer = bluetoothManager.openGattServer(
            context,
            bluetoothGattServerCallback
        )
        if (!infoService.addCharacteristic(infoCharacteristic))
        {
            Log.e("AdHocNet", "Failed to add characteristic to service")
        }
        if (!this.bluetoothGattServer!!.addService(infoService))
        {
            Log.e("AdHocNet", "Failed to add service to GATT server")
        }
        var advertisingSettingsBuilder = AdvertiseSettings
            .Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            advertisingSettingsBuilder = advertisingSettingsBuilder.setDiscoverable(true)
        }
        Log.i("AdHocNet", "AdHocNetWorker started on device \"${bluetoothAdapter.name}\"")
        bluetoothAdapter.bluetoothLeAdvertiser.startAdvertising(
            advertisingSettingsBuilder.build(),
            AdvertiseData
                .Builder()
                .setIncludeTxPowerLevel(true)
                .setIncludeDeviceName(true)
                .build(),
            advertiseCallback
        )
        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun loop() {
        for ((_, gatt) in connectedGattSet) {
            val service = gatt.getService(infoService.uuid)
            val characteristic = service?.getCharacteristic(infoCharacteristic.uuid)
            if (characteristic != null) {
                Log.d("AdHocNet", "Reading characteristic for device \"${gatt.device.name}\"")
                gatt.readCharacteristic(characteristic)
            } else {
                if (service == null) {
                    Log.e("AdHocNet", "Service not found for device \"${gatt.device.name}\"")
                    Log.i("AdHocNet", "Services: ${gatt.services}")
                } else {
                    Log.e("AdHocNet", "Characteristic not found for device \"${gatt.device.name}\"")
                    Log.i("AdHocNet", "Characteristics: ${service.characteristics}")
                }
                gatt.discoverServices()
            }
        }
    }
}