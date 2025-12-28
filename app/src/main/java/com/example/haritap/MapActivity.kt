package com.example.haritap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.haritap.data.AppDatabase
import com.example.haritap.data.entity.ReportEntity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // Seçim modu için tek marker
    private var selectedMarker: Marker? = null

    // Rapor marker’larını tek tek temizleyebilmek için
    private val reportMarkers = mutableListOf<Marker>()

    // “Haritada göster” için
    private var focusReportId: Long? = null

    // Map filtre
    private var mapFilter: String = "Hepsi"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 5) GMS yoksa fallback
        val gmsStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (gmsStatus != ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Bu cihaz Google Haritalar'ı desteklemiyor. Liste ekranına dönülüyor.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_map)

        focusReportId = intent.getLongExtra("focusReportId", -1L).takeIf { it > 0L }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        // Filtre spinner
        val sp = findViewById<Spinner>(R.id.spMapFilter)
        val options = listOf("Hepsi", "Sadece Açık")
        sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        sp.setSelection(0, false)
        sp.setOnItemSelectedListener { selected ->
            mapFilter = selected
            loadReportsOnMap()
        }

        enableMyLocation()
        getCurrentLocation()

        // Marker’a tıklayınca detay
        map.setOnMarkerClickListener { marker ->
            val id: Long? = when (val t = marker.tag) {
                is Long -> t
                is Int -> t.toLong()
                is String -> t.toLongOrNull()
                else -> null
            }

            if (id != null) {
                val i = Intent(this, ReportDetailActivity::class.java)
                i.putExtra("reportId", id)
                startActivity(i)
                true
            } else false
        }

        // 1) seçim modu
        val selectMode = intent.getBooleanExtra("selectMode", false)
        if (selectMode) {
            Toast.makeText(this, "Rapor için haritada bir yer seç", Toast.LENGTH_LONG).show()

            map.setOnMapClickListener { latLng ->
                // Tek marker kalsın
                selectedMarker?.remove()
                selectedMarker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Seçilen Konum")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                )
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

                AlertDialog.Builder(this)
                    .setTitle("Konum seçildi")
                    .setMessage("Bu konumda rapor oluşturulsun mu?")
                    .setPositiveButton("Evet") { _, _ ->
                        val i = Intent(this, NewReportActivity::class.java)
                        i.putExtra("lat", latLng.latitude)
                        i.putExtra("lng", latLng.longitude)
                        startActivity(i)
                    }
                    .setNegativeButton("Hayır", null)
                    .show()
            }
        } else {
            map.setOnMapClickListener(null)
        }

        loadReportsOnMap()
    }

    override fun onResume() {
        super.onResume()
        if (::map.isInitialized) {
            loadReportsOnMap() // 6) rapor ekledikten sonra otomatik yenileme
        }
    }

    // İzin verildikten sonra: map hazır değilse crash olmasın
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            if (::map.isInitialized) {
                enableMyLocation()
                getCurrentLocation()
            }
        } else {
            Toast.makeText(this, "Konum izni verilmedi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val myLocation = LatLng(it.latitude, it.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))
            }
        }
    }

    private fun clearReportMarkers() {
        reportMarkers.forEach { it.remove() }
        reportMarkers.clear()
    }

    // 2) status/type’a göre marker rengi
    private fun hueForReport(r: ReportEntity): Float {
        return when (r.status) {
            "Çözüldü" -> BitmapDescriptorFactory.HUE_GREEN
            "İnceleniyor" -> BitmapDescriptorFactory.HUE_YELLOW
            else -> when (r.type) {
                "Güvenlik" -> BitmapDescriptorFactory.HUE_RED
                "Sağlık" -> BitmapDescriptorFactory.HUE_ORANGE
                "Teknik" -> BitmapDescriptorFactory.HUE_AZURE
                "Çevre" -> BitmapDescriptorFactory.HUE_GREEN
                "Kayıp Eşya" -> BitmapDescriptorFactory.HUE_VIOLET
                else -> BitmapDescriptorFactory.HUE_ROSE
            }
        }
    }

    private fun loadReportsOnMap() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(this@MapActivity).reportDao()

            val reports = if (mapFilter == "Sadece Açık") dao.getOpen() else dao.getAll()
            val focus = focusReportId?.let { dao.getById(it) }

            withContext(Dispatchers.Main) {
                clearReportMarkers()

                var focusMarker: Marker? = null

                for (r in reports) {
                    val m = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(r.latitude, r.longitude))
                            .title(r.title)
                            .snippet("${r.type} • ${r.status}")
                            .icon(BitmapDescriptorFactory.defaultMarker(hueForReport(r)))
                    )
                    if (m != null) {
                        m.tag = r.id
                        reportMarkers.add(m)
                        if (focusReportId != null && r.id == focusReportId) {
                            focusMarker = m
                        }
                    }
                }

                // Eğer filtre yüzünden görünmüyorsa, focus raporu ayrıca göster
                if (focusMarker == null && focus != null) {
                    val m = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(focus.latitude, focus.longitude))
                            .title(focus.title)
                            .snippet("${focus.type} • ${focus.status}")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    )
                    if (m != null) {
                        m.tag = focus.id
                        reportMarkers.add(m)
                        focusMarker = m
                    }
                }

                if (focusMarker != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(focusMarker!!.position, 16f))
                    focusMarker.showInfoWindow()
                    focusReportId = null // her yenilemede tekrar zoom yapmasın
                }
            }
        }
    }

    // Spinner listener küçük helper
    private fun Spinner.setOnItemSelectedListener(onSelected: (String) -> Unit) {
        this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                onSelected(this@setOnItemSelectedListener.selectedItem.toString())
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
}
