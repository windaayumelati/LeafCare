package com.example.herbal

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Classifier(assetManager: AssetManager, modelPath: String) {

    companion object {
        private const val THRESHOLD = 0.7f
    }

    private val interpreter: Interpreter
    private val labels = listOf(
        "Daun Jambu Biji", "Daun Kari", "Daun Kemangi", "Daun Kunyit",
        "Daun Mint", "Daun Pepaya", "Daun Sirih", "Daun Sirsak",
        "Lidah Buaya", "Teh Hijau", "Tidak Dikenali"   // Harus ada ini
    )

    init {
        val model = assetManager.open(modelPath).readBytes()
        val buffer = ByteBuffer.allocateDirect(model.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(model)
        interpreter = Interpreter(buffer)
    }

    fun classify(bitmap: Bitmap): String {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 299, 299, true)
        val input = Array(1) { Array(299) { Array(299) { FloatArray(3) } } }

        for (x in 0 until 299) {
            for (y in 0 until 299) {
                val pixel = scaledBitmap.getPixel(x, y)
                input[0][x][y][0] = Color.red(pixel) / 255.0f
                input[0][x][y][1] = Color.green(pixel) / 255.0f
                input[0][x][y][2] = Color.blue(pixel) / 255.0f
            }
        }

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(input, output)

        val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val confidence = if (maxIdx != -1) output[0][maxIdx] else 0f

        // Tentukan label dengan threshold
        val label = if (maxIdx != -1 && confidence >= THRESHOLD) labels[maxIdx] else "Tidak Dikenali"

        return if (label == "Tidak Dikenali") {
            label
        } else {
            val percentage = String.format("%.2f", confidence * 100)
            "$label\nAkurasi: $percentage%"
        }
    }

    fun getBenefits(context: Context, label: String): String {
        val fullText = getFullText(context, label)
        return if (fullText.contains("Manfaat:")) {
            fullText.substringAfter("Manfaat:").trim()
        } else {
            ""
        }
    }

    fun getDescription(context: Context, label: String): String {
        val fullText = getFullText(context, label)
        return if (fullText.contains("Deskripsi:")) {
            fullText.substringAfter("Deskripsi:").substringBefore("Manfaat:").trim()
        } else {
            ""
        }
    }

    private fun getFullText(context: Context, label: String): String {
        return when (label) {
            "Daun Jambu Biji" -> context.getString(R.string.daun_jambu_biji)
            "Daun Kari" -> context.getString(R.string.daun_kari)
            "Daun Kemangi" -> context.getString(R.string.daun_kemangi)
            "Daun Kunyit" -> context.getString(R.string.daun_kunyit)
            "Daun Mint" -> context.getString(R.string.daun_mint)
            "Daun Pepaya" -> context.getString(R.string.daun_pepaya)
            "Daun Sirih" -> context.getString(R.string.daun_sirih)
            "Daun Sirsak" -> context.getString(R.string.daun_sirsak)
            "Lidah Buaya" -> context.getString(R.string.lidah_buaya)
            "Teh Hijau" -> context.getString(R.string.teh_hijau)
            "Tidak Dikenali" -> context.getString(R.string.unknown_prediction_text)
            else -> context.getString(R.string.unknown_prediction_text)
        }
    }
}
