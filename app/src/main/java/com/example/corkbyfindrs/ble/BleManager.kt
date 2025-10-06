package com.example.corkbyfindrs.ble

import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.corkbyfindrs.app.BleRepository
import com.example.corkbyfindrs.ble.BleConstants.PUBLIC_CHARACTERISTIC_UUID
import com.example.corkbyfindrs.ble.BleConstants.PUBLIC_SERVICE_UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.altbeacon.beacon.Beacon
import java.util.UUID

sealed class BleCommand {
    class Connect(val address: String, val isKnown: Boolean) : BleCommand()
    class DiscoverServices(val gatt: BluetoothGatt) : BleCommand()
    class ReadCharacteristic(val gatt: BluetoothGatt, val characteristic: BluetoothGattCharacteristic) : BleCommand()
    class WriteCharacteristic(val gatt: BluetoothGatt, val characteristic: BluetoothGattCharacteristic, val data: ByteArray) : BleCommand()
    class SendPublicDataToServer(val gatt: BluetoothGatt) : BleCommand()
    class Disconnect(val gatt: BluetoothGatt) : BleCommand()
}

class BleManager(
    private val context: Context,
    private val bleRepository: BleRepository,
    private val onStateChange: (String) -> Unit = {},
    private val onLog: (String) -> Unit = {}
) {
    private val connectedDevices = MutableStateFlow<Set<String>>(emptySet()) //Used for Compose UI
    private val bleDevices = mutableMapOf<String, BleDeviceInfo>()  //Local

    // BLE command queue with timeout
    private val commandQueue = ArrayDeque<BleCommand>()
    private var isProcessing = false
    private val commandTimeoutMillis = 10_000L
    private var timeoutJob: Job? = null

    fun start() {
        onStateChange("SCANNING")
        onLog("BLE scanning started")
    }

    fun stop() {
        onStateChange("STOPPED")
        onLog("BLE scanning stopped")
    }

    fun connectNormal(beacon: Beacon){
        onStateChange("CONNECTING")
        onLog("Normal mode device found")
        //TODO
        Log.i(TAG,"Connecting to normal mode device ${beacon.bluetoothAddress} is disabled!")
        //enqueueCommand(BleCommand.Connect(beacon.bluetoothAddress, true))
    }

    fun connectAlert(beacon: Beacon){
        onStateChange("CONNECTING")
        onLog("Alert mode device found")
        enqueueCommand(BleCommand.Connect(beacon.bluetoothAddress, false))
    }

    // *********************** Private functions *************************************
    private fun updateConnectedList(update: (Set<String>) -> Set<String>) {
        val newSet = update(connectedDevices.value)
        connectedDevices.value = newSet
        bleRepository.updateConnectedDevices(newSet)
        Log.i(TAG,"Connected list updated! Devices: ${connectedDevices.value.size}")
    }

    private fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No need to check before Android 12
        }
    }

    fun enqueueCommand(command: BleCommand) {
        if (command is BleCommand.Connect) {
            if (bleDevices.containsKey(command.address)) {
                onLog("⚠\uFE0F Device ${command.address} already known. Request aborted!")
                return
            }
            else{
                bleDevices[command.address] = BleDeviceInfo(
                    macAddress = command.address,
                    isKnown = command.isKnown
                )
                onLog("Device ${command.address} registered!")
            }
        }
        commandQueue.add(command)
        //Log.i(TAG,"New command in queue. Length: ${commandQueue.size}")
        processNext()
    }

    private fun processNext() {
        if (isProcessing) return

        if (commandQueue.isEmpty()){
            Log.i(TAG,"Command queue is empty!")
            return
        }

        val command = commandQueue.removeFirst()
        isProcessing = true
        startTimeoutFor(command)

        when (command) {
            is BleCommand.Connect -> {
                if (!hasConnectPermission()) {
                    onLog("❌ Connection aborted. Missing BLUETOOTH_CONNECT permission")
                    Log.w(TAG, "Missing BLUETOOTH_CONNECT permission")
                    //TODO Inform user that permission is missing! Stop scanning?
                    isProcessing = false
                    processNext()
                    return
                }

                try {
                    onLog("🔗 Connecting to ${command.address}")
                    val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
                    if (bluetoothAdapter == null) {
                        onLog("❌ Bluetooth adapter not available")
                        isProcessing = false
                        processNext()
                        return
                    }

                    val device = bluetoothAdapter.getRemoteDevice(command.address)
                    val bluetoothGatt = device.connectGatt(context, false, corkGattCallback)
                    if (bluetoothGatt == null) {
                        onLog("❌ connectGatt() returned null for ${command.address}")
                        isProcessing = false
                        processNext()
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "🔒 SecurityException: Permission not granted for connectGatt()", e)
                    onLog("❌ SecurityException during connectGatt()")
                    isProcessing = false
                    processNext()
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "⚠️ Invalid MAC address: ${command.address}", e)
                    onLog("❌ Invalid MAC address: ${command.address}")
                    isProcessing = false
                    processNext()
                }
            }

            is BleCommand.DiscoverServices -> {
                val address = command.gatt.device.address
                onLog("Discovering services on $address")

                try {
                    val result = command.gatt.discoverServices()
                    if (!result) {
                        onLog("❌ Failed to start service discovery on $address (API returned false)")
                        isProcessing = false
                        processNext()
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "🔒 SecurityException: Service discovery failed for $address", e)
                    onLog("❌ SecurityException during service discovery on $address")
                    isProcessing = false
                    processNext()
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "⚠️ IllegalArgumentException: Invalid GATT object for $address", e)
                    onLog("❌ Invalid GATT object for $address")
                    isProcessing = false
                    processNext()
                }
            }

            is BleCommand.ReadCharacteristic -> {
                val address = command.gatt.device.address
                onLog("📥 Reading characteristic from $address: ${command.characteristic.uuid}")

                try {
                    val success = command.gatt.readCharacteristic(command.characteristic)
                    if (!success) {
                        onLog("❌ readCharacteristic() returned false for $address")
                        isProcessing = false
                        processNext()
                    }
                } catch (e: SecurityException) {
                    onLog("❌ SecurityException reading from $address")
                    Log.e(TAG, "Permission issue reading characteristic", e)
                    isProcessing = false
                    processNext()
                } catch (e: IllegalArgumentException) {
                    onLog("❌ Invalid characteristic or GATT for $address")
                    Log.e(TAG, "Invalid read request", e)
                    isProcessing = false
                    processNext()
                }
            }

            is BleCommand.WriteCharacteristic ->{
                val address = command.gatt.device.address
                onLog("📥 Write public data to $address")

                try {
                    command.characteristic.value = command.data
                    command.characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

                    val success = command.gatt.writeCharacteristic(command.characteristic)

                    if (!success) {
                        onLog("❌ writeCharacteristic() returned false for $address")
                        isProcessing = false
                        processNext()
                    }

                } catch (e: SecurityException) {
                    onLog("❌ SecurityException writing to $address")
                    Log.e(TAG, "Permission issue writing characteristic", e)
                    isProcessing = false
                    processNext()
                } catch (e: IllegalArgumentException) {
                    onLog("❌ Invalid characteristic or GATT for $address")
                    Log.e(TAG, "Invalid write request", e)
                    isProcessing = false
                    processNext()
                }
            }

            is BleCommand.SendPublicDataToServer -> {
                onLog("📥 Sending public data from ${command.gatt.device.address} to server")
                //TODO Server interface not implemented
                dummyServerCallback(command.gatt,"PublicReply")
            }

            is BleCommand.Disconnect ->{
                val address = command.gatt.device.address
                onLog("📥 Disconnecting from $address")

                if (!hasConnectPermission()) {
                    Log.w(TAG, "Missing BLUETOOTH_CONNECT permission")
                    isProcessing = false
                    processNext()
                    return
                }

                try {
                    command.gatt.disconnect() // first disconnect
                    onLog("Sent disconnect request to $address")
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException: Failed to disconnect BLE device", e)
                    onLog("❌ Failed to disconnect $address (permission)")
                    isProcessing = false
                    processNext()
                }
            }
        }
    }

    private fun startTimeoutFor(command: BleCommand) {
        timeoutJob?.cancel()

        timeoutJob = CoroutineScope(Dispatchers.IO).launch {
            delay(commandTimeoutMillis)
            onLog("⚠️ Timeout for command: $command")
            isProcessing = false
            processNext()
        }
    }

    fun dummyServerCallback(gatt: BluetoothGatt, reply: String){
        timeoutJob?.cancel()

        if(reply == "PublicReply"){
            onLog("✅ Dummy server reply: OK!")

            val service = gatt.getService(PUBLIC_SERVICE_UUID)
            if (service == null) {
                onLog("⚠️ Target service not found on ${gatt.device.address}")
            } else {
                val characteristic = service.getCharacteristic(PUBLIC_CHARACTERISTIC_UUID)
                if (characteristic == null) {
                    onLog("⚠️ Target characteristic not found in service on ${gatt.device.address}")
                } else if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
                    onLog("❌ Characteristic on ${gatt.device.address} is not writeable")
                } else {
                    onLog("📥 Enqueuing public write ${gatt.device.address}")
                    val data = byteArrayOf(0x02, 0x00, 0x22, 0x08)
                    enqueueCommand(BleCommand.WriteCharacteristic(gatt, characteristic, data))
                }
            }
        }

        isProcessing = false
        processNext()

    }
    // ************************ GATT CALLBACK ****************************************************
    private val corkGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            timeoutJob?.cancel()
            val address = gatt.device.address

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (!hasConnectPermission()) {
                    Log.w(TAG, "Missing BLUETOOTH_CONNECT permission")
                    return
                }

                try {
                    onStateChange("CONNECTED")
                    val device = bleDevices[address]
                    if (device != null) {
                        device.gatt = gatt
                        device.isConnected = true
                        onLog("✅ ${if (device.isKnown) "Own" else "Foreign"} device connected - $address")
                        Log.i(TAG,"Connected to ${if (device.isKnown) "Own" else "Foreign"} device: ${address}")
                    } else {
                        onLog("❌ To connect, $address unknown in device list!")
                    }
                    updateConnectedList { it + address }
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException: Failed to get BLE device name and address", e)
                }
                enqueueCommand(BleCommand.DiscoverServices(gatt))
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                try{
                    gatt.close()
                    onLog("Disconnected from $address")
                }catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException: Failed to close gatt connection", e)
                    onLog("❌ Failed to close gatt")
                }
                onStateChange("DISCONNECTED")

                updateConnectedList { it - address }
                bleDevices.remove(address)
            }

            isProcessing = false
            processNext()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            timeoutJob?.cancel()

            val address = gatt.device.address
            val isForeign = bleDevices[address]?.isKnown ?: true

            if (status == BluetoothGatt.GATT_SUCCESS) {
                onLog("✅ Services discovered on $address (${if (isForeign) "Foreign" else "Known"})")

                val service = gatt.getService(PUBLIC_SERVICE_UUID)
                if (service == null) {
                    onLog("⚠️ Target service not found on $address")
                } else {
                    val characteristic = service.getCharacteristic(PUBLIC_CHARACTERISTIC_UUID)
                    if (characteristic == null) {
                        onLog("⚠️ Target characteristic not found in service on $address")
                    } else if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ) == 0) {
                        onLog("❌ Characteristic on $address is not readable")
                    } else {
                        onLog("📥 Enqueuing read for characteristic on $address")
                        enqueueCommand(BleCommand.ReadCharacteristic(gatt, characteristic))
                    }
                }
            } else {
                onLog("❌ Service discovery failed on $address — status: $status")
            }

            isProcessing = false
            processNext()
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            timeoutJob?.cancel()

            val address = gatt.device.address
            val valueHex = characteristic.value?.joinToString(" ") { "%02X".format(it) } ?: "null"

            if (status == BluetoothGatt.GATT_SUCCESS) {
                onLog("✅ Read data from $address: ${characteristic.uuid} = $valueHex")
                enqueueCommand(BleCommand.SendPublicDataToServer(gatt))
            } else {
                onLog("❌ Failed to read from $address: ${characteristic.uuid}, status=$status")
                Log.w(TAG, "Characteristic read failed on $address, status=$status")
            }

            isProcessing = false
            processNext()
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            timeoutJob?.cancel()

            val address = gatt.device.address
            val value = characteristic.value?.joinToString(" ") { "%02X".format(it) } ?: "null"

            if (status == BluetoothGatt.GATT_SUCCESS) {
                onLog("✅ Write successful to $address: ${characteristic.uuid} = $value")
                //TODO enqueueCommand(BleCommand.Disconnect(gatt))
            } else {
                onLog("❌ Write failed to $address: ${characteristic.uuid}, status=$status")
            }

            isProcessing = false
            processNext()
        }
    }

    companion object {
        private const val TAG = "BleManager"
    }
}
