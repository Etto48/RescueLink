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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
    }

    private fun changeToMapsView(v: View?)
    {
        Log.d(null, "Changing to map view")
        startActivity(Intent(this, MapsActivity::class.java))
    }

    private fun startAdHocNetwork(v: View?)
    {
        val bluetoothManager : BluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled)
            {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 2)
                    }
                }
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivity(enableIntent)
            }
            Log.d(null, "Starting ad-hoc network")
            val workRequest = PeriodicWorkRequestBuilder<it.unipi.rescuelink.adhocnet.AdHocNetWorker>(
                Duration.ofSeconds(900)).build()
            WorkManager
                .getInstance(applicationContext)
                .enqueueUniquePeriodicWork("AdHocNetwork", ExistingPeriodicWorkPolicy.KEEP, workRequest)
        }
    }
}