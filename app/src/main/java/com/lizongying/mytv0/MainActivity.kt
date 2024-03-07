package com.lizongying.mytv0

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.lizongying.mytv0.models.TVList


class MainActivity : FragmentActivity() {

    private var playerFragment = PlayerFragment()
    private var infoFragment = InfoFragment()
    private var channelFragment = ChannelFragment()
    private var menuFragment = MenuFragment()
    private lateinit var settingFragment: SettingFragment

    private val handler = Handler(Looper.myLooper()!!)
    private val delayHideMenu = 10000L
    private val delayHideSetting = 10000L

    private var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.main_browse_fragment, playerFragment)
                .add(R.id.main_browse_fragment, infoFragment)
                .add(R.id.main_browse_fragment, channelFragment)
                .add(R.id.main_browse_fragment, menuFragment)
                .hide(menuFragment)
                .commitNow()
        }

        TVList.listModel.forEach { tvModel ->
            tvModel.errInfo.observe(this) { _ ->
                if (tvModel.errInfo.value != null
                    && tvModel.tv.id == TVList.position.value
                ) {
                    Toast.makeText(this, tvModel.errInfo.value, Toast.LENGTH_LONG)
                        .show()
                }
            }
            tvModel.ready.observe(this) { _ ->

                // not first time && channel is not changed
                if (tvModel.ready.value != null
                    && tvModel.tv.id == TVList.position.value
                ) {
                    Log.i(TAG, "info ${tvModel.tv.title}")
                    infoFragment.show(tvModel)
                    if (SP.channelNum) {
                        channelFragment.show(tvModel)
                    }
                }
            }
        }

        TVList.setPosition(SP.position)

        val packageInfo = getPackageInfo()
        val versionName = packageInfo.versionName
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
        settingFragment = SettingFragment(versionName, versionCode)
    }

    fun play(position: Int) {
        if (position > -1 && position < TVList.size()) {
            TVList.setPosition(position)
        } else {
            Toast.makeText(this, "频道不存在", Toast.LENGTH_LONG).show()
        }
    }

    fun prev() {
        position = TVList.position.value?.dec() ?: 0
        if (position == -1) {
            position = TVList.size() - 1
        }
        TVList.setPosition(position)
    }

    fun next() {
        position = TVList.position.value?.inc() ?: 0
        if (position == TVList.size()) {
            position = 0
        }
        TVList.setPosition(position)
    }

    fun menuActive() {
        handler.removeCallbacks(hideMenu)
        handler.postDelayed(hideMenu, delayHideMenu)
    }

    val hideMenu = Runnable {
        if (!menuFragment.isHidden) {
            supportFragmentManager.beginTransaction().hide(menuFragment).commit()
        }
    }

    fun settingActive() {
        handler.removeCallbacks(hideSetting)
        handler.postDelayed(hideSetting, delayHideSetting)
    }

    private val hideSetting = Runnable {
        if (!settingFragment.isHidden) {
            supportFragmentManager.beginTransaction().hide(settingFragment).commit()
        }
    }

    private fun getPackageInfo(): PackageInfo {
        val flag = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNATURES
        } else {
            PackageManager.GET_SIGNING_CERTIFICATES
        }

        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, flag)
        } else {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
            )
        }
    }

    private fun showChannel(channel: String) {
        if (!menuFragment.isHidden) {
            return
        }

        if (settingFragment.isVisible) {
            return
        }

        if (SP.channelNum) {
            channelFragment.show(channel)
        }
    }


    private fun channelUp() {
        if (menuFragment.isHidden) {
            if (SP.channelReversal) {
                next()
                return
            }
            prev()
        }
    }

    private fun channelDown() {
        if (menuFragment.isHidden) {
            if (SP.channelReversal) {
                prev()
                return
            }
            next()
        }
    }

    private fun back() {
        if (!menuFragment.isHidden) {
            hideMenuFragment()
            return
        }
//
//        if (doubleBackToExitPressedOnce) {
//            super.onBackPressed()
//            return
//        }
//
//        doubleBackToExitPressedOnce = true
//        Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show()
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            doubleBackToExitPressedOnce = false
//        }, 2000)
    }


    private fun showSetting() {
        if (!menuFragment.isHidden) {
            return
        }


//        Log.i(TAG, "settingFragment ${settingFragment.isVisible}")
//        if (!settingFragment.isVisible) {
//            settingFragment.show(supportFragmentManager, "setting")
//            settingActive()
//        } else {
//            handler.removeCallbacks(hideSetting)
//            settingFragment.dismiss()
//        }
    }

    fun switchMainFragment() {
        val transaction = supportFragmentManager.beginTransaction()

        if (menuFragment.isHidden) {
//            menuFragment.setPosition()
            transaction.show(menuFragment)
            menuActive()
        } else {
            transaction.hide(menuFragment)
        }

        transaction.commit()
    }


    private fun showMenuFragment() {
        supportFragmentManager.beginTransaction()
            .show(menuFragment)
            .commitNow()
        menuActive()
    }

    fun hideMenuFragment() {
        supportFragmentManager.beginTransaction()
            .hide(menuFragment)
            .commit()
    }

    fun onKey(keyCode: Int): Boolean {
        Log.d(TAG, "keyCode $keyCode")
        when (keyCode) {
            KeyEvent.KEYCODE_0 -> {
                showChannel("0")
                return true
            }

            KeyEvent.KEYCODE_1 -> {
                showChannel("1")
                return true
            }

            KeyEvent.KEYCODE_2 -> {
                showChannel("2")
                return true
            }

            KeyEvent.KEYCODE_3 -> {
                showChannel("3")
                return true
            }

            KeyEvent.KEYCODE_4 -> {
                showChannel("4")
                return true
            }

            KeyEvent.KEYCODE_5 -> {
                showChannel("5")
                return true
            }

            KeyEvent.KEYCODE_6 -> {
                showChannel("6")
                return true
            }

            KeyEvent.KEYCODE_7 -> {
                showChannel("7")
                return true
            }

            KeyEvent.KEYCODE_8 -> {
                showChannel("8")
                return true
            }

            KeyEvent.KEYCODE_9 -> {
                showChannel("9")
                return true
            }

            KeyEvent.KEYCODE_ESCAPE -> {
                back()
                return true
            }

            KeyEvent.KEYCODE_BACK -> {
                back()
                return true
            }

            KeyEvent.KEYCODE_BOOKMARK -> {
                showSetting()
                return true
            }

            KeyEvent.KEYCODE_UNKNOWN -> {
                showSetting()
                return true
            }

            KeyEvent.KEYCODE_HELP -> {
                showSetting()
                return true
            }

            KeyEvent.KEYCODE_SETTINGS -> {
                showSetting()
                return true
            }

            KeyEvent.KEYCODE_MENU -> {
                showSetting()
                return true
            }

            KeyEvent.KEYCODE_ENTER -> {
                switchMainFragment()
            }

            KeyEvent.KEYCODE_DPAD_CENTER -> {
                switchMainFragment()
            }

            KeyEvent.KEYCODE_DPAD_UP -> {
                channelUp()
            }

            KeyEvent.KEYCODE_CHANNEL_UP -> {
                channelUp()
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                channelDown()
            }

            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                channelDown()
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                showMenuFragment()
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                channelDown()
            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (onKey(keyCode)) {
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}