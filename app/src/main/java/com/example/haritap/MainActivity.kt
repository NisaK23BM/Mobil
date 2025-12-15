package com.example.haritap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val openMapBtn = findViewById<Button>(R.id.openMapBtn)
        val openNewReportBtn = findViewById<Button>(R.id.openNewReportBtn)

        openMapBtn.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        openNewReportBtn.setOnClickListener {
            val intent = Intent(this, NewReportActivity::class.java)
            startActivity(intent)
        }
    }
}
