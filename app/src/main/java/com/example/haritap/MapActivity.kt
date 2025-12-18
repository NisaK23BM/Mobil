package com.example.haritap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.lifecycle.lifecycleScope
import com.example.haritap.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.gms.maps.model.Marker



class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    private var selectedMarker: Marker? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()
    }

    //  KONUM İZNİ KONTROLÜ
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
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

        enableMyLocation()
        getCurrentLocation()
        loadReportsOnMap()


        // HARİTAYA TIKLAYINCA → NEW REPORT
        map.setOnMapClickListener { latLng ->

            selectedMarker?.remove()
            selectedMarker = map.addMarker(
                MarkerOptions().position(latLng).title("Seçilen Konum")
            )

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Seçilen Konum")
            )

            val intent = Intent(this, NewReportActivity::class.java)
            intent.putExtra("lat", latLng.latitude)
            intent.putExtra("lng", latLng.longitude)
            startActivity(intent)
        }
    }

    //  MEVCUT KONUMU AL
    private fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {

                val myLocation = LatLng(it.latitude, it.longitude)

                map.addMarker(
                    MarkerOptions()
                        .position(myLocation)
                        .title("Şu anki konumum")
                )

                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(myLocation, 15f)
                )
            }
        }
    }

    //  HARİTADA MAVİ NOKTA
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }
    }

    //  İZİN VERİLDİKTEN SONRA TEKRAR DENEME
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
            enableMyLocation()
            getCurrentLocation()
        } else {
            Toast.makeText(
                this,
                "Konum izni verilmedi",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun loadReportsOnMap() {
        lifecycleScope.launch(Dispatchers.IO) {
            val reports = AppDatabase.getInstance(this@MapActivity).reportDao().getAll()
            withContext(Dispatchers.Main) {
                for (r in reports) {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(r.latitude, r.longitude))
                            .title(r.title)
                            .snippet("${r.type} • ${r.status}")
                    )
                }
            }
        }
    }

}