package com.example.herbal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class InformasiAplikasiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.informasi_aplikasi)

        // Hubungkan Toolbar yang di-include
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set judul toolbar yang diinginkan
        supportActionBar?.title = "Info Aplikasi"

        // Aktifkan tombol back di toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Tangani aksi klik tombol back toolbar
    override fun onSupportNavigateUp(): Boolean {
        finish() // Tutup activity ini
        return true
    }
}
