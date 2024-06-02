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
import it.unipi.rescuelink.RescueLink
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.UUID

class AdHocNetWorker(appContext: Context, workerParameters: WorkerParameters) :
    Worker(appContext, workerParameters) {
    companion object {
        private const val TAG = "AdHocNet"
    }

    private val infoCharacteristic = BluetoothGattCharacteristic(
        UUID.fromString("9578d7f6-3fe4-404c-b2cc-841747b6163f"),
        BluetoothGattCharacteristic.PROPERTY_READ,
        BluetoothGattCharacteristic.PERMISSION_READ
    )
    private val infoService = BluetoothGattService(
        UUID.fromString("2c376359-addd-4cdf-acec-4fefe531a59d"),
        BluetoothGattService.SERVICE_TYPE_PRIMARY
    )

    private var requestBuffers = mutableMapOf<String, ByteArray>()

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
            if (device == null ||
                characteristic == null ||
                characteristic.uuid != infoCharacteristic.uuid
            ) {
                Log.e(TAG, "Invalid read request")
                return
            }

            if (requestBuffers[device.address] == null) {
                requestBuffers[device.address] = RescueLink.info.toBinary()
                Log.d(TAG, "Allocating buffer for device \"${device.address}\"")
            }

            val serializedInfo = requestBuffers[device.address]!!

            if (offset + 22 >= serializedInfo.size) {
                requestBuffers.remove(device.address)
                Log.d(TAG, "Freeing buffer for device \"${device.address}\"")
            }

            Log.d(TAG, "Sending response to ${device.address} (offset: $offset / ${serializedInfo.size})")

            if (bluetoothGattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    serializedInfo
                        .sliceArray(offset until serializedInfo.size)
                ) != true
            ) {
                Log.e(TAG, "Failed to respond to read request")
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
                Log.d(TAG, "Connected to device \"${gatt.device.name}\"")
                // connectedGattSet[gatt.device.address] = gatt
                gatt.discoverServices()
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from device \"${gatt.device.name}\"")
                connectedGattSet.remove(gatt.device.address)
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (gatt != null && status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Discovered services for device \"${gatt.device.name}\"")
                connectedGattSet[gatt.device.address] = gatt
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.e(TAG, "Write characteristic not implemented")
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        private fun onReadSuccess(gatt: BluetoothGatt, value: ByteArray) {
            try {
                val info = RescueLink.Info.fromBinary(value)
                // TODO: check if gatt.device.address is consistent,
                //  previously, it gave some errors during testing
                RescueLink.info.merge(gatt.device.address, info)
                Log.d(TAG, "Data received from \"${gatt.device?.name}\": ${info.toJSON()}")
            } catch (e: Exception)
            {
                Log.e(TAG, "Failed to parse: $e")
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
                    val gatt = result.device.connectGatt(
                        context,
                        false,
                        gattCallback,
                        BluetoothDevice.TRANSPORT_LE,
                        result.primaryPhy
                    )
                    if (RescueLink.info.thisDeviceInfo.getExactPosition() != null) {
                        val txPower = if (result.txPower == ScanResult.TX_POWER_NOT_PRESENT)
                            RescueLink.TYPICAL_TX_POWER
                        else
                            result.txPower

                        val distance = DistanceInfo.estimateDistance(result.rssi, txPower, true)
                        val distanceInfo = DistanceInfo(distance, Position.fromLatLng(RescueLink.info.thisDeviceInfo.getExactPosition()!!))
                        if (RescueLink.info.nearbyDevicesInfo[gatt.device.address] == null) {
                            val newInfo = DeviceInfo()
                            newInfo.setDeviceName(result.device.name)
                            Log.d(TAG, "New device found: ${result.device.name}")
                            newInfo.addDistanceInfo(distanceInfo)
                            RescueLink.info.nearbyDevicesInfo[gatt.device.address] = newInfo
                        } else {
                            RescueLink.info.nearbyDevicesInfo[gatt.device.address]!!.addDistanceInfo(distanceInfo)
                        }
                    }
                }
            }
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "BLE advertising started")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e(TAG, "BLE advertising failed with error code $errorCode")
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
                    Log.e(TAG, "Missing permission")
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
                        Log.e(TAG, "Missing permission")
                        return Result.failure()
                    }
                } else {
                    Log.e(TAG, "Bluetooth disabled")
                }
                runBlocking {
                    delay(RescueLink.UPDATE_INTERVAL_MS)
                }
            }
        }
        else {
            Log.e(TAG, "This device does not support bluetooth")
            return Result.failure()
        }
    }

    @RequiresPermission(allOf = [
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE])
    private fun start(bluetoothManager: BluetoothManager, bluetoothAdapter: BluetoothAdapter) {
        Log.d(TAG, "Starting GATT server")
        bluetoothGattServer = bluetoothManager.openGattServer(
            context,
            bluetoothGattServerCallback
        )
        if (!infoService.addCharacteristic(infoCharacteristic))
        {
            Log.e(TAG, "Failed to add characteristic to service")
        }
        if (this.bluetoothGattServer?.addService(infoService) != true)
        {
            Log.e(TAG, "Failed to add service to GATT server")
        }
        var advertisingSettingsBuilder = AdvertiseSettings
            .Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            advertisingSettingsBuilder = advertisingSettingsBuilder.setDiscoverable(true)
        }
        Log.i(TAG, "AdHocNetWorker started on device \"${bluetoothAdapter.name}\"")
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
                Log.d(TAG, "Reading characteristic for device \"${gatt.device.name}\"")
                gatt.readCharacteristic(characteristic)
            } else {
                if (service == null) {
                    Log.e(TAG, "Service not found for device \"${gatt.device.name}\"")
                    Log.i(TAG, "Services: ${gatt.services}")
                } else {
                    Log.e(TAG, "Characteristic not found for device \"${gatt.device.name}\"")
                    Log.i(TAG, "Characteristics: ${service.characteristics}")
                }
                gatt.discoverServices()
            }
        }
    }
}