package com.example.haritap

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NewReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_report)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //  FORM ELEMANLARI
        val spinner = findViewById<Spinner>(R.id.spinnerType)
        val title = findViewById<EditText>(R.id.etTitle)
        val desc = findViewById<EditText>(R.id.etDescription)
        val btn = findViewById<Button>(R.id.btnSubmit)
        val locationText = findViewById<TextView>(R.id.locationText)

        //  KONUM VERİSİNİ MAP'TEN AL
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)

        if (lat != 0.0 && lng != 0.0) {
            locationText.text = "Konum: $lat, $lng"
        } else {
            locationText.text = "Konum: seçilmedi"
        }

        //  SPINNER
        val types = listOf("Sağlık", "Güvenlik", "Teknik", "Çevre", "Kayıp Eşya")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )
        spinner.adapter = adapter

        //  GÖNDER BUTONU
        btn.setOnClickListener {

            if (title.text.isBlank() || desc.text.isBlank()) {
                Toast.makeText(
                    this,
                    "Lütfen tüm alanları doldurun",
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("type", spinner.selectedItem.toString())
                intent.putExtra("title", title.text.toString())
                intent.putExtra("desc", desc.text.toString())
                intent.putExtra("lat", lat)
                intent.putExtra("lng", lng)

                startActivity(intent)
                finish()
            }
        }
    }
}
