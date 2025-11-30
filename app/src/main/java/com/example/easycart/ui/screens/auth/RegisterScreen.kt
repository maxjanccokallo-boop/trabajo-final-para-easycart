package com.example.easycart.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.easycart.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // 🎨 Paleta EasyCart
    val deepBlue = Color(0xFF1E3A8A)     // Azul carrito
    val softBlue = Color(0xFFEEF2FF)     // Fondo suave
    val greenCheck = Color(0xFF22C55E)   // Verde del check
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

            // 🔙 Volver
            TextButton(onClick = onBackToLogin) {
                Text("← Volver al inicio de sesión", color = deepBlue)
            }

            Spacer(Modifier.height(8.dp))

            // Título
            Text(
                "Crear Cuenta",
                style = MaterialTheme.typography.headlineLarge,
                color = deepBlue
            )

            Text(
                "Únete a EasyCart",
                style = MaterialTheme.typography.bodyMedium,
                color = grayText
            )

            Spacer(Modifier.height(20.dp))

            // Campos de registro
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Número de celular") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña (mínimo 6 caracteres)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Confirmar contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(Modifier.height(4.dp))

            // 👁 Mostrar/Ocultar contraseñas
            TextButton(onClick = { showPassword = !showPassword }) {
                Text(
                    if (showPassword) "Ocultar contraseñas" else "Mostrar contraseñas",
                    color = deepBlue
                )
            }

            Spacer(Modifier.height(12.dp))

            // 🟦 Botón Crear Cuenta
            Button(
                onClick = {
                    if (password.length >= 6 && password == confirm) {
                        viewModel.register(
                            fullName.trim(),
                            email.trim(),
                            password.trim(),
                            phone.trim(),
                            onRegisterSuccess
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = deepBlue,
                    contentColor = Color.White
                )
            ) {
                Text("Crear Cuenta")
            }

            // ❗ Error
            if (uiState.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    uiState.error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
