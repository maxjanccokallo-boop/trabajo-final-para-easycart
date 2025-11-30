package com.example.easycart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.easycart.viewmodel.MainViewModel

@Composable
fun AppHeader(
    viewModel: MainViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val ui = viewModel.uiState.collectAsState().value
    val email = ui.user?.email ?: "Usuario"
    val name = email.substringBefore("@")

    val bg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFEFF1F5)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            // ● Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "● Sistema operativo",
                    color = Color(0xFF10B981),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ☀️ Botón modo claro/oscuro
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.DarkMode,
                    contentDescription = "Cambiar tema",
                    tint = if (isDarkMode) Color.White else Color(0xFF0F172A)
                )
            }

            // ⚙️ Configuración
            IconButton(onClick = { /* abrir settings */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = if (isDarkMode) Color.White else Color(0xFF0F172A)
                )
            }
        }
    }
}
