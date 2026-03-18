package com.android.filemanager.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.android.filemanager.R

open class SimpleActivity : AppCompatActivity() {
    
    protected var actionOnPermission: ((granted: Boolean) -> Unit)? = null
    protected var isAskingPermissions = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    open fun getAppIconIDs() = arrayListOf(
        R.mipmap.ic_launcher
    )

    companion object {
        private const val MANAGE_STORAGE_RC = 201
        const val PERMISSION_WRITE_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    }

    open fun getAppLauncherName() = getString(R.string.app_launcher_name)

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        isAskingPermissions = false
        if (requestCode == MANAGE_STORAGE_RC) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                actionOnPermission?.invoke(Environment.isExternalStorageManager())
            }
        }
    }

    fun isRPlus() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R

    @SuppressLint("NewApi")
    fun hasStoragePermission(): Boolean {
        return if (isRPlus()) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, PERMISSION_WRITE_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("InlinedApi")
    fun handleStoragePermission(callback: (granted: Boolean) -> Unit) {
        actionOnPermission = null
        if (hasStoragePermission()) {
            callback(true)
        } else {
            if (isRPlus()) {
                // Simplified for now, should show a dialog first in a real app
                isAskingPermissions = true
                actionOnPermission = callback
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResult(intent, MANAGE_STORAGE_RC)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivityForResult(intent, MANAGE_STORAGE_RC)
                }
            } else {
                actionOnPermission = callback
                ActivityCompat.requestPermissions(this, arrayOf(PERMISSION_WRITE_STORAGE), 100)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            actionOnPermission?.invoke(granted)
        }
    }
    
    // Stub for setupEdgeToEdge if used
    fun setupEdgeToEdge(padBottomImeAndSystem: List<android.view.View> = emptyList()) {
        // Implementation for edge-to-edge
    }
    
    open fun onBackPressedCompat(): Boolean = false

    override fun onBackPressed() {
        if (!onBackPressedCompat()) {
            super.onBackPressed()
        }
    }
}
