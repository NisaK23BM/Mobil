package com.example.haritap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.haritap.data.AppDatabase
import com.example.haritap.ui.ReportAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.AdapterView
import android.text.InputType
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog




class MainActivity : AppCompatActivity() {

    private lateinit var rvReports: RecyclerView
    private lateinit var etSearch: android.widget.EditText
    private lateinit var spFilter: android.widget.Spinner
    private lateinit var adapter: ReportAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val openMapBtn = findViewById<Button>(R.id.openMapBtn)
        val openNewReportBtn = findViewById<Button>(R.id.openNewReportBtn)
        val profileButton = findViewById<Button>(R.id.profileButton)
        val latest = AnnouncementStore.latest(this)
        if (latest != null) {
            Toast.makeText(this, "DUYURU: ${latest.title}", Toast.LENGTH_LONG).show()
        }


        openMapBtn.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        openNewReportBtn.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("selectMode", true)  // konum seçme modu
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnAnnouncements).setOnClickListener {
            startActivity(Intent(this, AnnouncementsActivity::class.java))
        }

        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        rvReports = findViewById(R.id.rvReports)
        etSearch = findViewById(R.id.etSearch)
        spFilter = findViewById(R.id.spFilter)

        adapter = ReportAdapter(emptyList()) { report ->
            val i = Intent(this, ReportDetailActivity::class.java)
            i.putExtra("reportId", report.id)
            startActivity(i)
        }

        rvReports.layoutManager = LinearLayoutManager(this)
        rvReports.adapter = adapter

        val filters = listOf("Hepsi", "Açık", "Sağlık", "Güvenlik", "Çevre", "Kayıp Eşya", "Teknik")
        spFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filters)

// İlk yükleme
        applyFilter("Hepsi", "")

// Filtre değişince
        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                applyFilter(filters[position], etSearch.text.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

// Arama yazınca
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                val f = spFilter.selectedItem?.toString() ?: "Hepsi"
                applyFilter(f, q)
            }
        })
        if (!UserPrefs.isProfileDone(this)) {
            showFirstProfileDialog()
        }


    }
    override fun onResume() {
        super.onResume()
        val q = etSearch.text?.toString() ?: ""
        val f = spFilter.selectedItem?.toString() ?: "Hepsi"
        applyFilter(f, q)
    }

    private fun applyFilter(filter: String, query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(this@MainActivity).reportDao()

            val baseList = when (filter) {
                "Hepsi" -> dao.getAll()
                "Açık" -> dao.getOpen()
                else -> dao.getByType(filter)
            }

            val finalList = if (query.isBlank()) {
                baseList
            } else {
                val searched = dao.search(query)
                val allowedIds = baseList.map { it.id }.toSet()
                searched.filter { allowedIds.contains(it.id) }
            }

            withContext(Dispatchers.Main) {
                adapter.submit(finalList)
            }
        }
    }
    private fun showFirstProfileDialog() {
        val nameInput = EditText(this).apply {
            hint = "Ad Soyad"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }

        val emailInput = EditText(this).apply {
            hint = "E-posta"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(nameInput)
            addView(emailInput)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Profil Bilgileri")
            .setMessage("İlk kullanım için bilgileri giriniz.")
            .setView(layout)
            .setCancelable(false) // kapatamasın, zorunlu
            .setPositiveButton("Kaydet", null)
            .create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val name = nameInput.text.toString().trim()
                val email = emailInput.text.toString().trim()

                if (name.isBlank()) {
                    nameInput.error = "Ad Soyad gerekli"
                    return@setOnClickListener
                }
                if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInput.error = "Geçerli e-posta gir"
                    return@setOnClickListener
                }

                UserPrefs.setProfile(this, name, email, "User", "Birim")

                dialog.dismiss()
            }
        }

        dialog.show()
    }

}
