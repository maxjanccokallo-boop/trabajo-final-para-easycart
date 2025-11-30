package com.example.easycart.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.easycart.di.AppModule

class BluetoothViewModel : ViewModel() {

    fun connectToDevice(address: String, onFinish: (String?) -> Unit) {
        Log.d("BT_FLOW", "UI solicita conexión con $address")
        AppModule.bluetoothService.connect(address) { err ->
            Log.e("BT_FLOW", "Error conectando: $err")
            onFinish(err)
        }
    }

    fun disconnect() {
        Log.d("BT_FLOW", "UI solicita desconexión.")
        AppModule.bluetoothService.disconnect()
    }

    fun refreshState() {
        Log.d("BT_FLOW", "Estado refrescado")
        // En el futuro, aquí podrías forzar la actualización del estado de la UI
        // si el StateFlow del servicio no se recompone automáticamente.
    }
}
