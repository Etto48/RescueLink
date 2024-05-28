package it.unipi.rescuelink.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import it.unipi.rescuelink.R

object IconProvider {

    // Private variables to store the icons (consider using lateinit if initialization is non-trivial)
    private var victimIcon: BitmapDescriptor? = null
    private var sarIcon: BitmapDescriptor? = null
    private const val SIZE = 100


    // Public static methods to access the icons with proper null checks
    fun getVictimIcon(context: Context, targetSize: Int = SIZE): BitmapDescriptor? {
        if (victimIcon == null) {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pv_indicator)
            victimIcon = BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
            )
        }
        return victimIcon
    }

    fun getSarIcon(context: Context, targetSize: Int = SIZE): BitmapDescriptor? {
        if (sarIcon == null) {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sar_indicator)
            sarIcon = BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
            )
        }
        return sarIcon
    }
}
