package com.example.easycart.ui.screens.home

import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
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
import com.example.easycart.viewmodel.LedState
import com.example.easycart.viewmodel.MainViewModel
import com.example.easycart.viewmodel.ScanEntry
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import androidx.compose.runtime.saveable.rememberSaveable

const val FIXED_BARCODE_LENGTH = 12

// ================================
// 🎨 ESTILOS
// ================================
private val ScanHeaderGradient = Brush.horizontalGradient(
    listOf(Color(0xFF4A64F0), Color(0xFF8B5CF6))
)
private val ScreenBg = Brush.verticalGradient(
    listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF))
)

// ================================
// ⭐ PANTALLA PRINCIPAL
// ================================
@OptIn(ExperimentalMaterial3Api::class)
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

    // Configs locales (no rompen lógica)
    var soundEnabled by rememberSaveable { mutableStateOf(true) }
    var hapticEnabled by rememberSaveable { mutableStateOf(true) }
    var accept13Digits by rememberSaveable { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // animación entrada
    var appear by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appear = true }

    // Responsive simple
    val widthDp = LocalConfiguration.current.screenWidthDp
    val horizontalPad = if (widthDp < 400) 12.dp else 16.dp

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
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPad, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ======================================================
        // ✅ HEADER PRO + ⚙️ funcional
        // ======================================================
        AnimatedVisibility(
            visible = appear,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500), initialOffsetY = { it / 2 })
        ) {
            ScanHeaderCard(
                today = todayScans,
                total = totalScans,
                successRate = successRate,
                onSettingsClick = { showSettings = true }
            )
        }

        // ======================================================
        // ✅ CARD CONEXIONES (VISUAL)
        // ======================================================
        AnimatedVisibility(
            visible = appear,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { it / 2 })
        ) {
            ConnectionsCard(
                usbConnected = null,
                btConnected = null
            )
        }

        // ======================================================
        // ✅ CARD USB (MISMA LÓGICA)
        // ======================================================
        AnimatedVisibility(
            visible = appear,
            enter = fadeIn(tween(650)) + slideInVertically(tween(650), initialOffsetY = { it / 2 })
        ) {
            SectionCardPro(
                icon = Icons.Default.Usb,
                title = "Escáner USB",
                subtitle = "Usa tu pistola de escaneo"
            ) {
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
                    label = { Text("Escanea aquí...") },
                    placeholder = { Text("Esperando escaneo...") },
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
                        focusedContainerColor = Color(0xFFF1F0FF),
                        unfocusedContainerColor = Color(0xFFF1F0FF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B61FF).copy(alpha = 0.15f),
                        disabledContainerColor = Color(0xFF7B61FF).copy(alpha = 0.15f)
                    )
                ) {
                    Text("⚡ Escanear con USB", color = Color(0xFF7B61FF), fontWeight = FontWeight.Bold)
                }
            }
        }

        // ======================================================
        // ✅ CARD CÁMARA (MISMA LÓGICA)
        // ======================================================
        AnimatedVisibility(
            visible = appear,
            enter = fadeIn(tween(700)) + slideInVertically(tween(700), initialOffsetY = { it / 2 })
        ) {
            SectionCardPro(
                icon = Icons.Default.Wifi,
                title = "Escáner por cámara",
                subtitle = "Usa la cámara de tu dispositivo"
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Icon(Icons.Default.CenterFocusWeak, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Escanear por cámara", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    if (torchSupported) {
                        IconButton(
                            onClick = { torchEnabled = !torchEnabled },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF111827))
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
                            shape = RoundedCornerShape(14.dp)
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
        // ✅ RESULTADOS ESCANEO (MISMA LÓGICA)
        // ======================================================
        AnimatedVisibility(
            visible = uiState.lastScanned != null || uiState.scanError != null,
            enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { it / 3 }),
            exit = fadeOut(tween(200))
        ) {
            SectionCardPro(
                icon = Icons.Default.Timelapse,
                title = "Resultado",
                subtitle = "Último escaneo"
            ) {
                AnimatedContent(
                    targetState = uiState.scanError to uiState.lastScanned,
                    transitionSpec = {
                        fadeIn(tween(200)) + slideInVertically(tween(250)) togetherWith
                                fadeOut(tween(150)) + slideOutVertically(tween(200))
                    },
                    label = "scanResultAnim"
                ) { (error, last) ->
                    if (error == null && last != null) {
                        SuccessMessage("Producto agregado: $last")
                    } else {
                        ErrorMessage(error ?: "Error desconocido")
                    }
                }
            }
        }

        // ======================================================
        // ✅ HISTORIAL DE ESCANEOS (NUEVO)
        // ======================================================
        SectionCardPro(
            icon = Icons.Default.History,
            title = "Historial de escaneos",
            subtitle = "Últimos códigos leídos"
        ) {
            if (history.isEmpty()) {
                Text("Aún no hay escaneos.", color = Color.Gray)
            } else {
                history.take(15).forEach { entry ->
                    HistoryRow(entry)
                    Spacer(Modifier.height(8.dp))
                }

                if (history.size > 15) {
                    Text("Mostrando 15 de ${history.size}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { viewModel.clearScanHistory() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Limpiar historial")
                }
            }
        }

        // ======================================================
        // ✅ LED (MISMA LÓGICA)
        // ======================================================
        val ledColor by animateColorAsState(
            targetValue = when (uiState.ledState) {
                LedState.GREEN -> Color(0xFF22C55E)
                LedState.RED -> Color(0xFFEF4444)
                LedState.YELLOW -> Color(0xFFF59E0B)
            },
            animationSpec = tween(450),
            label = "ledAnim"
        )

        AnimatedVisibility(
            visible = appear,
            enter = fadeIn(tween(800)) + slideInVertically(tween(800), initialOffsetY = { it / 2 })
        ) {
            SectionCardPro(
                icon = Icons.Default.Settings,
                title = "Estado LED",
                subtitle = "Conexión del carrito"
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier.size(18.dp)
                            .background(ledColor, CircleShape)
                    )
                    Text(uiState.ledState.name, fontWeight = FontWeight.Bold)
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
            sheetState = settingsSheetState
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Configuración de escaneo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

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
    onSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(ScanHeaderGradient)
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Escanear",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Modo rápido y preciso",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    HeaderIconButton(Icons.Default.Timelapse) { }
                    Spacer(Modifier.width(8.dp))
                    HeaderIconButton(Icons.Default.Settings) { onSettingsClick() }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStatCard("Hoy", today.toString())
                    MiniStatCard("Total", total.toString())
                    MiniStatCard("Éxito", "$successRate%")
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
private fun HistoryRow(entry: ScanEntry) {
    val dateFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val time = dateFmt.format(Date(entry.timestamp))

    val bg = if (entry.success) Color(0xFFE8FFF0) else Color(0xFFFFE6E6)
    val fg = if (entry.success) Color(0xFF2E7D32) else Color(0xFFD32F2F)

    Row(
        Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(time, color = fg, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.label, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Código: ${entry.barcode}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
    btConnected: Boolean?
) {
    SectionCardPro(
        icon = Icons.Default.Wifi,
        title = "Estado de Conexiones",
        subtitle = "Arduino / Pistola / Bluetooth"
    ) {
        ConnectionRow(Icons.Default.Usb, "Escáner USB", usbConnected)
        Spacer(Modifier.height(8.dp))
        ConnectionRow(Icons.Default.Bluetooth, "Bluetooth", btConnected)
    }
}

@Composable
private fun ConnectionRow(
    icon: ImageVector,
    label: String,
    connected: Boolean?
) {
    val (dotColor, statusText, statusColor) = when (connected) {
        true -> Triple(Color(0xFF22C55E), "Conectado", Color(0xFF16A34A))
        false -> Triple(Color(0xFF9CA3AF), "Desconectado", Color(0xFF6B7280))
        null -> Triple(Color(0xFF9CA3AF), "Sin info", Color(0xFF6B7280))
    }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF111827))
        }

        Spacer(Modifier.width(10.dp))

        Text(label, Modifier.weight(1f), fontWeight = FontWeight.Medium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(dotColor, CircleShape))
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
    content: @Composable ColumnScope.() -> Unit
) {
    val elev by animateDpAsState(
        targetValue = 8.dp,
        animationSpec = tween(600),
        label = "cardElev"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(elev),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                        .background(Color(0xFFF3F0FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color(0xFF7B61FF))
                }

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Black)
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            content()
        }
    }
}

@Composable
fun SuccessMessage(text: String) {
    Box(
        Modifier.fillMaxWidth()
            .background(Color(0xFFD4FCD4), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) { Text("✔ $text", color = Color(0xFF2E7D32)) }
}

@Composable
fun ErrorMessage(text: String) {
    Box(
        Modifier.fillMaxWidth()
            .background(Color(0xFFFFE0E0), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) { Text("✖ $text", color = Color(0xFFC62828)) }
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
