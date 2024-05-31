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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import it.unipi.rescuelink.adhocnet.AdHocNetWorker
import it.unipi.rescuelink.location.LocationUpdateWorker
import java.time.Duration

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted: Map<String, @JvmSuppressWildcards Boolean> ->
        if (isGranted.all { it.value }) {
            startBackgroundWorkers()
        } else {
            for (permission in isGranted) {
                if (!permission.value) {
                    Toast.makeText(this, "Permission ${permission.key} not granted", Toast.LENGTH_LONG).show()
                }
            }

            Toast.makeText(this, "All permissions are required to use the app", Toast.LENGTH_LONG).show()
            finish()
        }
    }
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
        buttonMapsActivity.setOnClickListener {changeToMapsView()}

        val buttonUserInfoActivity = findViewById<Button>(R.id.button_infoactivity)
        buttonUserInfoActivity.setOnClickListener {changeToUserInfoView()}

        val buttonDebugActivity = findViewById<Button>(R.id.button_debugactivity)
        buttonDebugActivity.setOnClickListener {changeToDebugView()}

    }

    override fun onStart() {
        super.onStart()
        getPermissions()
    }

    private fun changeToUserInfoView() {
        Log.d(null, "Changing to user info view")
        try {
            startActivity(Intent(this, UserInfoActivity::class.java))
        }
        catch (e: Exception){
            Log.e("MainActivity", e.message.toString())
        }
    }

    private fun changeToMapsView()
    {
        Log.d(null, "Changing to map view")
        startActivity(Intent(this, MapsActivity::class.java))
    }

    private fun changeToDebugView()
    {
        Log.d(null, "Changing to debug view")
        startActivity(Intent(this, DebugActivity::class.java))
    }

    private fun getPermissions()
    {
        var permissionSet: Array<String>
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

        permissionSet = permissionSet.filter {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionSet.isEmpty())
        {
            startBackgroundWorkers()
        }
        else
        {
            requestPermissionLauncher.launch(permissionSet)
        }
    }

    private fun startBackgroundWorkers()
    {
        startAdHocNetwork()
        startLocationUpdate()
        Toast.makeText(this, "Background workers started", Toast.LENGTH_LONG).show()
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
            val workRequest =
                PeriodicWorkRequestBuilder<AdHocNetWorker>(
                    Duration.ofSeconds(15*60)
                ).build()
            WorkManager
                .getInstance(applicationContext)
                .enqueueUniquePeriodicWork("AdHocNet", ExistingPeriodicWorkPolicy.KEEP, workRequest)
        }
    }

    private fun startLocationUpdate()
    {
        Log.d("AdHocNet", "Starting ad-hoc network")
        val workRequest =
            PeriodicWorkRequestBuilder<LocationUpdateWorker>(
                Duration.ofSeconds(15*60)
            ).build()
        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork("LocationUpdate", ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }
}