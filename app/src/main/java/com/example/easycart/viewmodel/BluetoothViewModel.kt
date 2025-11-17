package com.example.easycart.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.easycart.bluetooth.BluetoothService
import kotlinx.coroutines.flow.StateFlow

class BluetoothViewModel(app: Application) : AndroidViewModel(app) {

    private val service = BluetoothService(app)

    val connected: StateFlow<Boolean> = service.connected
    val receivedData: StateFlow<String> = service.receivedData

    fun connect(address: String, onError: (String) -> Unit) {
        service.connect(address, onError)
    }

    fun disconnect() = service.disconnect()

    fun send(msg: String) = service.sendData(msg)
}
