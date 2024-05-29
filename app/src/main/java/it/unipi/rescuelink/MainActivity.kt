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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.model.LatLng
import it.unipi.rescuelink.location.LocationReceiver
import it.unipi.rescuelink.location.LocationUpdateService
import it.unipi.rescuelink.trilateration.Trilateration

class MainActivity : AppCompatActivity() {

    private lateinit var locationService: LocationUpdateService
    private var isBound = false
    private lateinit var locationReceiver: LocationReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {v -> this.testTrilateration(v)}

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

    private fun testTrilateration(v: View?){
        val points = listOf(
            LatLng(43.8433, 10.5031),
        )
        val distances = listOf(100.0)

        val t = Trilateration(points, distances)
        try {
            val location2 = t.locate()
            Log.d(TAG, "Location2: ${location2.latitude}, ${location2.longitude}")
        }
        catch (e: Exception) {
            Log.e(TAG, "Error locating location", e)
        }

    }

    companion object {
        private const val TAG = "MainActivity"
    }

}