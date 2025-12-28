package com.example.haritap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvDept: TextView

    private lateinit var btnLogout: Button
    private lateinit var btnFollowed: Button
    private lateinit var settingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // TextViews
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvRole = findViewById(R.id.tvRole)
        tvDept = findViewById(R.id.tvDept)

        // Buttons
        settingsButton = findViewById(R.id.settingsButton)
        btnFollowed = findViewById(R.id.btnFollowedReports)
        btnLogout = findViewById(R.id.btnLogout)

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnFollowed.setOnClickListener {
            startActivity(Intent(this, FollowedReportsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            AuthStore.logout(this)
            UserPrefs.clearProfile(this)
            finishAffinity() // uygulamayı tamamen kapatır
        }
    }

    override fun onResume() {
        super.onResume()
        tvName.text = UserPrefs.getName(this)
        tvEmail.text = UserPrefs.getEmail(this)
        tvRole.text = "Rol: " + UserPrefs.getRole(this)
        tvDept.text = "Birim: " + UserPrefs.getDept(this)
    }
}
