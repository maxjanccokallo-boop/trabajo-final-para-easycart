package com.example.easycart.ui.screens.home

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycart.data.model.BtDeviceUi
import com.example.easycart.viewmodel.BluetoothViewModel
import com.example.easycart.viewmodel.LedState
import com.example.easycart.viewmodel.MainViewModel
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.viewmodel.compose.viewModel

// ===============================================================
// 🎨 COLORES Y ESTILOS
// ===============================================================

private val BlueGradient = Brush.horizontalGradient(
    listOf(Color(0xFF0EA5E9), Color(0xFF3B82F6))
)

private val ScreenBg = Brush.verticalGradient(
    listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF))
)

private val CardBg = Color.White
private val BorderGray = Color(0xFFE5E7EB)
private val PrimaryBlue = Color(0xFF2563EB)

// ===============================================================
// ⭐ PANTALLA BLUETOOTH COMPLETA
// ===============================================================

@Composable
fun BluetoothScreen(mainViewModel: MainViewModel) {

    val vm: BluetoothViewModel = viewModel()

    LaunchedEffect(Unit) {
        // Obtener estado de conexión del servicio global
        vm.refreshState()
    }

    // LED STATE
    val uiState by mainViewModel.uiState.collectAsState()
    val ledState = uiState.ledState
    val ledColor = when (ledState) {
        LedState.RED -> Color(0xFFEF4444)
        LedState.YELLOW -> Color(0xFFFACC15)
        LedState.GREEN -> Color(0xFF22C55E)
    }

    // BLUETOOTH
    val ctx = LocalContext.current
    val manager = ctx.getSystemService(BluetoothManager::class.java)
    val adapter = manager.adapter

    var bluetoothEnabled by remember { mutableStateOf(adapter?.isEnabled == true) }

    // Activar BT
    val enableBtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        bluetoothEnabled = adapter?.isEnabled == true
    }

    fun requestEnableBluetooth() {
        if (adapter != null && !adapter.isEnabled) {
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    // PERMISOS
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionsGranted = result.values.all { it }
    }

    LaunchedEffect(Unit) {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms += Manifest.permission.BLUETOOTH_CONNECT
            perms += Manifest.permission.BLUETOOTH_SCAN
        }
        permissionLauncher.launch(perms.toTypedArray())
    }

    if (!permissionsGranted) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("Solicitando permisos…")
        }
        return
    }

    // LISTA DE DISPOSITIVOS
    var devices by remember { mutableStateOf(emptyList<BtDeviceUi>()) }

    fun loadBonded() {
        devices =
            adapter?.bondedDevices?.map {
                BtDeviceUi(
                    name = it.name ?: "Arduino",
                    address = it.address,
                    signal = (40..90).random(),
                    battery = (70..100).random()
                )
            } ?: emptyList()
    }

    LaunchedEffect(bluetoothEnabled) {
        if (bluetoothEnabled) loadBonded()
    }

    // ESTADO DE CONEXIÓN LOCAL
    var connectedAddress by remember { mutableStateOf<String?>(null) }
    var connectedName by remember { mutableStateOf<String?>(null) }
    var isConnecting by remember { mutableStateOf(false) }

    // ===============================================================
    // UI
    // ===============================================================

    Column(
        Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(16.dp)
    ) {

        // HEADER
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                Modifier
                    .background(BlueGradient)
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        Modifier
                            .size(44.dp)
                            .background(Color.White.copy(0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bluetooth, null, tint = Color.White)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text("Bluetooth", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Conexión Arduino", fontSize = 14.sp, color = Color.White.copy(0.9f))
                    }

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = {}) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .background(Color.White.copy(0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, null, tint = Color.White)
                        }
                    }
                }

                Row(
                    Modifier.align(Alignment.BottomEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (bluetoothEnabled) "Activado" else "Desactivado", color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = bluetoothEnabled,
                        onCheckedChange = {
                            if (it) requestEnableBluetooth()
                            else {
                                adapter?.disable()
                                connectedAddress = null
                                connectedName = null
                                vm.disconnect()
                            }
                            bluetoothEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.White.copy(0.3f),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.White.copy(0.2f)
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ESTADO DE CONEXIÓN
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(Color(0xFF1E7CF0).copy(alpha = 0.12f))
        ) {
            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    Modifier
                        .size(40.dp)
                        .background(Color.White.copy(0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (connectedAddress == null) Icons.Default.Close else Icons.Default.Check,
                        null, tint = Color.White
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        if (connectedAddress == null) "Desconectado" else "Conectado",
                        fontWeight = FontWeight.Bold
                    )
                    Text(connectedName ?: "Sin conexión activa", fontSize = 12.sp, color = Color.Gray)
                }

                if (connectedAddress != null) {
                    TextButton(onClick = {
                        connectedAddress = null
                        connectedName = null
                        vm.disconnect()
                    }) {
                        Text("Desconectar", color = Color.Red)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ESTADO DEL SENSOR
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(CardBg)
        ) {
            Column(Modifier.padding(14.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, null, tint = PrimaryBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("Estado del Sensor", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(12.dp))

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(Modifier.padding(14.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Box(
                                Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(ledColor)
                                    .border(4.dp, Color.White, CircleShape)
                            )

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text("LED", fontWeight = FontWeight.Bold)
                                Text(
                                    when (ledState) {
                                        LedState.RED -> "Esperando"
                                        LedState.YELLOW -> "Alerta"
                                        LedState.GREEN -> "Agregado"
                                    },
                                    color = Color.Gray, fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LedChip("Espera", ledState == LedState.RED)
                            LedChip("Agregado", ledState == LedState.GREEN)
                            LedChip("Alerta", ledState == LedState.YELLOW)
                            LedChip("Escaneo", false)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // DISPOSITIVOS DISPONIBLES
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WifiTethering, null, tint = PrimaryBlue)
                Spacer(Modifier.width(6.dp))
                Text("Dispositivos Disponibles", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))

            FilledTonalButton(
                onClick = { loadBonded() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(6.dp))
                Text("Buscar")
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(devices) { dev ->

                val isConnected = dev.address == connectedAddress

                DeviceCardPro(
                    dev = dev,
                    isConnected = isConnected,
                    isConnecting = isConnecting && isConnected,
                    onConnect = {
                        if (!bluetoothEnabled) {
                            requestEnableBluetooth()
                            return@DeviceCardPro
                        }

                        isConnecting = true
                        connectedAddress = dev.address
                        connectedName = dev.name

                        vm.connectToDevice(dev.address) { error ->
                            isConnecting = false
                            error?.let { println("BT_FLOW ERROR: $it") }
                        }
                    },
                    onDisconnect = {
                        connectedAddress = null
                        connectedName = null
                        vm.disconnect()
                    }
                )
            }
        }
    }
}

// ===============================================================
// CHIP DE LED
// ===============================================================

@Composable
private fun LedChip(label: String, selected: Boolean) {
    val bg = if (selected) Color(0xFFFDECEC) else Color(0xFFF8FAFC)
    val borderColor = if (selected) Color(0xFFFCA5A5) else BorderGray
    val dot = if (selected) Color(0xFFEF4444) else Color(0xFFD1D5DB)

    Column(
        Modifier
            .width(74.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dot)
        )
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp)
    }
}

// ===============================================================
// DEVICE CARD
// ===============================================================
@Composable
fun DeviceCardPro(
    dev: BtDeviceUi,
    isConnected: Boolean,
    isConnecting: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (isConnected) PrimaryBlue else BorderGray),
        colors = CardDefaults.cardColors(CardBg)
    ) {
        Column(
            Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DeveloperBoard, null, tint = PrimaryBlue)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(dev.name, fontWeight = FontWeight.Bold)
                    Text(dev.address, color = Color.Gray, fontSize = 12.sp)
                }
                if (isConnecting) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Button(onClick = if (isConnected) onDisconnect else onConnect) {
                        Text(if (isConnected) "Desconectar" else "Conectar")
                    }
                }
            }
        }
    }
}
