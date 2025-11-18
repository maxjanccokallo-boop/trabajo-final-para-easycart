package com.example.easycart.di

import com.example.easycart.data.EasyCartRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object AppModule {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    // ⭐ CORRECCIÓN: Aseguramos el orden correcto: auth, firestore
    val repo: EasyCartRepository by lazy {
        EasyCartRepository(
            auth = auth,
            db = firestore
        )
    }

    // Si tienes un AuthViewModelFactory, iría aquí, pero lo manejas en MainActivity.

    // Si tienes un MainViewModelFactory, debería estar aquí:
    val mainRepo = repo // Renombramos la referencia para usarla en el Factory
    val repoForFactory = repo

    val repoForAuthFactory = repo // Si tienes una factoría de AuthViewModel, usaría esta referencia.
}