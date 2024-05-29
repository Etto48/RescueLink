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
import it.unipi.location.OnLocationReceivedCallback
import it.unipi.rescuelink.databinding.ActivityMapsBinding
import it.unipi.rescuelink.maps.IconProvider
import it.unipi.rescuelink.maps.PossibleVictimTag
import it.unipi.rescuelink.maps.SarInfoWindowAdapter

class MapsActivity : AppCompatActivity(), OnLocationReceivedCallback, OnMapReadyCallback {

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
        addMyLocation(LatLng(0.0,0.0))
    }

    private fun addPossibleVictim(victim: PossibleVictimTag){
        val markerOpt = MarkerOptions().position(victim.position).icon(IconProvider.getVictimIcon(this,100))
        val marker = mMap.addMarker(markerOpt)
        marker?.snippet = POSSIBLE_VICTIM
        marker?.tag = victim
        possibleVictimMarkers[victim.id] = marker
    }

    private fun updatePossibleVictim(victim: PossibleVictimTag){
        val marker = possibleVictimMarkers[victim.id]
        marker?.position = victim.position
        marker?.tag = victim
    }

    private fun addMyLocation(latLng: LatLng){
        val markerOpt = MarkerOptions().position(latLng).title("Current Location").icon(IconProvider.getSarIcon(this,100))
        myLocationMarker = mMap.addMarker(markerOpt)
        myLocationMarker?.snippet = SAR_OPERATOR
    }

    private fun updateMyLocation(latLng: LatLng) {
        Log.d(TAG, "updateMyLocation: $latLng")
        myLocationMarker!!.position = latLng
    }

    companion object {
        private const val TAG = "MapsActivity"
        const val POSSIBLE_VICTIM = "possible_victim"
        const val SAR_OPERATOR = "sar_operator"
    }

    override fun onLocationReceived(location: LatLng) {
        RescueLink.info.thisDeviceInfo.exactPosition = location
        updateMyLocation(location)
        update()
    }

    private fun update(){
        val nearbyDev = RescueLink.info.nearbyDevicesInfo

        for(dev in nearbyDev){
            val id = dev.key
            val info = dev.value

            var ranges = info.knownDistances?.map {
                it.estimatedDistance
            }
            var positions = info.knownDistances?.map {
                it.measurementPosition
            }

            // TRILATERIZZA LA NUOVA POSIZIONE
            // ###############################
            // ###############################
            // ###############################
            val newPosition = LatLng(0.0,0.0)
            // ###############################
            // ###############################
            // ###############################
            // ##### DEVO UNIRE LE BRANCH ####

            var pv : PossibleVictimTag
            if(info.personalInfo != null){
                val name = info.personalInfo!!.completeName
                val age = info.personalInfo!!.getAge()
                val weight = info.personalInfo!!.weightKg
                val hr = info.personalInfo!!.heartBPM

                pv = PossibleVictimTag(id,name, newPosition, age, weight, hr)
            }
            else{
                pv = PossibleVictimTag(id, newPosition)
            }

            val marker = possibleVictimMarkers[id]
            if(marker == null){
                addPossibleVictim(pv)
            }
            else {
                updatePossibleVictim(pv)
            }

        }
    }
}