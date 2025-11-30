package com.example.easycart

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import com.example.easycart.bluetooth.BluetoothService
import com.example.easycart.di.AppModule
import com.example.easycart.ui.navigation.RootNavGraph
import com.example.easycart.ui.theme.EasyCartTheme
import com.example.easycart.viewmodel.AuthViewModel
import com.example.easycart.viewmodel.MainViewModel
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {

    private val btPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )

    private fun ensureBluetoothPermissions() {
        btPermissions.forEach { perm ->
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, btPermissions, 100)
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ensureBluetoothPermissions()
        AppModule.bluetoothService = BluetoothService(applicationContext)

        val repo = AppModule.repo
        val authVm = AuthViewModel(repo)
        val mainVm = MainViewModel(repo)

        setContent {

            // 🔥 Tema Global
            var darkMode by remember { mutableStateOf(true) }

            EasyCartTheme(darkTheme = darkMode) {

                val navController = rememberNavController()

                RootNavGraph(
                    navController = navController,
                    authViewModel = authVm,
                    mainViewModel = mainVm,
                    darkMode = darkMode,
                    onToggleTheme = { darkMode = !darkMode }
                )
            }
        }
    }
}
