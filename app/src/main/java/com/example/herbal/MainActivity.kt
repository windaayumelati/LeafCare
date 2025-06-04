package com.example.herbal

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_click)

        // Menu Klasifikasi Daun
        val menuKlasifikasi = findViewById<LinearLayout>(R.id.menuKlasifikasi)
        menuKlasifikasi.setOnClickListener {
            menuKlasifikasi.startAnimation(scaleAnim)
            val intent = Intent(this, ClassificationActivity::class.java)
            startActivity(intent)
        }

        // Menu Prediksi Fitur Daun
        val menuPrediksi = findViewById<LinearLayout>(R.id.menuPrediksi)
        menuPrediksi.setOnClickListener {
            menuPrediksi.startAnimation(scaleAnim)
            val intent = Intent(this, PredictionActivity::class.java)
            startActivity(intent)
        }

        // Menu Informasi Aplikasi
        val menuInformasi = findViewById<ImageButton>(R.id.menuInformasi)
        menuInformasi.setOnClickListener {
            menuInformasi.startAnimation(scaleAnim)
            val intent = Intent(this, InformasiAplikasiActivity::class.java) // Mengarah ke InformasiAplikasiActivity
            startActivity(intent)
        }

    }
}
