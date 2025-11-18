package com.example.easycart.bluetooth

import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import java.util.*

class BluetoothService(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var bluetoothSocket: BluetoothSocket? = null

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private val _receivedData = MutableStateFlow("")
    val receivedData: StateFlow<String> = _receivedData

    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // RFCOMM

    fun connect(address: String, onError: (String) -> Unit) {
        scope.launch {
            try {
                _connected.emit(false)

                val adapter = BluetoothAdapter. getDefaultAdapter()
                val device = adapter.getRemoteDevice(address)

                bluetoothSocket =
                    device.createRfcommSocketToServiceRecord(uuid)

                adapter.cancelDiscovery()
                bluetoothSocket!!.connect()

                _connected.emit(true)   

                listenForData()

            } catch (e: Exception) {
                onError(e.message ?: "Error desconocido")
                _connected.emit(false)
            }
        }
    }

    fun sendData(data: String) {
        scope.launch {
            try {
                bluetoothSocket?.outputStream?.write(data.toByteArray())
            } catch (e: IOException) {
                Log.e("BluetoothService", "Error enviando datos: ${e.message}")
            }
        }
    }

    private fun listenForData() {
        scope.launch {
            try {
                val buffer = ByteArray(1024)
                val input = bluetoothSocket!!.inputStream

                while (true) {
                    val bytes = input.read(buffer)
                    val message = String(buffer, 0, bytes)
                    _receivedData.emit(message)
                }

            } catch (e: Exception) {
                _connected.emit(false)
            }
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                bluetoothSocket?.close()
                _connected.emit(false)
            } catch (_: Exception) {}
        }
    }
}
