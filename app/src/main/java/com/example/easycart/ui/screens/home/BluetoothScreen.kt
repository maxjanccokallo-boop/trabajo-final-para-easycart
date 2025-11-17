package com.example.easycart.ui.screens.home

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easycart.viewmodel.BluetoothViewModel
import com.example.easycart.data.model.BtDeviceUi
@Composable
fun BluetoothScreen(vm: BluetoothViewModel = viewModel()) {

    val context = LocalContext.current
    val manager = context.getSystemService(BluetoothManager::class.java)
    val adapter = manager.adapter

    // -------- PERMISOS --------
    var permissionsGranted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        permissionsGranted = granted.values.all { it }
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    if (!permissionsGranted) {
        Text("Solicitando permisos...", modifier = Modifier.padding(20.dp))
        return
    }

    // -------- LISTA DE DISPOSITIVOS --------
    var devices by remember { mutableStateOf(listOf<BtDeviceUi>()) }

    LaunchedEffect(true) {
        val bonded = try {
            adapter?.bondedDevices?.map {
                BtDeviceUi(
                    it.name ?: "Dispositivo",
                    it.address,
                    (80..99).random(),
                    (70..100).random()
                )
            } ?: emptyList()
        } catch (_: SecurityException) {
            emptyList()
        }

        devices = bonded
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ---------- HEADER ----------
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(Color(0xFF1E88E5)),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Bluetooth, null, tint = Color.White, modifier = Modifier.size(38.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Bluetooth", color = Color.White, fontSize = 20.sp)
                    Text("Conexión Arduino", color = Color.White.copy(0.8f))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Dispositivos Disponibles", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        devices.forEach { dev ->
            DeviceCard(
                dev = dev,
                onConnect = {
                    vm.connect(dev.address) { err ->
                        vm.disconnect()
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun DeviceCard(dev: BtDeviceUi, onConnect: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(dev.name, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(4.dp))

            Text("MAC: ${dev.address}", color = Color.Gray)

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Señal: ${dev.signal}%", color = Color(0xFF1565C0))
                Text("Batería: ${dev.battery}%", color = Color(0xFF00C853))
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = { onConnect() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Conectar")
            }
        }
    }
}
