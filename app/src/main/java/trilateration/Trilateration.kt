package trilateration

import com.google.android.gms.maps.model.LatLng
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.util.Pair
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Trilateration(private val points: List<LatLng>, private val distances: List<Double>) {

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000 // Raggio della Terra in metri
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun residualsAndJacobian(x: RealVector): Pair<RealVector, RealMatrix>? {
        val lat = x.getEntry(0)
        val lon = x.getEntry(1)

        val residuals = DoubleArray(points.size)
        val jacobian = Array(points.size) { DoubleArray(2) }

        for (i in points.indices) {
            val point = points[i]
            val calculatedDistance = haversineDistance(lat, lon, point.latitude, point.longitude)
            residuals[i] = calculatedDistance - distances[i]

            val R = 6371000 // Raggio della Terra in metri
            val latRad1 = Math.toRadians(lat)
            val lonRad1 = Math.toRadians(lon)
            val latRad2 = Math.toRadians(point.latitude)
            val lonRad2 = Math.toRadians(point.longitude)

            val dLat = latRad2 - latRad1
            val dLon = lonRad2 - lonRad1

            val a = sin(dLat / 2).pow(2.0) + cos(latRad1) * cos(latRad2) * sin(dLon / 2).pow(2.0)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            val dcdlat = (R * cos(latRad2) * sin(dLat / 2)) / sqrt(a * (1 - a))
            val dcdlon = (R * cos(latRad1) * cos(latRad2) * sin(dLon / 2)) / sqrt(a * (1 - a))

            jacobian[i][0] = -dcdlat / (sqrt(1 - a) * sqrt(a))
            jacobian[i][1] = -dcdlon / (sqrt(1 - a) * sqrt(a))
        }

        return Pair(ArrayRealVector(residuals), Array2DRowRealMatrix(jacobian))
    }

    fun locate(): LatLng {
        val initialGuess = doubleArrayOf(points.map { it.latitude }.average(), points.map { it.longitude }.average())
        val initialGuessVector = ArrayRealVector(initialGuess)

        val problem = LeastSquaresBuilder()
            .start(initialGuessVector)
            .model { point -> residualsAndJacobian(point) }
            .target(DoubleArray(distances.size))
            .lazyEvaluation(false)
            .maxEvaluations(1000)
            .maxIterations(1000)
            .build()

        val optimizer = LevenbergMarquardtOptimizer()
        val optimum = optimizer.optimize(problem)

        val resultPoint = optimum.point.toArray()
        return LatLng(resultPoint[0], resultPoint[1])
    }
}