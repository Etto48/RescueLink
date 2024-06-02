package it.unipi.rescuelink.UserInfo

import android.content.Context
import it.unipi.rescuelink.adhocnet.PersonalInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserInfoManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("PersonalInfo", Context.MODE_PRIVATE)

    var name: String?
        get() = sharedPreferences.getString("name", "")
        set(value) = sharedPreferences.edit().putString("name", value).apply()

    var surname: String?
        get() = sharedPreferences.getString("surname", "")
        set(value) = sharedPreferences.edit().putString("surname", value).apply()

    var birthDate: String?
        get() = sharedPreferences.getString("birthDate", "")
        set(value) = sharedPreferences.edit().putString("birthDate", value).apply()

    var weight: String?
        get() = sharedPreferences.getString("weight", "")
        set(value) = sharedPreferences.edit().putString("weight", value).apply()

    fun loadPersonalInfo(): PersonalInfo {
        val fullName = "${name.orEmpty()} ${surname.orEmpty()}"
        val weightValue = weight?.toDoubleOrNull() ?: 0.0
        val birthDateValue = birthDate?.let { stringToDate(it) } ?: Date()

        return PersonalInfo(fullName, 0.0, weightValue, birthDateValue.toString())
    }

    private fun stringToDate(dateString: String): Date? {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}
