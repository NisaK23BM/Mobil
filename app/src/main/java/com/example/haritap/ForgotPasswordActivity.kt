package com.example.haritap

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etNew = findViewById<EditText>(R.id.etNewPass)
        val btn = findViewById<Button>(R.id.btnReset)

        btn.setOnClickListener {
            val (ok, msg) = AuthStore.resetPassword(this,
                etEmail.text.toString().trim(),
                etNew.text.toString()
            )
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            if (ok) finish()
        }
    }
}
