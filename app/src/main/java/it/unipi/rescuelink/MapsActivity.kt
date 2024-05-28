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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import it.unipi.location.LocationReceiver
import it.unipi.location.LocationUpdateService
import it.unipi.location.onLocationReceivedCallback
import it.unipi.rescuelink.adhocnet.Position
import it.unipi.rescuelink.databinding.ActivityMapsBinding
import it.unipi.rescuelink.maps.IconProvider
import it.unipi.rescuelink.maps.PossibleVictimTag
import it.unipi.rescuelink.maps.SarInfoWindowAdapter

class MapsActivity : AppCompatActivity(), onLocationReceivedCallback, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationReceiver: LocationReceiver

    private var possibleVictimMarkers = mutableMapOf<String, Marker?>()
    private var myLocationMarker: Marker? = null



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

        val victim = PossibleVictimTag("aa","Paolo Palumbo", LatLng(40.7128, -74.0060), 25, 80, 1000)
        updatePossibleVictim(victim)
    }

    private fun updatePossibleVictim(victim: PossibleVictimTag){
        var marker = possibleVictimMarkers[victim.id]
        if(marker == null){
            val markerOpt = MarkerOptions().position(victim.position).icon(IconProvider.getVictimIcon(this,100))
            marker = mMap.addMarker(markerOpt)
            marker?.snippet = POSSIBLE_VICTIM
            marker?.tag = victim
            possibleVictimMarkers[victim.id] = marker
        }
        else{
            marker.position = victim.position
        }
    }

    private fun updateMyLocation(latLng: LatLng) {
        Log.d(TAG, "updateMyLocation: $latLng")
        if (myLocationMarker == null) {
            val markerOpt = MarkerOptions().position(latLng).title("Current Location").icon(IconProvider.getSarIcon(this,100))
            myLocationMarker = mMap.addMarker(markerOpt)
            myLocationMarker?.snippet = SAR_OPERATOR
        }
        else {
            myLocationMarker!!.position = latLng
        }
    }

    companion object {
        private const val TAG = "MapsActivity"
        const val POSSIBLE_VICTIM = "possible_victim"
        const val SAR_OPERATOR = "sar_operator"
    }

    override fun onLocationReceived(location: LatLng) {
        RescueLink.thisDeviceInfo.exactPosition = Position(location.latitude, location.longitude)
        updateMyLocation(location)
    }
}