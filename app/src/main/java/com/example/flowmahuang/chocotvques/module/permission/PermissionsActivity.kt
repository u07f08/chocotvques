package com.example.flowmahuang.chocotvques.module.permission

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.example.flowmahuang.kotlinpractice.module.permission.PermissionsChecker

@TargetApi(Build.VERSION_CODES.M)
class PermissionsActivity : AppCompatActivity() {
    private val PERMISSIONS_ACCEPT = 0
    private val PERMISSIONS_REFUSE = 1

    private val PERMISSION_REQUEST_CODE = 0
    private val EXTRA_PERMISSIONS = "permission"

    private var permissionsChecker: PermissionsChecker? = null
    private var isRequireCheck: Boolean = false

    companion object {
        fun startPermissionsForResult(activity: Activity, requestCode: Int, vararg permissions: String) {
            val intent = Intent(activity, PermissionsActivity::class.java)
            intent.putExtra("permission", permissions)
            ActivityCompat.startActivityForResult(activity, intent, requestCode, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent == null || !intent.hasExtra(EXTRA_PERMISSIONS)) {
            throw RuntimeException("PermissionsActivity need to use a static method startPermissionsForResult to start!")
        }

        permissionsChecker = PermissionsChecker(this)
        isRequireCheck = true
    }

    override fun onResume() {
        super.onResume()
        if (isRequireCheck) {
            val permissions = getExtraPermissions()
            if (permissionsChecker!!.missingPermissions(*permissions)) {
                requestPermissions(permissions, PERMISSION_REQUEST_CODE)
            } else {
                allPermissionsAccept()
            }
        } else {
            isRequireCheck = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE && isAllPermissionsAccept(grantResults)) {
            isRequireCheck = true
            allPermissionsAccept()
        } else {
            isRequireCheck = false
            showMissingPermissionDialog()
        }
    }

    private fun getExtraPermissions(): Array<String> {
        return intent.getStringArrayExtra(EXTRA_PERMISSIONS)
    }

    private fun allPermissionsAccept() {
        setResult(PERMISSIONS_ACCEPT)
        finish()
    }

    private fun isAllPermissionsAccept(grant: IntArray): Boolean {
        return !grant.contains(PackageManager.PERMISSION_DENIED)
    }

    private fun startSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + packageName)
        startActivity(intent)
    }

    private fun showMissingPermissionDialog() {
        val builder = AlertDialog.Builder(this@PermissionsActivity)
        builder.setTitle("Help")
        builder.setMessage("Missed needing permissions ")

        builder.setNegativeButton("Quit", { dialog, which ->
            setResult(PERMISSIONS_REFUSE)
            finish()
        })

        builder.setPositiveButton("Settings", { dialog, which ->
            startSettings()
        })

        builder.show()
    }
}