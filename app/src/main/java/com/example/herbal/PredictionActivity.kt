package com.example.herbal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PredictionActivity : AppCompatActivity() {

    private lateinit var classifier: Classifier
    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var benefitText: TextView
    private lateinit var btnDetail: LinearLayout

    private val IMAGE_PICK_CODE = 1000
    private val CAMERA_REQUEST_CODE = 1001
    private val CAMERA_PERMISSION_CODE = 2001
    private val STORAGE_PERMISSION_CODE = 2002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)

        // Inisialisasi Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Deteksi Daun Herbal"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        classifier = Classifier(assets, "Coba.tflite")
        imageView = findViewById(R.id.imageView)
        resultText = findViewById(R.id.resultText)
        benefitText = findViewById(R.id.benefitText)
        btnDetail = findViewById(R.id.btnDetail)

        // Ubah tipe btnCamera dan btnGallery jadi LinearLayout
        val btnCamera = findViewById<LinearLayout>(R.id.btnCamera)
        val btnGallery = findViewById<LinearLayout>(R.id.btnGallery)

        val scaleClick = AnimationUtils.loadAnimation(this, R.anim.scale_click)

        btnCamera.setOnClickListener {
            it.startAnimation(scaleClick)
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        btnGallery.setOnClickListener {
            it.startAnimation(scaleClick)
            if (checkStoragePermission()) {
                openGallery()
            } else {
                requestStoragePermission()
            }
        }

    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        ActivityCompat.requestPermissions(this, arrayOf(permission), STORAGE_PERMISSION_CODE)
    }

    private fun showSettingsDialog(source: String) {
        AlertDialog.Builder(this)
            .setTitle("Izin $source Ditolak Permanen")
            .setMessage("Silakan buka pengaturan aplikasi untuk memberikan izin $source secara manual.")
            .setPositiveButton("Buka Pengaturan") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                CAMERA_PERMISSION_CODE -> openCamera()
                STORAGE_PERMISSION_CODE -> openGallery()
            }
        } else {
            val permanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
            val source = when (requestCode) {
                CAMERA_PERMISSION_CODE -> "Kamera"
                STORAGE_PERMISSION_CODE -> "Galeri"
                else -> "Fitur"
            }

            if (permanentlyDenied) {
                showSettingsDialog(source)
            } else {
                Toast.makeText(this, "Izin $source ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val chooser = Intent.createChooser(intent, "Pilih aplikasi untuk membuka gambar")
        startActivityForResult(chooser, IMAGE_PICK_CODE)
    }




    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val bitmap: Bitmap? = when (requestCode) {
                IMAGE_PICK_CODE -> {
                    val uri: Uri? = data.data
                    uri?.let { getBitmapFromUri(it) }
                }
                CAMERA_REQUEST_CODE -> data.extras?.get("data") as? Bitmap
                else -> null
            }

            bitmap?.let {
                processBitmap(it)
            } ?: Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val maxDim = 1000
            var sampleSize = 1
            while (options.outWidth / sampleSize > maxDim || options.outHeight / sampleSize > maxDim) {
                sampleSize *= 2
            }

            val inputStream2 = contentResolver.openInputStream(uri)
            val finalOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, finalOptions)
            inputStream2?.close()

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun processBitmap(bitmap: Bitmap) {
        imageView.setImageBitmap(bitmap)
        val resultFullText = classifier.classify(bitmap)
        resultText.text = "Prediksi: $resultFullText"

        val label = resultFullText.substringBefore("\n")
        val intentLabel = label.lowercase()
        val recognizedLabels = listOf(
            "daun jambu biji", "daun kari", "daun kemangi", "daun kunyit",
            "daun mint", "daun pepaya", "daun sirih", "daun sirsak",
            "lidah buaya", "teh hijau"
        )

        val benefitContainer = findViewById<LinearLayout>(R.id.benefitContainer)
        val btnDetail = findViewById<LinearLayout>(R.id.btnDetail)

        val isLabelRecognized = intentLabel in recognizedLabels
        val benefit = if (isLabelRecognized) classifier.getBenefits(this, label) else ""
        val description = if (isLabelRecognized) classifier.getDescription(this, label) else ""

        if (isLabelRecognized && benefit.isNotEmpty() && description.isNotEmpty()) {
            val daunName = label.replaceFirstChar { it.uppercase() }
            val deskripsiTitle = "Deskripsi $daunName:"
            val manfaatTitle = "Informasi Manfaat $daunName:"
            val fullText = "$deskripsiTitle\n$description\n\n$manfaatTitle\n$benefit"
            val spannable = SpannableString(fullText)

            // Bold + 16sp untuk judul utama
            spannable.setSpan(StyleSpan(Typeface.BOLD), 0, deskripsiTitle.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(AbsoluteSizeSpan(16, true), 0, deskripsiTitle.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            val manfaatStart = fullText.indexOf(manfaatTitle)
            spannable.setSpan(StyleSpan(Typeface.BOLD), manfaatStart, manfaatStart + manfaatTitle.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(AbsoluteSizeSpan(16, true), manfaatStart, manfaatStart + manfaatTitle.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Karena Android gak punya semi-bold, pakai BOLD untuk subjudul juga
            val sub1 = "Manfaat $daunName untuk Kulit:"
            val sub1Start = fullText.indexOf(sub1)
            if (sub1Start >= 0) {
                spannable.setSpan(StyleSpan(Typeface.BOLD), sub1Start, sub1Start + sub1.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(AbsoluteSizeSpan(15, true), sub1Start, sub1Start + sub1.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            val sub2 = "Manfaat $daunName untuk Rambut:"
            val sub2Start = fullText.indexOf(sub2)
            if (sub2Start >= 0) {
                spannable.setSpan(StyleSpan(Typeface.BOLD), sub2Start, sub2Start + sub2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(AbsoluteSizeSpan(15, true), sub2Start, sub2Start + sub2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            // Isi teks biasa (Regular + 14sp)
            val descIsiStart = deskripsiTitle.length + 1 // +1 agar spasi baru ikut
            spannable.setSpan(AbsoluteSizeSpan(14, true), descIsiStart, manfaatStart - 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            val manfaatIsiStart = manfaatStart + manfaatTitle.length + 1
            spannable.setSpan(AbsoluteSizeSpan(14, true), manfaatIsiStart, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            benefitText.text = spannable
            benefitText.visibility = View.VISIBLE
            benefitContainer.visibility = View.VISIBLE
            btnDetail.visibility = View.VISIBLE

            findViewById<ImageView>(R.id.resultIcon).visibility = View.GONE

            val scaleClick = AnimationUtils.loadAnimation(this, R.anim.scale_click)
            btnDetail.setOnClickListener { view ->
                view.startAnimation(scaleClick)
                view.postDelayed({
                    val intent = Intent(this, DaunDetailActivity::class.java)
                    intent.putExtra("DAUN_LABEL", intentLabel)
                    startActivity(intent)
                }, 150)
            }
        } else {
            val unknownText = getString(R.string.unknown_prediction_text)
            val spannable = SpannableString(unknownText)

// Bold seluruh teks
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                unknownText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

// Buat center alignment dengan AlignmentSpan.Standard
            spannable.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0,
                unknownText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            benefitText.text = spannable
            benefitText.visibility = View.VISIBLE
            benefitContainer.visibility = View.VISIBLE
            btnDetail.visibility = View.GONE

            findViewById<ImageView>(R.id.resultIcon).visibility = View.VISIBLE
        }
    }
}