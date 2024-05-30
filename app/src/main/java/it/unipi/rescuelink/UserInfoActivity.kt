package it.unipi.rescuelink

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UserInfoActivity : AppCompatActivity() {

    private lateinit var etWeight: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var etSurname: EditText
    private lateinit var etName: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etBirthDate = findViewById(R.id.etBirthDate)
        etWeight = findViewById(R.id.etWeight)

        btnSave = findViewById(R.id.btnSave)

        // Carica i dati salvati (se esistono)
        loadUserData(sharedPreferences)

        btnSave.setOnClickListener {
            saveUserData(sharedPreferences)
        }

        etBirthDate.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDay)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            etBirthDate.setText(dateFormat.format(selectedDate.time))
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun loadUserData(sharedPreferences: SharedPreferences) {
        val name = sharedPreferences.getString("name", "")
        val surname = sharedPreferences.getString("surname", "")
        val birthDate = sharedPreferences.getString("birthDate", "")
        val weight = sharedPreferences.getString("weight", "")

        etName.setText(name)
        etSurname.setText(surname)
        etBirthDate.setText(birthDate)
        etWeight.setText(weight)
    }

    private fun saveUserData(sharedPreferences: SharedPreferences) {
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val birthDate = etBirthDate.text.toString()
        val weight = etWeight.text.toString()

        with(sharedPreferences.edit()) {
            putString("name", name)
            putString("surname", surname)
            putString("birthDate", birthDate)
            putString("weight", weight)
            apply()
        }
    }

    companion object{
        private const val TAG = "UserInfoActivity"
    }
}
