package com.example.haritap

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnReset = findViewById<Button>(R.id.btnReset)

        btnReset.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isBlank()) {
                etEmail.error = "E-posta gir"
                return@setOnClickListener
            }

            // ✅ Tik işaretli simülasyon mesajı
            AlertDialog.Builder(this)
                .setTitle("✅ Link Gönderildi")
                .setMessage("$email adresine şifre yenileme bağlantısı gönderildi.\n")
                .setPositiveButton("Tamam") { _, _ ->
                    startActivity(android.content.Intent(this, LoginActivity::class.java))
                    finish()

                }
                .show()
        }
    }
}
