package it.unipi.rescuelink.adhocnet

import com.google.android.gms.maps.model.LatLng

class DeviceInfo(
    var exactPosition: LatLng? = null,
    var knownDistances: MutableList<DistanceInfo>? = null,
    var personalInfo: PersonalInfo? = null
)