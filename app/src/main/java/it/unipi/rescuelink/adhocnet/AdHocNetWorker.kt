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
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.Worker
import androidx.work.WorkerParameters
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
        UUID.fromString("9578d7f6-3fe4-404c-b2cc-841747b6163f"),
        BluetoothGattService.SERVICE_TYPE_PRIMARY
    )

    init {
        infoService.addCharacteristic(infoCharacteristic)
    }

    private var bluetoothGattServer: BluetoothGattServer? = null

    private var context = appContext

    private var connectedGattSet = mutableMapOf<String,BluetoothGatt>()

    private var gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            if (characteristic != null && characteristic.uuid == infoCharacteristic.uuid) {
                try {
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        offset,
                        // TODO: You should send the actual data here in json
                        "[]".toByteArray())
                } catch (e: SecurityException) {
                    Log.e("AdHocNet", "Failed to send response")
                }
            }
        }
    }

    private var scanCallback = object : ScanCallback() {
        private var gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(
                gatt: BluetoothGatt?,
                status: Int,
                newState: Int
            ) {
                super.onConnectionStateChange(gatt, status, newState)
                if (gatt == null || status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e("AdHocNet", "Failed to connect to device ${gatt?.device}")
                    return
                }
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d("AdHocNet", "Connected to device ${gatt.device}")
                    connectedGattSet[gatt.device.address] = gatt
                }
                if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.d("AdHocNet", "Disconnected from device ${gatt.device}")
                    connectedGattSet.remove(gatt.device.address)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (gatt != null && status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("AdHocNet", "Discovered services for device ${gatt.device}")
                    connectedGattSet[gatt.device.address] = gatt
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, value, status)
                if (characteristic.uuid == infoCharacteristic.uuid && status == BluetoothGatt.GATT_SUCCESS) {
                    // TODO: handle the read value
                    Log.d("AdHocNet", "Read value: ${String(value)}")
                }
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result != null) {
                Log.d("AdHocNet", "BLE Device: $result")
                try {
                    result.device.connectGatt(context, false, gattCallback)
                } catch (_: SecurityException) {
                    Log.e("AdHocNet", "Missing BLUETOOTH_CONNECT permission")
                }
            }
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
                    Log.d("AdHocNet", "AdHocNetWorker periodic work")
                    // START PERIODIC WORK
                    try {
                        loop()
                    } catch (e: SecurityException) {
                        Log.e("AdHocNet", "Missing permission")
                        return Result.failure()
                    }
                    // END PERIODIC WORK
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

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    private fun start(bluetoothManager: BluetoothManager, bluetoothAdapter: BluetoothAdapter) {
        Log.d("AdHocNet", "Starting GATT server")
        this.bluetoothGattServer = bluetoothManager.openGattServer(context,gattServerCallback)
        this.bluetoothGattServer!!.addService(infoService)
        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
    }

    private fun loop() {
        for ((address, gatt) in connectedGattSet) {
            gatt.getService(infoService.uuid)?.getCharacteristic(infoCharacteristic.uuid)?.let {
                try {
                    gatt.readCharacteristic(it)
                } catch (e: SecurityException) {
                    Log.e("AdHocNet", "Failed to read characteristic from $address")
                }
            }
        }
    }
}