package com.example.haritap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class AdminPanelActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Admin değilse içeri sokma
        if (UserPrefs.getRole(this) != "Admin") {
            Toast.makeText(this, "Bu sayfa sadece Admin içindir", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_admin_panel)

        findViewById<Button>(R.id.btnGoMain).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            AuthStore.logout(this)
            UserPrefs.clearProfile(this)
            finishAffinity()
        }


        findViewById<Button>(R.id.btnOpenAnnouncements).setOnClickListener {
            startActivity(Intent(this, AnnouncementsActivity::class.java))
        }

        findViewById<Button>(R.id.btnSendAnnouncement).setOnClickListener {
            val titleEt = android.widget.EditText(this).apply { hint = "Başlık" }
            val msgEt = android.widget.EditText(this).apply { hint = "Mesaj" }

            val layout = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(40, 20, 40, 0)
                addView(titleEt)
                addView(msgEt)
            }

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Yeni Duyuru")
                .setView(layout)
                .setPositiveButton("Gönder") { _, _ ->
                    val t = titleEt.text.toString().trim()
                    val m = msgEt.text.toString().trim()
                    if (t.isNotBlank() && m.isNotBlank()) {
                        AnnouncementStore.add(this, t, m)
                        Toast.makeText(this, "Duyuru gönderildi", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Başlık ve mesaj boş olamaz", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }

    }


}
