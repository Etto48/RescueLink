package it.unipi.rescuelink

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class SarInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker): View? {

        if(p0.snippet == "SAROperator"){
            return LayoutInflater.from(context).inflate(
                R.layout.current_location_info, null
            )
        }
        // 1. Get tag
        val victim = p0.tag as PossibleVictim


        // 2. Inflate view and set title, address, and rating
        val view = LayoutInflater.from(context).inflate(
            R.layout.possible_victim_info, null
        )
        view.findViewById<TextView>(
            R.id.name
        ).text = victim.name

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

    override fun getInfoWindow(p0: Marker): View? {
        // Return null to indicate that the
        // default window (white bubble) should be used
        return null
    }
}