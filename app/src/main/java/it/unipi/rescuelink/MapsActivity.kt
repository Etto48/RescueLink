package it.unipi.rescuelink

import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import it.unipi.location.LocationReceiver
import it.unipi.location.LocationUpdateService
import it.unipi.location.onLocationReceivedCallback
import it.unipi.rescuelink.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), onLocationReceivedCallback, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationReceiver: LocationReceiver

    private var possibleVictims = mutableMapOf<String, Marker?>()

    private var myLocationMarker: Marker? = null

    private val victimIcon: BitmapDescriptor by lazy{
        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.pv_indicator)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
        BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    private val sarIcon: BitmapDescriptor by lazy{
        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.sar_indicator)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
        BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }
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
        mMap.setInfoWindowAdapter(SarInfoWindowAdapter(this))

        val victim = PossibleVictim("aa","Paolo Palumbo", LatLng(40.7128, -74.0060), 25, 80, 1000)
        updatePossibleVictim(victim)
    }

    private fun updatePossibleVictim(victim: PossibleVictim){
        var marker = possibleVictims[victim.id]
        if(marker == null){
            val markerOpt = MarkerOptions().position(victim.position).icon(victimIcon)
            marker = mMap.addMarker(markerOpt)
            marker?.tag = victim
            possibleVictims[victim.id] = marker
        }
        else{
            marker.position = victim.position
        }
    }

    private fun updateMyLocation(latLng: LatLng) {
        Log.d(TAG, "updateMyLocation: $latLng")
        if (myLocationMarker == null) {
            val markerOpt = MarkerOptions().position(latLng).title("Current Location").icon(sarIcon)
            myLocationMarker = mMap.addMarker(markerOpt)
            myLocationMarker?.snippet = "SAROperator"
        }
        else {
            myLocationMarker!!.position = latLng
        }
    }

    companion object {
        private const val TAG = "MapsActivity"
        private const val targetSize = 100
    }

    override fun onLocationReceived(location: LatLng) {
        updateMyLocation(location)
    }
}