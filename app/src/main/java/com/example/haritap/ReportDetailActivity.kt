package com.example.haritap

import android.content.Intent
import android.os.Bundle
import android.view.View
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

    private lateinit var btnFollowToggle: Button
    private lateinit var btnShowOnMap: Button
    private lateinit var tvCreatedAt: TextView


    private val statusOptions = listOf("Açık", "İnceleniyor", "Çözüldü")

    // ✅ Admin kontrol (class içinde)
    private val isAdmin: Boolean by lazy { UserPrefs.getRole(this) == "Admin" }

    private var currentReport: ReportEntity? = null
    private lateinit var btnDeleteReport: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        tvTitle = findViewById(R.id.tvDetailTitle)
        tvType = findViewById(R.id.tvDetailType)
        tvDesc = findViewById(R.id.tvDetailDesc)
        tvStatus = findViewById(R.id.tvDetailStatus)
        tvCreatedAt = findViewById(R.id.tvDetailCreatedAt)

        spStatus = findViewById(R.id.spStatus)
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus)

        btnShowOnMap = findViewById(R.id.btnShowOnMap)
        btnFollowToggle = findViewById(R.id.btnFollowToggle)

        reportId = intent.getLongExtra("reportId", -1L)


        btnDeleteReport = findViewById(R.id.btnDeleteReport)
        btnDeleteReport.visibility = if (isAdmin) View.VISIBLE else View.GONE

        btnDeleteReport.setOnClickListener {
            if (!isAdmin) {
                Toast.makeText(this, "Bu işlem sadece admin için", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val r = currentReport
            if (r == null) {
                Toast.makeText(this, "Rapor yüklenemedi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Rapor Sil")
                .setMessage("Bu raporu silmek istiyor musun?")
                .setPositiveButton("Sil") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val dao = AppDatabase.getInstance(this@ReportDetailActivity).reportDao()
                        dao.delete(r)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ReportDetailActivity, "Rapor silindi", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        if (reportId <= 0L) {
            Toast.makeText(this, "Rapor bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Spinner seçenekleri
        spStatus.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            statusOptions
        )

        // ✅ Admin değilse durum güncelleme UI’sı görünmesin
        spStatus.visibility = if (isAdmin) View.VISIBLE else View.GONE
        btnUpdateStatus.visibility = if (isAdmin) View.VISIBLE else View.GONE

        // ✅ Admin kontrolü (click)
        btnUpdateStatus.setOnClickListener {
            if (!isAdmin) {
                Toast.makeText(this, "Bu işlem sadece admin için", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newStatus = spStatus.selectedItem?.toString() ?: "Açık"
            updateStatus(newStatus)
        }

        btnShowOnMap.setOnClickListener {
            val i = Intent(this, MapActivity::class.java)
            i.putExtra("focusReportId", reportId)
            startActivity(i)
        }

        btnFollowToggle.setOnClickListener {
            val nowFollowed = UserPrefs.toggleFollow(this, reportId)
            btnFollowToggle.text = if (nowFollowed) "Takibi Bırak" else "Takip Et"
            Toast.makeText(
                this,
                if (nowFollowed) "Takibe alındı" else "Takipten çıkarıldı",
                Toast.LENGTH_SHORT
            ).show()
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
        currentReport = r

        tvTitle.text = r.title
        tvType.text = "Tür: ${r.type}"
        tvDesc.text = r.description
        tvStatus.text = "Durum: ${r.status}"

        val idx = statusOptions.indexOf(r.status).let { if (it >= 0) it else 0 }
        spStatus.setSelection(idx, false)

        btnFollowToggle.text =
            if (UserPrefs.isFollowed(this, reportId)) "Takibi Bırak" else "Takip Et"
        val fmt = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale("tr","TR"))
        tvCreatedAt.text = "Oluşturulma: " + fmt.format(java.util.Date(r.createdAt))


    }

    private fun updateStatus(newStatus: String) {
        // ✅ ekstra güvenlik: fonksiyon seviyesinde de admin kontrol
        if (!isAdmin) return

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
