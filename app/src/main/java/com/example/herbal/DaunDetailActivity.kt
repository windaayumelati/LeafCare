package com.example.herbal

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class DaunDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val daunLabel = intent.getStringExtra("DAUN_LABEL")
        val daunId = intent.getIntExtra("DAUN_ID", -1)

        val (layoutResId, title) = when {
            !daunLabel.isNullOrEmpty() -> {
                val layout = getLayoutByLabel(daunLabel)
                val judul = "Detail ${formatJudulWithPrefix(daunLabel)}"
                Pair(layout, judul)
            }

            daunId != -1 -> getLayoutAndTitleById(daunId)
            else -> Pair(null, null)
        }

        if (layoutResId != null && title != null) {
            setContentView(layoutResId)
            setupToolbar(title)

            val imageByteArray = intent.getByteArrayExtra("IMAGE_BITMAP")
            if (imageByteArray != null) {
                val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                val imageView: ImageView? = findViewById(R.id.imageView)
                imageView?.setImageBitmap(bitmap)
            }
        } else {
            finish()
        }
    }

    private fun setupToolbar(title: String) {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun getLayoutByLabel(label: String): Int? {
        return when (label.lowercase()) {
            "daun jambu biji" -> R.layout.activity_daun_jambu_biji
            "daun kari" -> R.layout.activity_daun_kari
            "daun kemangi" -> R.layout.activity_daun_kemangi
            "daun kunyit" -> R.layout.activity_daun_kunyit
            "daun mint" -> R.layout.activity_daun_mint
            "daun pepaya" -> R.layout.activity_daun_papaya
            "daun sirih" -> R.layout.activity_daun_sirih
            "daun sirsak" -> R.layout.activity_daun_sirsak
            "lidah buaya" -> R.layout.activity_lidah_buaya
            "teh hijau" -> R.layout.activity_daun_teh_hijau
            else -> null
        }
    }

    private fun getLayoutAndTitleById(id: Int): Pair<Int?, String?> {
        return when (id) {
            R.id.card_daun1 -> Pair(R.layout.activity_daun_jambu_biji, "Daun Jambu Biji")
            R.id.card_daun2 -> Pair(R.layout.activity_daun_kari, "Daun Kari")
            R.id.card_daun3 -> Pair(R.layout.activity_daun_kemangi, "Daun Kemangi")
            R.id.card_daun4 -> Pair(R.layout.activity_daun_kunyit, "Daun Kunyit")
            R.id.card_daun5 -> Pair(R.layout.activity_daun_mint, "Daun Mint")
            R.id.card_daun6 -> Pair(R.layout.activity_daun_papaya, "Daun Pepaya")
            R.id.card_daun7 -> Pair(R.layout.activity_daun_sirih, "Daun Sirih")
            R.id.card_daun8 -> Pair(R.layout.activity_daun_sirsak, "Daun Sirsak")
            R.id.card_daun9 -> Pair(R.layout.activity_lidah_buaya, "Lidah Buaya")
            R.id.card_daun10 -> Pair(R.layout.activity_daun_teh_hijau, "Daun Teh Hijau")
            else -> Pair(null, null)
        }
    }

    private fun formatJudulWithPrefix(text: String): String {
        val cleaned = text.trim().lowercase()

        // Jika lidah buaya, langsung format saja tanpa tambahan "daun"
        val withPrefix = if (cleaned == "lidah buaya") {
            cleaned
        } else {
            if (!cleaned.startsWith("daun")) "daun $cleaned" else cleaned
        }

        // Capitalize setiap kata
        return withPrefix.split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }
}