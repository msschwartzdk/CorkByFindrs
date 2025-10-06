package com.example.corkbyfindrs.ble

import android.bluetooth.BluetoothGatt

class BleDeviceInfo (
    val macAddress: String,
    var gatt: BluetoothGatt? = null,
    var isKnown: Boolean = false,
    var isConnected: Boolean = false
)

