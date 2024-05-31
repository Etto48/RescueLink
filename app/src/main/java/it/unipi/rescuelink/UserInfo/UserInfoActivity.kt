package it.unipi.rescuelink.UserInfo

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import it.unipi.rescuelink.R
import it.unipi.rescuelink.RescueLink
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UserInfoActivity : AppCompatActivity() {

    private lateinit var etWeight: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var etSurname: EditText
    private lateinit var etName: EditText
    private lateinit var btnSave: Button
    private lateinit var userInfoManager: UserInfoManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        userInfoManager = UserInfoManager(this)

        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etBirthDate = findViewById(R.id.etBirthDate)
        etWeight = findViewById(R.id.etWeight)
        btnSave = findViewById(R.id.btnSave)

        // Carica i dati salvati (se esistono)
        loadUserData()

        btnSave.setOnClickListener {
            saveUserData()
            RescueLink.info.thisDeviceInfo.personalInfo = userInfoManager.loadPersonalInfo()
            Toast.makeText(this, R.string.saved_message, Toast.LENGTH_SHORT).show()
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
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etBirthDate.setText(dateFormat.format(selectedDate.time))
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun loadUserData() {
        etName.setText(userInfoManager.name)
        etSurname.setText(userInfoManager.surname)
        etBirthDate.setText(userInfoManager.birthDate)
        etWeight.setText(userInfoManager.weight)
    }

    private fun saveUserData() {
        userInfoManager.name = etName.text.toString()
        userInfoManager.surname = etSurname.text.toString()
        userInfoManager.birthDate = etBirthDate.text.toString()
        userInfoManager.weight = etWeight.text.toString()
    }

    companion object {
        private const val TAG = "UserInfoActivity"
    }
}
