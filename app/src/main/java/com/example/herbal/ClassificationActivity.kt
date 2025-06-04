package com.example.herbal

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

import androidx.appcompat.widget.Toolbar

class ClassificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classification)

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Menambahkan tombol back pada toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Menetapkan judul toolbar
        supportActionBar?.title = "Daftar Daun Herbal" // Pastikan judul sesuai

        // Handle back button press on toolbar
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Menekan tombol back akan mengarah kembali ke halaman sebelumnya
        }

        // Ambil semua CardView
        val cardIds = listOf(
            R.id.card_daun1, // Daun Jambu Biji
            R.id.card_daun2, // Daun Kari
            R.id.card_daun3, // Daun Kemangi
            R.id.card_daun4, // Daun Kunyit
            R.id.card_daun5, // Daun Mint
            R.id.card_daun6, // Daun Papaya
            R.id.card_daun7, // Daun Sirih
            R.id.card_daun8, // Daun Sirsak
            R.id.card_daun9, // Lidah Buaya
            R.id.card_daun10 // Daun Teh Hijau
        )

        // Pasang listener untuk masing-masing CardView
        for (id in cardIds) {
            val cardView = findViewById<CardView>(id)
            cardView?.setOnClickListener {
                val animation = AnimationUtils.loadAnimation(this, R.anim.scale_click)
                cardView.startAnimation(animation)

                // Tentukan nama daun berdasarkan ID CardView
                val daunName = when (id) {
                    R.id.card_daun1 -> "Daun Jambu Biji"
                    R.id.card_daun2 -> "Daun Kari"
                    R.id.card_daun3 -> "Daun Kemangi"
                    R.id.card_daun4 -> "Daun Kunyit"
                    R.id.card_daun5 -> "Daun Mint"
                    R.id.card_daun6 -> "Daun Papaya"
                    R.id.card_daun7 -> "Daun Sirih"
                    R.id.card_daun8 -> "Daun Sirsak"
                    R.id.card_daun9 -> "Lidah Buaya"
                    R.id.card_daun10 -> "Daun Teh Hijau"
                    else -> "Daun Tidak Dikenal"
                }

                // Menampilkan Toast dengan nama daun yang dipilih
                Toast.makeText(this, "$daunName clicked!", Toast.LENGTH_SHORT).show()

                // Kirim ID daun ke halaman detail
                val intent = Intent(this, DaunDetailActivity::class.java).apply {
                    putExtra("DAUN_ID", id)  // Kirim ID CardView
                }

                startActivity(intent)
            }
        }
    }
}


