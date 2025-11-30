package com.example.easycart.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.easycart.R
import com.example.easycart.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var useEmail by remember { mutableStateOf(true) }

    // 🎨 Paleta EasyCart
    val deepBlue = Color(0xFF1E3A8A)     // Azul del carrito
    val softBlue = Color(0xFFEEF2FF)     // Fondo suave
    val grayText = Color(0xFF6B7280)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(softBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {

            // 🛒 Logo real
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .background(
                        color = softBlue,
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.easycart_logo),
                    contentDescription = "Logo EasyCart",
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "EasyCart",
                style = MaterialTheme.typography.headlineLarge,
                color = deepBlue
            )

            Text(
                "Carrito Inteligente",
                style = MaterialTheme.typography.bodyMedium,
                color = grayText
            )

            Spacer(Modifier.height(24.dp))

            // 🔄 Toggle Correo / Celular
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE5E7EB), RoundedCornerShape(50.dp))
                    .padding(4.dp)
            ) {
                FilterChip(
                    selected = useEmail,
                    onClick = { useEmail = true },
                    label = { Text("Correo") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = deepBlue,
                        selectedLabelColor = Color.White,
                        containerColor = Color.Transparent
                    )
                )
                Spacer(Modifier.width(4.dp))
                FilterChip(
                    selected = !useEmail,
                    onClick = { useEmail = false },
                    label = { Text("Celular") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = deepBlue,
                        selectedLabelColor = Color.White,
                        containerColor = Color.Transparent
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            // ✏ Inputs
            if (useEmail) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Número de celular") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showPassword = !showPassword }) {
                        Text(if (showPassword) "Ocultar" else "Ver")
                    }
                }
            )

            Spacer(Modifier.height(18.dp))

            // 🔐 Botón Login
            Button(
                onClick = {
                    viewModel.login(email.trim(), password.trim(), onLoginSuccess)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = deepBlue,
                    contentColor = Color.White
                )
            ) {
                Text("Iniciar Sesión")
            }

            if (uiState.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            // 👉 Crear cuenta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("¿No tienes una cuenta?")
                TextButton(onClick = onGoToRegister) {
                    Text("Regístrate aquí", color = deepBlue)
                }
            }
        }
    }
}
