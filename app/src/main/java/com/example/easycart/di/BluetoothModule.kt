package com.example.easycart.di

import com.example.easycart.bluetooth.BluetoothService

/**
 * Singleton object to hold a global instance of the BluetoothService.
 */
object BluetoothModule {
    lateinit var bluetoothService: BluetoothService
}
