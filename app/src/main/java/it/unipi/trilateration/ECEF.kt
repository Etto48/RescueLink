package it.unipi.trilateration

import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object ECEF {
    // Costanti WGS84
    private const val a = 6378137.0 // Semiasse maggiore in metri
    private const val b = 6356752.314245 // Semiasse minore in metri
    private const val e2 = 1 - (b * b) / (a * a) // Eccentricità al quadrato
    private const val e2Prime = (a * a - b * b) / (b * b) // Seconda eccentricità al quadrato

    fun latLngToECEF(latLng: LatLng, altitude: Double = 0.0): ECEFCoordinate {
        // Converti latitudine e longitudine in radianti
        val phi = Math.toRadians(latLng.latitude)
        val lambda = Math.toRadians(latLng.longitude)

        // Calcola il raggio della curvatura nel primo verticale
        val N = a / sqrt(1 - e2 * sin(phi) * sin(phi))

        // Calcola le coordinate ECEF
        val x = (N + altitude) * cos(phi) * cos(lambda)
        val y = (N + altitude) * cos(phi) * sin(lambda)
        val z = ((1 - e2) * N + altitude) * sin(phi)

        return ECEFCoordinate(x, y, z)
    }

    fun ecefToLatLng(ecef: ECEFCoordinate): LatLng {
        with(ecef) {
            val p = sqrt(x * x + y * y)
            val theta = atan2(z * a, p * b)

            val latitude = atan2(z + e2Prime * b * sin(theta).pow(3.0), p - e2 * a * cos(theta).pow(3.0))
            val longitude = atan2(y, x)

            return LatLng(Math.toDegrees(latitude), Math.toDegrees(longitude))
        }
    }
}