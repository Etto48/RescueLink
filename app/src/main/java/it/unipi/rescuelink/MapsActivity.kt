package it.unipi.rescuelink

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import it.unipi.location.LocationReceiver
import it.unipi.location.LocationUpdateService
import it.unipi.location.onLocationReceivedCallback
import it.unipi.rescuelink.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), onLocationReceivedCallback, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationReceiver: LocationReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the location receiver
        locationReceiver = LocationReceiver(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(LocationUpdateService.LOCATION_UPDATE_ACTION)
        ContextCompat.registerReceiver(this, locationReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Log.d(TAG, "Starting location update")

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationReceiver)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun updateMap(latLng: LatLng) {
        Log.d(TAG, "Received location update: $latLng")
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).title("Current Location"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }


    companion object {
        private const val TAG = "MapsActivity"
    }

    override fun onLocationReceived(location: LatLng) {
        updateMap(location)
    }
}