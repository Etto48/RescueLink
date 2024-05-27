package trilateration

import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


object ECEF {
    fun latLngToECEF(latLng: LatLng, altitude: Double = 0.0): ECEFCoordinate {
        // Costanti WGS84
        val a = 6378137.0 // Semiasse maggiore in metri
        val b = 6356752.314245 // Semiasse minore in metri
        val e2 = 1 - (b * b) / (a * a) // Eccentricità al quadrato

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
        val x = ecef.x
        val y = ecef.y
        val z = ecef.z

        // Costanti WGS84
        val a = 6378137.0 // Semiasse maggiore in metri
        val b = 6356752.314245 // Semiasse minore in metri
        val e2 = 1 - (b * b) / (a * a) // Eccentricità al quadrato

        // Calcola longitudine
        val longitude = atan2(y, x)

        // Calcola p
        val p = sqrt(x * x + y * y)

        // Itera per la latitudine
        var latitude = atan2(z, p * (1 - e2))
        var N: Double
        var latitudePrev: Double

        do {
            latitudePrev = latitude
            N = a / sqrt(1 - e2 * sin(latitude) * sin(latitude))
            latitude = atan2(z + e2 * N * sin(latitude), p)
        } while (abs(latitude - latitudePrev) > 1e-12)

        return LatLng(Math.toDegrees(latitude), Math.toDegrees(longitude))
    }
}