package com.example.haritap

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.haritap.data.AppDatabase
import com.example.haritap.data.entity.ReportEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportDetailActivity : AppCompatActivity() {

    private var reportId: Long = -1L

    private lateinit var tvTitle: TextView
    private lateinit var tvType: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvStatus: TextView
    private lateinit var spStatus: Spinner
    private lateinit var btnUpdateStatus: Button
    private lateinit var btnShowOnMap: Button

    private val statusOptions = listOf("Açık", "İnceleniyor", "Çözüldü")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        tvTitle = findViewById(R.id.tvDetailTitle)
        tvType = findViewById(R.id.tvDetailType)
        tvDesc = findViewById(R.id.tvDetailDesc)
        tvStatus = findViewById(R.id.tvDetailStatus)
        spStatus = findViewById(R.id.spStatus)
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus)
        btnShowOnMap = findViewById(R.id.btnShowOnMap)

        reportId = intent.getLongExtra("reportId", -1L)
        if (reportId <= 0L) {
            Toast.makeText(this, "Rapor bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        spStatus.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            statusOptions
        )

        btnUpdateStatus.setOnClickListener {
            val newStatus = spStatus.selectedItem?.toString() ?: "Açık"
            updateStatus(newStatus)
        }

        btnShowOnMap.setOnClickListener {
            val i = Intent(this, MapActivity::class.java)
            i.putExtra("focusReportId", reportId)
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        loadReport()
    }

    private fun loadReport() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(this@ReportDetailActivity).reportDao()
            val r: ReportEntity? = dao.getById(reportId)

            withContext(Dispatchers.Main) {
                if (r == null) {
                    Toast.makeText(this@ReportDetailActivity, "Rapor bulunamadı", Toast.LENGTH_SHORT).show()
                    finish()
                    return@withContext
                }
                bind(r)
            }
        }
    }

    private fun bind(r: ReportEntity) {
        tvTitle.text = r.title
        tvType.text = "Tür: ${r.type}"
        tvDesc.text = r.description
        tvStatus.text = "Durum: ${r.status}"

        val idx = statusOptions.indexOf(r.status).let { if (it >= 0) it else 0 }
        spStatus.setSelection(idx, false)
    }

    private fun updateStatus(newStatus: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(this@ReportDetailActivity).reportDao()
            dao.updateStatus(reportId, newStatus)
            val updated = dao.getById(reportId)

            withContext(Dispatchers.Main) {
                if (updated != null) bind(updated)
                Toast.makeText(this@ReportDetailActivity, "Durum güncellendi: $newStatus", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
