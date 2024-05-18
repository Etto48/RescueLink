package it.unipi.rescuelink

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import it.unipi.location.LocationUpdateService

class MainActivity : AppCompatActivity() {

    private lateinit var locationService: LocationUpdateService
    private var isBound = false
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
        button.setOnClickListener {v -> this.testService(v)}

        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        bindService(serviceIntent, connection, BIND_AUTO_CREATE)
        startService(serviceIntent)
        val pos_tag = findViewById<TextView>(R.id.position)
        pos_tag.text = "0"

        //val workRequest = PeriodicWorkRequestBuilder<LocationWorker>(5, TimeUnit.SECONDS).build()
        //WorkManager.getInstance(this).enqueue(workRequest)
        //pos_tag.text = locationService.test
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

    private fun testService(v: View?){
        val pos_tag = findViewById<TextView>(R.id.position)
        //locationService.getCurrentLocation()
        //val location = locationService.get_location()

        //Log.d("LocationService", "Location: $location")
        //locationService.location
        //pos_tag.text = locationService.get_location().toString()
    }

}