package com.ccscan.barcode

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            checkOverlayPermissionAndStart()
        } else {
            showPermissionDialog("需要相机权限", "请授予相机权限以识别条形码") {
                finish()
            }
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (canDrawOverlays()) {
            FloatingBallService.start(this)
            Toast.makeText(this, "悬浮球已启动", Toast.LENGTH_SHORT).show()
        } else {
            showPermissionDialog("需要悬浮窗权限", "请授予悬浮窗权限以显示悬浮球") {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        btnStart.setOnClickListener {
            requestCameraPermission()
        }

        btnStop.setOnClickListener {
            FloatingBallService.stop(this)
            Toast.makeText(this, "悬浮球已关闭", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestCameraPermission() {
        if (checkSelfPermissionCompat(Manifest.permission.CAMERA)) {
            checkOverlayPermissionAndStart()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkSelfPermissionCompat(permission: String): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            this,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun checkOverlayPermissionAndStart() {
        if (canDrawOverlays()) {
            FloatingBallService.start(this)
            Toast.makeText(this, "悬浮球已启动\n长按悬浮球开始扫码", Toast.LENGTH_LONG).show()
        } else {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun showPermissionDialog(title: String, message: String, onCancel: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("确定") { _, _ ->
                onCancel()
            }
            .setCancelable(false)
            .show()
    }
}
