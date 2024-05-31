package it.unipi.rescuelink.maps

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import it.unipi.rescuelink.MapsActivity.Companion.SAR_OPERATOR
import it.unipi.rescuelink.R

class SarInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker): View? {

        if(p0.snippet == SAR_OPERATOR){
            val view = LayoutInflater.from(context).inflate(
                R.layout.simple_marker_info, null
            )

            view.findViewById<TextView>(
                R.id.title
            ).text = context.getString(R.string.current_location)

            return view
        }
        else {
            // 1. Get tag
            val victim = p0.tag as PossibleVictimTag

            if(victim.simpleTag){
                val view = LayoutInflater.from(context).inflate(
                    R.layout.simple_marker_info, null
                )

                view.findViewById<TextView>(
                    R.id.title
                ).text = context.getString(R.string.possible_victim)

                view.findViewById<TextView>(
                    R.id.device_name
                ).text = victim.deviceName ?: context.getString(R.string.unknown_device_name)

                return view
            }


            // 2. Inflate view and set title, address, and rating
            val view = LayoutInflater.from(context).inflate(
                R.layout.possible_victim_marker_info, null
            )

            view.findViewById<TextView>(
                R.id.name
            ).text = victim.name

            view.findViewById<TextView>(
                R.id.device_name
            ).text = victim.deviceName ?: context.getString(R.string.unknown_device_name)

            view.findViewById<TextView>(
                R.id.age
            ).text = StringBuilder()
                .append(context.getString(R.string.tag_age))
                .append(victim.age)

            view.findViewById<TextView>(
                R.id.weight
            ).text = StringBuilder()
                .append(context.getString(R.string.tag_weight))
                .append(victim.weight).append(" kg")

            view.findViewById<TextView>(
                R.id.heartrate
            ).text = StringBuilder()
                .append(context.getString(R.string.tag_heart_rate))
                .append(victim.heartRate).append(" bpm")

            return view
        }
    }

    override fun getInfoWindow(p0: Marker): View? {
        // Return null to indicate that the
        // default window (white bubble) should be used
        return null
    }
}