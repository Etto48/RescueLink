package it.unipi.rescuelink.adhocnet

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.UUID

class AdHocNetWorker(appContext: Context, workerParameters: WorkerParameters) :
    Worker(appContext, workerParameters) {

    val infoCharacteristic = BluetoothGattCharacteristic(
        UUID.fromString("9578d7f6-3fe4-404c-b2cc-841747b6163f"),
        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
        BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    private var context = appContext

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
                // TODO: handle read request
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            if (characteristic != null && characteristic.uuid == infoCharacteristic.uuid) {
                // TODO: handle write request
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
                    // TODO: handle error
                    Log.e("AdHocNet", "Failed to connect to device ${gatt?.device}")
                    return
                }

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    // TODO: handle the new connection
                    Log.d("AdHocNet", "Connected to device ${gatt.device}")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (gatt != null && status == BluetoothGatt.GATT_SUCCESS) {
                    Toast.makeText(context, "Services discovered ${gatt.services}", Toast.LENGTH_LONG).show()
                    Log.d("AdHocNet", "Services discovered ${gatt.services}")
                }
                else {
                    Log.e("AdHocNet", "Failed to discover services")
                }
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result != null) {
                Log.d("AdHocNet", "BLE Device: $result")
                try {
                    result.device.connectGatt(context, true, gattCallback)
                }
                catch (_: SecurityException)
                {
                    Log.e("AdHocNet", "Missing BLUETOOTH_CONNECT permission")
                }
            }
        }
    }
    override fun doWork(): Result
    {
        val bluetoothManager : BluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter != null)
        {
            if (bluetoothAdapter.isEnabled)
            {
                try {
                    start(bluetoothManager, bluetoothAdapter)
                }
                catch (e: SecurityException)
                {
                    Log.e("AdHocNet", "Missing permission")
                    return Result.failure()
                }
            }
            else
            {
                return Result.failure()
            }

            while(true) {
                if (bluetoothAdapter.isEnabled) {
                    Log.d("AdHocNet", "AdHocNetWorker periodic work")
                    // START PERIODIC WORK
                    try
                    {
                        loop(bluetoothManager, bluetoothAdapter)
                    }
                    catch (e: SecurityException)
                    {
                        Log.e("AdHocNet", "Missing permission")
                        return Result.failure()
                    }
                    // END PERIODIC WORK
                }
                else
                {
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
    private fun start(bluetoothManager: BluetoothManager, bluetoothAdapter: BluetoothAdapter)
    {
        Log.d("AdHocNet", "Starting GATT server")
        bluetoothManager.openGattServer(context,gattServerCallback)
        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
    }

    private fun loop(bluetoothManager: BluetoothManager, bluetoothAdapter: BluetoothAdapter)
    {

    }
}