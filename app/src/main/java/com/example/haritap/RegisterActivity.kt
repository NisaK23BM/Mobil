package com.example.haritap

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val spDept = findViewById<Spinner>(R.id.spDept)
        val spRole = findViewById<Spinner>(R.id.spRole)
        val btn = findViewById<Button>(R.id.btnRegister)

        spDept.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Sağlık", "Güvenlik", "Teknik", "İdari", "Öğrenci İşleri"))

        spRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("User", "Admin")) // sunum için

        btn.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString()
            val dept = spDept.selectedItem.toString()
            val role = spRole.selectedItem.toString()

            if (name.isBlank() || email.isBlank() || pass.length < 4) {
                Toast.makeText(this, "Bilgileri düzgün gir (şifre min 4)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val (ok, msg) = AuthStore.register(this, UserAccount(name, email, pass, dept, role))
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            if (ok) {
                val next = if (UserPrefs.getRole(this) == "Admin") {
                    AdminPanelActivity::class.java
                } else {
                    MainActivity::class.java
                }
                startActivity(Intent(this, next))
                finishAffinity()

            }
        }
    }
}
