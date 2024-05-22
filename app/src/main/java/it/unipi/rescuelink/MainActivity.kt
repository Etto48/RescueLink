package it.unipi.rescuelink

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import it.unipi.location.LocationReceiver
import it.unipi.location.LocationUpdateService

class MainActivity : AppCompatActivity() {

    private lateinit var locationService: LocationUpdateService
    private var isBound = false
    private lateinit var locationReceiver: LocationReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {v -> this.changeToMapsView(v)}

        val pos_tag = findViewById<TextView>(R.id.position)
        pos_tag.text = "0"

        // Start the location service
        // Refine this part
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val serviceIntent = Intent(this, LocationUpdateService::class.java)
            bindService(serviceIntent, connection, BIND_AUTO_CREATE)
            startService(serviceIntent)
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            Log.d(TAG, "Permission not granted")
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationUpdateService.LocalBinder
            isBound = true
            locationService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    private fun changeToMapsView(v: View?)
    {
        Log.d(null, "Changing to map view")
        startActivity(Intent(this, MapsActivity::class.java))
    }

    private fun changeToLocationView(v: View?){
        Log.d(null, "Changing to location view")
        startActivity(Intent(this, LocationActivity::class.java))
    }


    companion object {
        private const val TAG = "MainActivity"
    }

}