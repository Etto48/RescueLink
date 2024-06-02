package it.unipi.rescuelink.maps

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import it.unipi.rescuelink.MapsActivity.Companion.CURRENT_OPERATOR
import it.unipi.rescuelink.MapsActivity.Companion.SAR_OPERATOR
import it.unipi.rescuelink.R

class SarInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(marker: Marker): View? {
        return when (marker.snippet) {
            CURRENT_OPERATOR -> createSimpleMarkerInfoView(
                context.getString(R.string.current_location),
                R.layout.simple_marker_info
            )
            SAR_OPERATOR -> {
                val sar = marker.tag as? SarOperatorTag
                sar?.let { createSarOperatorMarkerInfoView(it) }
            }
            else -> {
                val victim = marker.tag as? PossibleVictimTag
                victim?.let { createPossibleVictimMarkerInfoView(it) }
            }
        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        // Return null to indicate that the default window (white bubble) should be used
        return null
    }

    private fun createSimpleMarkerInfoView(title: String, layoutResId: Int): View {
        val view = LayoutInflater.from(context).inflate(layoutResId, null)
        val titleTextView: TextView = view.findViewById(R.id.title)
        titleTextView.text = title
        return view
    }

    private fun createSarOperatorMarkerInfoView(sar: SarOperatorTag): View {
        val view = LayoutInflater.from(context).inflate(R.layout.sar_operator_marker_info, null)
        val nameTextView: TextView = view.findViewById(R.id.name)
        val deviceNameTextView: TextView = view.findViewById(R.id.device_name)

        nameTextView.text = sar.name
        deviceNameTextView.text = sar.deviceName ?: context.getString(R.string.unknown_device_name)

        return view
    }

    private fun createPossibleVictimMarkerInfoView(victim: PossibleVictimTag): View {
        if (victim.simpleTag) {
            val view = createSimpleMarkerInfoView(
                context.getString(R.string.possible_victim),
                R.layout.simple_marker_info
            )
            val deviceNameTextView: TextView = view.findViewById(R.id.device_name)
            deviceNameTextView.text = victim.deviceName ?: context.getString(R.string.unknown_device_name)
            return view
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.possible_victim_marker_info, null)
            val nameTextView: TextView = view.findViewById(R.id.name)
            val deviceNameTextView: TextView = view.findViewById(R.id.device_name)
            val ageTextView: TextView = view.findViewById(R.id.age)
            val weightTextView: TextView = view.findViewById(R.id.weight)
            val heartRateTextView: TextView = view.findViewById(R.id.heartrate)

            nameTextView.text = victim.name
            deviceNameTextView.text = victim.deviceName ?: context.getString(R.string.unknown_device_name)
            ageTextView.text = StringBuilder()
                .append(context.getString(R.string.tag_age))
                .append(victim.age)
                .toString()
            weightTextView.text = StringBuilder()
                .append(context.getString(R.string.tag_weight))
                .append(victim.weight)
                .append(" kg")
                .toString()
            heartRateTextView.text = StringBuilder()
                .append(context.getString(R.string.tag_heart_rate))
                .append(victim.heartRate)
                .append(" bpm")
                .toString()

            return view
        }
    }
}
