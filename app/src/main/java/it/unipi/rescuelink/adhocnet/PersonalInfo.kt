package it.unipi.rescuelink.adhocnet

import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date

class PersonalInfo(
    var completeName: String,
    var heartBPM: Double,
    var weightKg: Double,
    var dateOfBirth: Date
){
    fun getAge(): Int {
        val today = LocalDate.now()
        val bd = dateOfBirth.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val years = Period.between(bd, today).years
        return years
    }
}