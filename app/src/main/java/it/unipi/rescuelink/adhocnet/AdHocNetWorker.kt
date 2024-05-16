package it.unipi.rescuelink.adhocnet

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass.Device
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class AdHocNetWorker(appContext: Context, workerParameters: WorkerParameters) :
    Worker(appContext, workerParameters) {

    var context = appContext
    init {
    }
    public override fun doWork(): Result
    {
        val bluetoothManager : BluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter != null)
        {
            while(true) {
                if (bluetoothAdapter.isEnabled) {
                    Log.d("AdHocNet", "AdHocNetWorker periodic work")
                    // START PERIODIC WORK
                    try
                    {
                        // TODO: Bluetooth ad hoc network code
                           
                    }
                    catch (e: SecurityException)
                    {
                        Log.e("AdHocNet", "Missing Bluetooth connect permission")
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
}