package com.example.easycart.ui.screens.home

import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.easycart.viewmodel.MainViewModel
import com.example.easycart.viewmodel.ScanEntry
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

const val FIXED_BARCODE_LENGTH = 12

// ================================
// 🎨 PALETA LIGHT / DARK
// ================================
private val LightHeaderGradient = Brush.horizontalGradient(
    listOf(Color(0xFF4A64F0), Color(0xFF8B5CF6))
)
private val DarkHeaderGradient = Brush.verticalGradient(
    listOf(Color(0xFF111827), Color(0xFF020617))
)

private val LightScreenBg = Brush.verticalGradient(
    listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF))
)
private val DarkScreenBg = Brush.verticalGradient(
    listOf(Color(0xFF020617), Color(0xFF020617))
)

// ================================
// ⭐ PANTALLA PRINCIPAL
// ================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ScanScreen(
    viewModel: MainViewModel,
    onScanSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var scannedText by remember { mutableStateOf("") }
    var showCameraScanner by remember { mutableStateOf(false) }
    var torchEnabled by remember { mutableStateOf(false) }
    var torchSupported by remember { mutableStateOf(false) }

    // ⚙️ Settings Sheet
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Configs locales
    var soundEnabled by rememberSaveable { mutableStateOf(true) }
    var hapticEnabled by rememberSaveable { mutableStateOf(true) }
    var accept13Digits by rememberSaveable { mutableStateOf(false) }

    // Tema claro / oscuro (empieza en oscuro como la captura)
    var isDarkMode by rememberSaveable { mutableStateOf(true) }

    // Idioma (solo visual)
    var currentLanguage by rememberSaveable { mutableStateOf("Español") }

    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // animación entrada
    var appear by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appear = true }

    // Responsive simple
    val widthDp = LocalConfiguration.current.screenWidthDp
    val horizontalPad = if (widthDp < 400) 12.dp else 16.dp

    // Fondo según tema
    val screenBg = if (isDarkMode) DarkScreenBg else LightScreenBg

    // ======================================================
    // ✅ MÉTRICAS REALES
    // ======================================================
    val history = uiState.scanHistory
    val totalScans = history.size
    val successScans = history.count { it.success }
    val successRate = if (totalScans == 0) 0 else ((successScans * 100.0) / totalScans).roundToInt()

    val todayScans = remember(history) {
        val calNow = Calendar.getInstance()
        history.count {
            val calIt = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            calNow.get(Calendar.YEAR) == calIt.get(Calendar.YEAR) &&
                    calNow.get(Calendar.DAY_OF_YEAR) == calIt.get(Calendar.DAY_OF_YEAR)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPad, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ======================================================
        // ✅ HEADER PRO + ⚙️ + botón tema
        // ======================================================
        AnimatedVisibility(
            visible = appear,
            enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            ScanHeaderCard(
                today = todayScans,
                total = totalScans,
                successRate = successRate,
                isDarkMode = isDarkMode,
                currentLanguage = currentLanguage,
                onToggleTheme = { isDarkMode = !isDarkMode },
                onSettingsClick = { showSettings = true }
            )
        }

        // ======================================================
        // ✅ 1) ESCÁNER POR CÁMARA (PRIMERO)
        // ======================================================
        AnimatedVisibility(
            visible = appear,
            enter = fadeIn(tween(650)) + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            SectionCardPro(
                icon = Icons.Default.CameraAlt,
                title = "Escáner por cámara",
                subtitle = "Usa la cámara de tu dispositivo",
                isDarkMode = isDarkMode
            ) {

                val pulseInf = rememberInfiniteTransition(label = "pulse")
                val pulseScale by pulseInf.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.03f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                val camButtonColor = if (isDarkMode) Color(0xFF4F46E5) else Color(0xFF1976D2)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showCameraScanner = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .graphicsLayer {
                                scaleX = if (!showCameraScanner) pulseScale else 1f
                                scaleY = if (!showCameraScanner) pulseScale else 1f
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = camButtonColor)
                    ) {
                        Icon(Icons.Default.CenterFocusWeak, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Iniciar escaneo", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    if (torchSupported) {
                        IconButton(
                            onClick = { torchEnabled = !torchEnabled },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDarkMode) Color(0xFF020617) else Color(0xFF111827))
                        ) {
                            Icon(
                                imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showCameraScanner,
                    enter = fadeIn(tween(250)) + expandVertically(),
                    exit = fadeOut(tween(200)) + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RequestCameraPermission {
                            CameraScanner(
                                torchEnabled = torchEnabled,
                                onTorchSupported = { torchSupported = it },
                                onDetected = { code ->
                                    showCameraScanner = false
                                    torchEnabled = false

                                    if (soundEnabled) {
                                        ToneGenerator(AudioManager.STREAM_MUSIC, 90)
                                            .startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                    }
                                    if (hapticEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }

                                    viewModel.onBarcodeScanned(code)
                                    onScanSuccess()
                                }
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                showCameraScanner = false
                                torchEnabled = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isDarkMode) Color(0xFFE5E7EB) else Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Cancelar cámara")
                        }
                    }
                }
            }
        }

        // ======================================================
        // ✅ 2) ESTADO DE CONEXIONES
        // ======================================================
        AnimatedVisibility(
            visible = appear,
            enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            ConnectionsCard(
                usbConnected = null,
                btConnected = null,
                isDarkMode = isDarkMode
            )
        }

        // ======================================================
        // ✅ 3) ESCÁNER USB
        // ======================================================
        AnimatedVisibility(
            visible = appear,
            enter = fadeIn(tween(700)) + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            SectionCardPro(
                icon = Icons.Default.Usb,
                title = "Escáner USB",
                subtitle = "Usa tu pistola de escaneo",
                isDarkMode = isDarkMode
            ) {
                val textFieldBg = if (isDarkMode) Color(0xFF020617) else Color(0xFFF1F0FF)
                val textColor = if (isDarkMode) Color.White else Color.Black

                TextField(
                    value = scannedText,
                    onValueChange = { newCode ->
                        scannedText = newCode
                        val temp = newCode.trim()

                        val validLen = temp.length == FIXED_BARCODE_LENGTH ||
                                (accept13Digits && temp.length == 13)

                        if (validLen) {
                            viewModel.onBarcodeScanned(temp)
                            scannedText = ""
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else if (newCode.endsWith("\n")) {
                            val clean = newCode.trim()
                            if (clean.isNotEmpty()) {
                                viewModel.onBarcodeScanned(clean)
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            scannedText = ""
                        }
                    },
                    label = {
                        Text(
                            "Escanea o ingresa código...",
                            color = if (isDarkMode) Color(0xFF9CA3AF) else Color.Gray
                        )
                    },
                    placeholder = {
                        Text(
                            "Esperando escaneo...",
                            color = if (isDarkMode) Color(0xFF6B7280) else Color.Gray
                        )
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.CenterFocusWeak, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .focusable(true),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = textFieldBg,
                        unfocusedContainerColor = textFieldBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = if (isDarkMode) Color(0xFF60A5FA) else Color(0xFF4A64F0)
                    )
                )

                Spacer(Modifier.height(10.dp))

                val usbButtonBg =
                    if (isDarkMode) Color(0xFF020617) else Color(0xFF7B61FF).copy(alpha = 0.15f)
                val usbTextColor = if (isDarkMode) Color(0xFF818CF8) else Color(0xFF7B61FF)

                Button(
                    onClick = { /* acción futura */ },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = usbButtonBg,
                        disabledContainerColor = usbButtonBg
                    )
                ) {
                    Text("⚡ Escanear código", color = usbTextColor, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ======================================================
        // ✅ 4) HISTORIAL DE ESCANEOS
        // ======================================================
        SectionCardPro(
            icon = Icons.Default.History,
            title = "Historial de escaneos",
            subtitle = "Últimos códigos leídos",
            isDarkMode = isDarkMode
        ) {
            if (history.isEmpty()) {
                Text(
                    "No hay escaneos aún",
                    color = if (isDarkMode) Color(0xFF9CA3AF) else Color.Gray
                )
            } else {
                history.take(15).forEach { entry ->
                    HistoryRow(entry, isDarkMode)
                    Spacer(Modifier.height(8.dp))
                }

                if (history.size > 15) {
                    Text(
                        "Mostrando 15 de ${history.size}",
                        color = if (isDarkMode) Color(0xFF9CA3AF) else Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { viewModel.clearScanHistory() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDarkMode) Color(0xFFE5E7EB) else Color.Black
                    )
                ) {
                    Text("Limpiar historial")
                }
            }
        }
    }

    // ======================================================
    // ✅ SETTINGS SHEET FUNCIONAL (⚙️)
    // ======================================================
    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = settingsSheetState,
            containerColor = if (isDarkMode) Color(0xFF020617) else Color.White
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Configuración",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDarkMode) Color.White else Color.Black
                )

                // Idioma
                Text(
                    "Idioma de la app",
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) Color(0xFFE5E7EB) else Color.Black
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = currentLanguage == "Español",
                        onClick = { currentLanguage = "Español" },
                        label = { Text("Español") }
                    )
                    FilterChip(
                        selected = currentLanguage == "English",
                        onClick = { currentLanguage = "English" },
                        label = { Text("English") }
                    )
                }

                Divider(Modifier.padding(vertical = 8.dp))

                Text(
                    "Preferencias de escaneo",
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) Color(0xFFE5E7EB) else Color.Black
                )

                SettingSwitch(
                    title = "Sonido al escanear",
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )

                SettingSwitch(
                    title = "Vibración (haptics)",
                    checked = hapticEnabled,
                    onCheckedChange = { hapticEnabled = it }
                )

                SettingSwitch(
                    title = "Aceptar códigos de 13 dígitos",
                    checked = accept13Digits,
                    onCheckedChange = { accept13Digits = it }
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { showSettings = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Listo")
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// ======================================================
// ✅ HEADER PRO
// ======================================================
@Composable
private fun ScanHeaderCard(
    today: Int,
    total: Int,
    successRate: Int,
    isDarkMode: Boolean,
    currentLanguage: String,
    onToggleTheme: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val gradient = if (isDarkMode) DarkHeaderGradient else LightHeaderGradient

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Avatar + nombre + estado
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Juan Pérez",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF22C55E), CircleShape)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Sistema operativo • $currentLanguage",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Botón tema claro / oscuro
                    HeaderIconButton(
                        icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        onClick = onToggleTheme
                    )
                    Spacer(Modifier.width(8.dp))
                    // Botón settings (idioma + opciones)
                    HeaderIconButton(Icons.Default.Settings, onClick = onSettingsClick)
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStatCard("Hoy", today.toString())
                    MiniStatCard("Total", total.toString())
                    MiniStatCard("Precisión", "$successRate%")
                }
            }
        }
    }
}

