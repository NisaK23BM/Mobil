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

        // Departmanlar
        spDept.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Sağlık", "Güvenlik", "Teknik", "İdari", "Öğrenci İşleri")
        )

        // Rol: Sadece User (Admin seçilemez)
        val roleOptions = listOf("User")
        spRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roleOptions)
        spRole.setSelection(0)
        spRole.isEnabled = false  // ✅ admin seçilemesin

        btn.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString()
            val dept = spDept.selectedItem.toString()

            // Rolü sabitle
            val role = "User"

            // Basit kontrol
            if (name.isBlank() || email.isBlank() || pass.length < 4) {
                Toast.makeText(this, "Bilgileri düzgün gir (şifre min 4)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Admin hesabı kayıtla oluşturulamasın (sadece login ekranından admin/admin)
            if (email == "admin") {
                Toast.makeText(this, "Bu e-posta kullanılamaz.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val (ok, msg) = AuthStore.register(this, UserAccount(name, email, pass, dept, role))
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

            if (ok) {
                // Kayıt başarılı -> Login ekranına dön
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}
