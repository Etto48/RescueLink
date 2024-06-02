package it.unipi.rescuelink.adhocnet

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.Period

@Serializable
class PersonalInfo(
    var completeName: String,
    var heartBPM: Double,
    var weightKg: Double,
    var dateOfBirth: String
){
    fun getAge(): Int {
        val today = LocalDate.now()
        val bd = LocalDate.parse(dateOfBirth)
        val years = Period.between(bd, today).years
        return years
    }
}