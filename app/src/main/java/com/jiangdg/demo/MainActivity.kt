package com.jiangdg.demo

import android.Manifest.permission.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.gyf.immersionbar.ImmersionBar
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.demo.databinding.ActivityMainBinding
import android.view.WindowManager

class MainActivity : AppCompatActivity() {
    private var immersionBar: ImmersionBar? = null
    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setStatusBar()
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        replaceDemoFragment(DemoFragment())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle USB device attachment when activity is already running
        intent?.let {
            if (it.action == "android.hardware.usb.action.USB_DEVICE_ATTACHED") {
                // USB device attached, refresh the current fragment if needed
                Log.d("MainActivity", "USB device attached via onNewIntent")
            }
        }
    }

    private fun replaceDemoFragment(fragment: Fragment) {
        val hasCameraPermission = PermissionChecker.checkSelfPermission(this, CAMERA)
        if (hasCameraPermission != PermissionChecker.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {
                ToastUtils.show(R.string.permission_tip)
            }
            ActivityCompat.requestPermissions(this, permissionList(), REQUEST_CAMERA)
            return
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commitAllowingStateLoss()
    }

    private fun permissionList(): Array<String> {
        return if(Build.VERSION.SDK_INT >= 30) {
            arrayOf(CAMERA, RECORD_AUDIO) // WRITE_EXTERNAL_STORAGE deprecated
        } else {
            arrayOf(CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA -> {
                val hasCameraPermission = PermissionChecker.checkSelfPermission(this, CAMERA)
                if (hasCameraPermission == PermissionChecker.PERMISSION_DENIED) {
                    ToastUtils.show(R.string.permission_tip)
                    return
                }

                val hasStoragePermission =
                    PermissionChecker.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                if (hasStoragePermission == PermissionChecker.PERMISSION_DENIED) {
                    ToastUtils.show(R.string.permission_tip)
                   // return
                }

//                replaceDemoFragment(DemoMultiCameraFragment())
                replaceDemoFragment(DemoFragment())
//                replaceDemoFragment(GlSurfaceFragment())
            }
            else -> {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        immersionBar= null
    }

    private fun setStatusBar() {
        immersionBar = ImmersionBar.with(this)
            .statusBarDarkFont(false)
            .statusBarColor(R.color.black)
            .navigationBarColor(R.color.black)
            .fitsSystemWindows(true)
            .keyboardEnable(true)
        immersionBar?.init()
    }

    companion object {
        private const val REQUEST_CAMERA = 0
    }
}