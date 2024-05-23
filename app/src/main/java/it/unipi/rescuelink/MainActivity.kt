package it.unipi.rescuelink

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Duration

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
        val button_mapsactivity = findViewById<Button>(R.id.button_mapsactivity)
        button_mapsactivity.setOnClickListener {v -> this.changeToMapsView(v)}

        val button_adhocnetwork = findViewById<Button>(R.id.button_adhocnetwork)
        button_adhocnetwork.setOnClickListener {v -> this.startAdHocNetwork(v)}

        val button_permissions = findViewById<Button>(R.id.button_permissions)
        button_permissions.setOnClickListener {v -> this.getPermissions(v)}
    }

    private fun changeToMapsView(v: View?)
    {
        Log.d(null, "Changing to map view")
        startActivity(Intent(this, MapsActivity::class.java))
    }

    private fun getPermissions(v: View?)
    {
        var permissionSet = arrayOf<String>()
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
                Toast.makeText(this, "Permissions $permission granted", Toast.LENGTH_LONG)
                    .show()
            }
            else {
                Toast.makeText(this, "Permission $permission already granted", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun startAdHocNetwork(v: View?)
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
            val workRequest = PeriodicWorkRequestBuilder<it.unipi.rescuelink.adhocnet.AdHocNetWorker>(
                Duration.ofSeconds(900)).build()
            WorkManager
                .getInstance(applicationContext)
                .enqueueUniquePeriodicWork("AdHocNetwork", ExistingPeriodicWorkPolicy.KEEP, workRequest)
        }
    }
}