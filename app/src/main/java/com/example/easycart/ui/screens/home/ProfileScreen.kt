package com.example.easycart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.easycart.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit   // 👈 NECESARIO
)  {

    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F4F7))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // ----------- TARJETA PRINCIPAL AZUL ------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)),
            elevation = CardDefaults.cardElevation(6.dp),
            shape = RoundedCornerShape(20.dp)
        ) {

            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = user?.email ?: "Sin correo",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Miembro desde 17/11/2025",
                    color = Color(0xFFDBEAFE)
                )

                Spacer(Modifier.height(12.dp))

                HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat("Compras", uiState.cart.size.toString())
                    ProfileStat("Productos", uiState.cart.sumOf { it.quantity }.toString())
                    ProfileStat("Gastado", "S/ ${"%.2f".format(uiState.total)}")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ----------- SINCRONIZACIÓN DE DATOS ----------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {

            Column(Modifier.padding(20.dp)) {

                Text(
                    "Sincronización de Datos",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth()) {
                    SyncBox("Usuarios", "1", Color(0xFFF2F7F2), Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    SyncBox("Boletas", "0", Color(0xFFF0FAF2), Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth()) {
                    SyncBox("Productos", uiState.products.size.toString(), Color(0xFFF5F3FF), Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    SyncBox("Dispositivos BT", "4", Color(0xFFFFF6E5), Modifier.weight(1f))
                }

                Spacer(Modifier.height(20.dp))

                Text("Última sincronización:", fontWeight = FontWeight.Bold)
                Text("17/11/2025, 2:13:30 a. m.")

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("🔄  Sincronizar con Arduino")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED)
                    )
                ) {
                    Text("⬇  Exportar Datos")
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E7))
                ) {
                    Text(
                        "Los datos se guardan localmente. Para producción usa Firebase, Supabase o MongoDB.",
                        Modifier.padding(12.dp),
                        color = Color(0xFF8A6D3B)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ----------- MIS ESTADÍSTICAS ----------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {

            Column(Modifier.padding(20.dp)) {

                Text("Mis Estadísticas", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(12.dp))

                StatRow("🛒 Total de Compras", uiState.cart.size.toString())
                StatRow("📦 Productos Comprados", uiState.products.size.toString())
                StatRow("💸 Total Gastado", "S/ ${"%.2f".format(uiState.total)}")
            }
        }

        Spacer(Modifier.height(20.dp))

        // ----------- HISTORIAL DE COMPRAS ----------
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {

            Column(Modifier.padding(20.dp)) {

                Text("Historial de Compras", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(16.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛍  Aún no has realizado compras", color = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ----------- INFORMACIÓN DE LA CUENTA ----------
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {

            Column(Modifier.padding(20.dp)) {

                Text("Información de la Cuenta", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(12.dp))

                Text("Tipo de cuenta")
                Text("Correo Electrónico", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(16.dp))

                Text("ID de Usuario")
                Text(user?.uid ?: "N/A", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ----------- BOTÓN CERRAR SESIÓN ----------
        Button(
            onClick = { onLogout() },   // 👈 AQUI ESTA LA CORRECCIÓN
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Cerrar Sesión")
        }

        Spacer(Modifier.height(20.dp))

        // ----------- SISTEMA SMARTCART ----------
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Sistema SmartCart", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tu cuenta está sincronizada con el sistema del carrito inteligente. Escaneos y compras seguras.",
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}


// ---------------- COMPONENTES ----------------------

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, color = Color(0xFFDDE7FF))
    }
}

@Composable
fun SyncBox(title: String, value: String, bgColor: Color, modifier: Modifier) {
    Column(
        modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(title)
        Spacer(Modifier.height(6.dp))
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.Bold)
    }
}