@Composable
private fun HeaderIconButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White)
    }
}

@Composable
fun MiniStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(130.dp)
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ======================================================
// ✅ HISTORIAL UI
// ======================================================
@Composable
private fun HistoryRow(entry: ScanEntry, isDarkMode: Boolean) {
    val dateFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val time = dateFmt.format(Date(entry.timestamp))

    val bg = if (entry.success) {
        if (isDarkMode) Color(0xFF052E16) else Color(0xFFE8FFF0)
    } else {
        if (isDarkMode) Color(0xFF450A0A) else Color(0xFFFFE6E6)
    }

    val fg = if (entry.success) {
        if (isDarkMode) Color(0xFF6EE7B7) else Color(0xFF2E7D32)
    } else {
        if (isDarkMode) Color(0xFFFCA5A5) else Color(0xFFD32F2F)
    }

    val secondary = if (isDarkMode) Color(0xFF9CA3AF) else Color.Gray

    Row(
        Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            time,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(70.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.label,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )
            Text(
                "Código: ${entry.barcode}",
                style = MaterialTheme.typography.bodySmall,
                color = secondary
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(if (entry.success) "OK" else "FAIL", color = fg, fontWeight = FontWeight.Bold)
    }
}

// ======================================================
// ✅ CONEXIONES (VISUAL)
// ======================================================
@Composable
private fun ConnectionsCard(
    usbConnected: Boolean?,
    btConnected: Boolean?,
    isDarkMode: Boolean
) {
    SectionCardPro(
        icon = Icons.Default.Wifi,
        title = "Estado de Conexiones",
        subtitle = "Dispositivos conectados",
        isDarkMode = isDarkMode
    ) {
        ConnectionRow(Icons.Default.Usb, "Escáner USB", usbConnected, isDarkMode)
        Spacer(Modifier.height(8.dp))
        ConnectionRow(Icons.Default.Bluetooth, "Bluetooth", btConnected, isDarkMode)
    }
}

@Composable
private fun ConnectionRow(
    icon: ImageVector,
    label: String,
    connected: Boolean?,
    isDarkMode: Boolean
) {
    val (dotColor, statusText, statusColor) = when (connected) {
        true -> Triple(Color(0xFF22C55E), "Conectado", Color(0xFF4ADE80))
        false -> Triple(Color(0xFF9CA3AF), "Desconectado", Color(0xFF9CA3AF))
        null -> Triple(Color(0xFF9CA3AF), "Sin información", Color(0xFF9CA3AF))
    }

    val iconBg = if (isDarkMode) Color(0xFF020617) else Color(0xFFF3F4F6)
    val labelColor = if (isDarkMode) Color.White else Color.Black

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF111827))
        }

        Spacer(Modifier.width(10.dp))

        Text(label, Modifier.weight(1f), fontWeight = FontWeight.Medium, color = labelColor)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(statusText, color = statusColor, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ======================================================
// ✅ SECTION CARD PRO
// ======================================================
@Composable
fun SectionCardPro(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isDarkMode: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val elev by animateDpAsState(
        targetValue = 8.dp,
        animationSpec = tween(600),
        label = "cardElev"
    )

    val cardColor = if (isDarkMode) Color(0xFF0F172A) else Color.White
    val iconBg = if (isDarkMode) Color(0xFF020617) else Color(0xFFF3F0FF)
    val iconTint = if (isDarkMode) Color(0xFF6366F1) else Color(0xFF7B61FF)
    val titleColor = if (isDarkMode) Color.White else Color.Black
    val subtitleColor = if (isDarkMode) Color(0xFF9CA3AF) else Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(elev),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconTint)
                }

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Black, color = titleColor)
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor
                        )
                    }
                }
            }

            content()
        }
    }
}

