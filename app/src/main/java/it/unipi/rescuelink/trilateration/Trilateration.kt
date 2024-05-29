package it.unipi.rescuelink.trilateration

import com.google.android.gms.maps.model.LatLng
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver
import com.lemmingapex.trilateration.TrilaterationFunction
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer


class Trilateration(points: List<LatLng>, ranges: List<Double>) {

    private var ecefPoints: MutableList<ECEFCoordinate> = mutableListOf()
    private var distances: DoubleArray
    init {
        for (p in points){
            val ecef = ECEF.latLngToECEF(p)
            ecefPoints.add(ecef)
        }
        distances = ranges.toDoubleArray()
    }

    fun locate(): LatLng {

        val positions = ecefPoints.map { coordinate ->
            doubleArrayOf(coordinate.x, coordinate.y, coordinate.z)
        }.toTypedArray()

        var centroid: DoubleArray
        try {
            val solver = NonLinearLeastSquaresSolver(
                TrilaterationFunction(positions, distances),
                LevenbergMarquardtOptimizer()
            )

            val optimum = solver.solve()
            centroid = optimum.point.toArray()
        }
        catch ( _: java.lang.IllegalArgumentException){
            if (positions.size == 1){
                centroid = doubleArrayOf(positions[0][0], positions[0][1], positions[0][2])
            }
            else throw java.lang.IllegalArgumentException("Need at least one positions")
        }
        val centroidECEF = ECEFCoordinate(centroid[0], centroid[1], centroid[2])
        val centroidLatLng = ECEF.ecefToLatLng(centroidECEF)
        return centroidLatLng
    }

    companion object{
        const val TAG = "Trilateration"
    }
}