package it.unipi.rescuelink

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class DebugActivity : AppCompatActivity() {

    var adapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_debug)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.debug)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listView = findViewById<ListView>(R.id.debug_list)
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        listView.adapter = adapter

        val debugView = findViewById<SwipeRefreshLayout>(R.id.debug)
        debugView.setOnRefreshListener {
            update()
            debugView.isRefreshing = false
        }

        update()
    }

    private fun update() {
        adapter?.clear()
        for ((address, deviceInfo) in RescueLink.info.nearbyDevicesInfo) {
            adapter?.add("${address}->${deviceInfo.deviceName} (${deviceInfo.knownDistances?.size ?: 0})")
        }
    }
}