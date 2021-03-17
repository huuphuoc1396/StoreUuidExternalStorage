package com.example.storeuuidexternalstorage

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            setYourDeviceUuidText(getDeviceUuid() ?: "Error")
        } else {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(
                    this,
                    RC_READ_WRITE_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ).build()
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        setYourDeviceUuidText(getDeviceUuid() ?: "Error")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
    }

    private fun setYourDeviceUuidText(uuid: String) {
        val textYourDeviceUuid = findViewById<TextView>(R.id.textYourDeviceUuid)
        textYourDeviceUuid.text = getString(R.string.your_device_uuid, uuid)
    }

    @Throws(FileNotFoundException::class)
    private fun getDeviceUuid(): String? {
        // Checks if a volume containing external storage is available for read and write.
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val documentFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            // Sure that the documents folder was created
            if (!documentFolder.exists()) {
                documentFolder.mkdir()
            }
            val dataFolder = File(documentFolder, "MyData")
            if (!dataFolder.exists()) {
                dataFolder.mkdir()
            }
            val file = File(dataFolder, "data.txt")
            if (file.exists()) {
                val length = file.length()
                val bytes = ByteArray(length.toInt())
                val inputStream = FileInputStream(file)
                inputStream.use {
                    inputStream.read(bytes)
                }
                var uuid = String(bytes)
                if (uuid.isEmpty()) {
                    val outputStream = FileOutputStream(file)
                    uuid = UUID.randomUUID().toString()
                    uuid.toByteArray(charset("UTF8")).let { byteArr ->
                        outputStream.use { outputStream.write(byteArr) }
                    }
                }
                return uuid
            } else {
                val outputStream = FileOutputStream(file)
                val uuid = UUID.randomUUID().toString()
                uuid.toByteArray(charset("UTF8")).let { byteArr ->
                    outputStream.use { outputStream.write(byteArr) }
                }
                return uuid
            }
        }
        return null
    }

    companion object {
        private const val RC_READ_WRITE_STORAGE = 123
    }
}