package com.example.haritap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val next = when {
            AuthStore.isLoggedIn(this) -> MainActivity::class.java
            !AuthStore.hasAnyUser(this) -> RegisterActivity::class.java
            else -> LoginActivity::class.java
        }

        startActivity(Intent(this, next))
        finish()
    }
}
