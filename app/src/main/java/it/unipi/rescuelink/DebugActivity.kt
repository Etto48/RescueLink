package it.unipi.rescuelink

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.maps.model.LatLng
import it.unipi.rescuelink.adhocnet.DeviceInfo
import it.unipi.rescuelink.trilateration.ECEF
import it.unipi.rescuelink.trilateration.Trilateration
import kotlin.math.pow

class DebugActivity : AppCompatActivity() {

    private var adapter: ArrayAdapter<String>? = null

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
            adapter?.add(deviceToString(address,deviceInfo))
        }
    }

    private fun deviceToString(address: String, deviceInfo: DeviceInfo): String {
        val a = ECEF.latLngToECEF(RescueLink.info.thisDeviceInfo.getExactPosition()!!)
        val b = if (deviceInfo.getExactPosition() != null && RescueLink.info.thisDeviceInfo.getExactPosition() != null) {
            ECEF.latLngToECEF(deviceInfo.getExactPosition()!!)
        } else if (deviceInfo.knownDistances != null) {
            val points = mutableListOf<LatLng>()
            val ranges = mutableListOf<Double>()
            if (deviceInfo.knownDistances!!.size < 3) {
                a
            } else {
                for (measurement in deviceInfo.knownDistances!!) {
                    points.add(measurement.measurementPosition.toLatLng())
                    ranges.add(measurement.estimatedDistance)
                }
                val trilateration = Trilateration(points, ranges)
                ECEF.latLngToECEF(trilateration.locate())
            }
        }
        else {
            a
        }

        val distance =
            ((a.x - b.x).pow(2) +
                    (a.y - b.y).pow(2) +
                    (a.z - b.z).pow(2))
                .pow(0.5)

        val sar = if (deviceInfo.getIsSAR()) "SAR" else "PV"

        return "$address $sar ${deviceInfo.getDeviceName()} ${distance}m"
    }
}