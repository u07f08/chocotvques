package com.example.flowmahuang.kotlinpractice.module.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class PermissionsChecker(val context:Context) {
    /**
     * check every input permissions
     *
     * @param permissions needs permissions
     * @return if this application lose permissions return true else return false
     */
    fun missingPermissions(vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.asSequence()
                    .filter { context.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED }
                    .forEach { return true }
        }
        return false
    }
}