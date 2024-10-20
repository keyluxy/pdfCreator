package com.example.pdff

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var et_pdf_data : EditText
    private lateinit var btn_generate_pdf: Button
    private val STORAGE_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        et_pdf_data = findViewById(R.id.ent_pdf_data)
//        btn_generate_pdr = findViewById(R.id.btn_generate_pdf)
        btn_generate_pdf = findViewById(R.id.btn_generate_pdf)

        btn_generate_pdf.setOnClickListener {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                if (checkSelfPermission(WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                    ) {
                        val permission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        requestPermissions(permission, STORAGE_CODE)
                } else {
                    savePDF()
                }
            } else {
                savePDF()
            }

        }

    }

    @SuppressLint("SimpleDateFormat")


    private fun savePDF() {
        val data = et_pdf_data.text.toString().trim()
        if (data.isEmpty()) {
            Toast.makeText(this, "No data to save!", Toast.LENGTH_SHORT).show()
            return
        }

        // Задаем имя файла и создаем ContentValues
        val fileName = SimpleDateFormat("yyMMdd_HHmmss", java.util.Locale.getDefault()).format(Date()) + ".pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) // Сохранение в Downloads
        }

        // Получаем URI для сохранения файла
        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        try {
            uri?.let {
                contentResolver.openOutputStream(it).use { outputStream ->
                    val document = Document()
                    PdfWriter.getInstance(document, outputStream)
                    document.open()
                    document.addAuthor("Your Author Name")
                    document.add(Paragraph(data))
                    document.close()

                    Toast.makeText(this, "$fileName created in Downloads", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    savePDF()
                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}