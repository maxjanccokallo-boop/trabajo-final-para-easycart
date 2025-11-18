package com.example.easycart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.easycart.data.EasyCartRepository
import com.example.easycart.ui.navigation.RootNavGraph
import com.example.easycart.ui.theme.EasyCartTheme
import com.example.easycart.viewmodel.AuthViewModel
import com.example.easycart.viewmodel.MainViewModel

// IMPORTS CORRECTOS DE FIREBASE
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializar Firebase y Repositorio
        // ⭐ AJUSTE: Pasamos Auth y luego Firestore, coincidiendo con el Repositorio.
        val repo = EasyCartRepository(
            auth = Firebase.auth,
            db = Firebase.firestore
        )

        // 2. ViewModels
        val authVm = AuthViewModel(repo)
        val mainVm = MainViewModel(repo)

        setContent {
            EasyCartTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()

                    RootNavGraph(
                        navController = navController,
                        authViewModel = authVm,
                        mainViewModel = mainVm
                    )
                }
            }
        }
    }
}