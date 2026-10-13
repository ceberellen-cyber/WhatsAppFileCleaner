package com.example.whatsappcleaner

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var fileRecyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private val scannedFiles = mutableListOf<FileItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fileRecyclerView = findViewById(R.id.fileRecyclerView)
        val btnScan: Button = findViewById(R.id.btnScan)
        val btnDelete: Button = findViewById(R.id.btnDelete)
        val btnGoogleBackup: Button = findViewById(R.id.btnGoogleBackup)

        btnScan.setOnClickListener {
            if (checkStoragePermission()) {
                scanWhatsAppFolder()
            } else {
                requestStoragePermission()
            }
        }

        btnDelete.setOnClickListener {
            val selectedFiles = scannedFiles.filter { it.isSelected }
            if (selectedFiles.isEmpty()) {
                Toast.makeText(this, "Silinecek dosya seçilmedi.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Dosyaları Sil")
                .setMessage("${selectedFiles.size} adet dosyayı kalıcı olarak silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet, Sil") { _, _ ->
                    val iterator = scannedFiles.iterator()
                    while (iterator.hasNext()) {
                        val item = iterator.next()
                        if (item.isSelected) {
                            if (item.file.delete()) {
                                iterator.remove()
                            }
                        }
                    }
                    setupRecyclerView()
                    Toast.makeText(this, "Seçilen dosyalar silindi.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        btnGoogleBackup.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_PRIVACY_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_SETTINGS)
                startActivity(intent)
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }
    }

    private fun scanWhatsAppFolder() {
        scannedFiles.clear()
        
        val whatsappPath = File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp/WhatsApp")
        val legacyPath = File(Environment.getExternalStorageDirectory(), "WhatsApp")
        val targetDir = if (whatsappPath.exists()) whatsappPath else legacyPath

        if (targetDir.exists()) {
            val cutOffCalendar = Calendar.getInstance().apply {
                add(Calendar.MONTH, -7)
            }
            val sevenMonthsAgo = cutOffCalendar.timeInMillis

            targetDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val isRecent = file.lastModified() >= sevenMonthsAgo
                    scannedFiles.add(FileItem(file, isSelected = isRecent))
                }
            }
            setupRecyclerView()
        } else {
            Toast.makeText(this, "WhatsApp klasörü bulunamadı.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(scannedFiles)
        fileRecyclerView.layoutManager = LinearLayoutManager(this)
        fileRecyclerView.adapter = fileAdapter
    }
}

data class FileItem(val file: File, var isSelected: Boolean)
