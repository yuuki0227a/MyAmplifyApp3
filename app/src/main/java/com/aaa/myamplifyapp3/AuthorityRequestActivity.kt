package com.aaa.myamplifyapp3

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class AuthorityRequestActivity : AppCompatActivity() {

    private val mIsGrantedMap: MutableMap<String, Boolean> = mutableMapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 権限要求
        requestPermission()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value
            mIsGrantedMap[permissionName] = isGranted
        }
        // 権限がすべて許可されていた場合は待機画面に遷移する。
        if (checkAllPermissionsGranted()) {
            showStandbyScreenActivity()
        } else {
            Toast.makeText(
                this,
                "権限を許可してください。",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }

    }

    private fun checkAllPermissionsGranted(): Boolean {
        // すべての値がtrueかどうかを確認
        return mIsGrantedMap.values.all { it }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                // 通知
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.FOREGROUND_SERVICE,
            )
        )
    }

    private fun showStandbyScreenActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}