// ======================================================
// ✅ SWITCH REUTILIZABLE
// ======================================================
@Composable
private fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// =====================================================================================
// ✅ PERMISO DE CÁMARA
// =====================================================================================
@Composable
fun RequestCameraPermission(onGranted: @Composable () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(android.Manifest.permission.CAMERA)
    }

    if (hasPermission) onGranted()
}

// =====================================================================================
// ✅ CÁMARA PRO con overlay + línea láser
// =====================================================================================
@Composable
fun CameraScanner(
    torchEnabled: Boolean,
    onTorchSupported: (Boolean) -> Unit,
    onDetected: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScanning by remember { mutableStateOf(true) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }

    val infinite = rememberInfiniteTransition(label = "scanLine")
    val scanY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "scanY"
    )

    LaunchedEffect(torchEnabled) {
        cameraRef?.cameraControl?.enableTorch(torchEnabled)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({

                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val selector = CameraSelector.DEFAULT_BACK_CAMERA
                    val analyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val scanner = BarcodeScanning.getClient()

                    analyzer.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->

                        if (!isScanning) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val img = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            scanner.process(img)
                                .addOnSuccessListener { codes ->
                                    codes.firstOrNull()?.rawValue?.let { code ->
                                        if (code.isNotBlank()) {
                                            isScanning = false
                                            onDetected(code)

                                            Handler(Looper.getMainLooper()).postDelayed({
                                                isScanning = true
                                            }, 1500)
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, selector, preview, analyzer
                    )
                    cameraRef = camera
                    onTorchSupported(camera.cameraInfo.hasFlashUnit())
                    camera.cameraControl.enableTorch(torchEnabled)

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .border(2.dp, Color(0xFF22C55E), RoundedCornerShape(14.dp))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.TopCenter)
                .offset(y = (scanY * 220).dp)
                .background(Color(0xFF22C55E))
        )
    }
}
