package it.unipi.rescuelink

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonMapsActivity = findViewById<Button>(R.id.button_mapsactivity)
        buttonMapsActivity.setOnClickListener {this.changeToMapsView()}

        val buttonAdHocNetwork = findViewById<Button>(R.id.button_adhocnetwork)
        buttonAdHocNetwork.setOnClickListener {this.startAdHocNetwork()}

        getPermissions()
    }

    private fun changeToMapsView()
    {
        Log.d(null, "Changing to map view")
        startActivity(Intent(this, MapsActivity::class.java))
    }

    private fun getPermissions()
    {
        val permissionSet: Array<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionSet = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            permissionSet = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        else
        {
            permissionSet = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        permissionSet.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission), 1
                )
            }
        }
    }

    private fun startAdHocNetwork()
    {
        val bluetoothManager : BluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled)
            {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "Consider enabling permissions before starting the network", Toast.LENGTH_LONG).show()
                }
                else {
                    startActivity(enableIntent)
                }
            }
            Log.d("AdHocNet", "Starting ad-hoc network")
            val workRequest = OneTimeWorkRequestBuilder<it.unipi.rescuelink.adhocnet.AdHocNetWorker>().build()
            WorkManager
                .getInstance(applicationContext)
                .enqueueUniqueWork("AdHocNet", ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}