package it.unipi.rescuelink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.model.LatLng
import it.unipi.trilateration.ECEF
import it.unipi.trilateration.Trilateration

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
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {v -> this.testTrilateration(v)}
    }

    private fun changeToMapsView(v: View?)
    {
        Log.d(null, "Changing to map view")
        startActivity(Intent(this, MapsActivity::class.java))
    }

    private fun testTrilateration(v: View?){
        val points = listOf(
            LatLng(43.8433, 10.5031),
            LatLng(43.8419, 10.4997),
            LatLng(43.8421, 10.5052),
        )
        val distances = listOf(100.0, 200.0, 110.0)

        val p = points[0]
        Log.d(TAG, "Points: $p")
        val transform = ECEF.latLngToECEF(p)
        Log.d(TAG, "Origin: $transform")
        val anti = ECEF.ecefToLatLng(transform)
        Log.d(TAG, "Anti: $anti")

        val t2 = Trilateration(points, distances)
        val location2 = t2.locate()
        Log.d(TAG, "Location2: ${location2.latitude}, ${location2.longitude}")

    }

    companion object {
        private const val TAG = "MainActivity"
    }
}