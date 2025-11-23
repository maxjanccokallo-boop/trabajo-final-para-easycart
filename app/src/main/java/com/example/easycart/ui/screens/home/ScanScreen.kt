package com.example.easycart.ui.screens.home

import android.content.pm.PackageManager
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easycart.di.AppModule
import com.example.easycart.viewmodel.LedState
import com.example.easycart.viewmodel.MainViewModel
import com.example.easycart.viewmodel.MainViewModelFactory
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import androidx.compose.ui.graphics.graphicsLayer

const val FIXED_BARCODE_LENGTH = 12

@Composable
fun ScanScreen(
    viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(AppModule.repo)),
    onScanSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var scannedText by remember { mutableStateOf("") }

    var showCameraScanner by remember { mutableStateOf(false) }
    var torchEnabled by remember { mutableStateOf(false) }
    var torchSupported by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // micro-animación para cards al cargar
    var appear by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appear = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // =========================
        // ✅ CARD USB (PRO)
        // =========================
        AnimatedVisibility(
            visible = appear,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500), initialOffsetY = { it / 2 })
        ) {
            SectionCard(
                title = "Escáner USB",
                subtitle = "Usa tu pistola de escaneo"
            ) {
                TextField(
                    value = scannedText,
                    onValueChange = { newCode ->
                        scannedText = newCode
                        val temp = newCode.trim()

                        if (temp.length == FIXED_BARCODE_LENGTH) {
                            viewModel.onBarcodeScanned(temp)
                            scannedText = ""
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else if (newCode.endsWith("\n")) {
                            val clean = newCode.trim()
                            if (clean.isNotEmpty()) {
                                viewModel.onBarcodeScanned(clean)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            scannedText = ""
                        }
                    },
                    label = { Text("Escanea aquí...") },
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
                        focusedContainerColor = Color(0xFFEDEAF6),
                        unfocusedContainerColor = Color(0xFFEDEAF6),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }

        // =========================
        // ✅ CARD CÁMARA (PRO)
        // =========================
        AnimatedVisibility(
            visible = appear,
            enter = fadeIn(tween(650)) + slideInVertically(tween(650), initialOffsetY = { it / 2 })
        ) {
            SectionCard(
                title = "Escáner por cámara",
                subtitle = "Presiona para abrir la cámara"
            ) {

                // Pulse del botón para verse más dinámico
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
                            .height(54.dp)
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
                                .size(54.dp)
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

                                    // beep mini tipo POS
                                    ToneGenerator(AudioManager.STREAM_MUSIC, 90)
                                        .startTone(ToneGenerator.TONE_PROP_BEEP, 150)

                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

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

        // =========================
        // ✅ RESULTADOS (PRO)
        // =========================
        AnimatedVisibility(
            visible = uiState.lastScanned != null || uiState.scanError != null,
            enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { it / 3 }),
            exit = fadeOut(tween(200))
        ) {
            SectionCard(
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
                        Spacer(Modifier.height(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("Código: $last") }
                        )
                    } else {
                        ErrorMessage(error ?: "Error desconocido")
                    }
                }
            }
        }

        // =========================
        // ✅ LED (PRO con animación)
        // =========================
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
            SectionCard(
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
}

@Composable
fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // animación de sombra suave
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
            Text(title, fontWeight = FontWeight.Black)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
    ) {
        Text("✔ $text", color = Color(0xFF2E7D32))
    }
}

@Composable
fun ErrorMessage(text: String) {
    Box(
        Modifier.fillMaxWidth()
            .background(Color(0xFFFFE0E0), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text("✖ $text", color = Color(0xFFC62828))
    }
}

// =====================================================================================
// ✅ PERMISO DE CÁMARA
// =====================================================================================
@Composable
fun RequestCameraPermission(
    onGranted: @Composable () -> Unit
) {
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

    // láser animado
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

                    analyzer.setAnalyzer(
                        ContextCompat.getMainExecutor(ctx)
                    ) { imageProxy ->

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
                                        if (code.length == FIXED_BARCODE_LENGTH) {
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

        // Frame verde pro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .border(2.dp, Color(0xFF22C55E), RoundedCornerShape(14.dp))
        )

        // Línea láser
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
