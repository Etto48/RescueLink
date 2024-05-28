package it.unipi.rescuelink.adhocnet

class DeviceInfo(
    var exactPosition: Position? = null,
    var knownDistances: MutableList<DistanceInfo>? = null,
    var personalInfo: PersonalInfo? = null
)