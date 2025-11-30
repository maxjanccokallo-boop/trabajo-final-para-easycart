package com.example.easycart.di

import com.example.easycart.bluetooth.BluetoothService
import com.example.easycart.data.EasyCartRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object AppModule {

    val repo: EasyCartRepository by lazy {
        EasyCartRepository(
            auth = Firebase.auth,
            db = Firebase.firestore
        )
    }

    // Instancia global del servicio Bluetooth
    lateinit var bluetoothService: BluetoothService
}
