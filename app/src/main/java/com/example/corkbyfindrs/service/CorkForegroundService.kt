package com.example.corkbyfindrs.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.altbeacon.beacon.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.corkbyfindrs.R
import com.example.corkbyfindrs.app.BleRepository
import com.example.corkbyfindrs.ble.BleManager
import com.example.corkbyfindrs.ble.BleViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CorkForegroundService : Service() {
    //BleRepository is used to share updates between service and BleViewModel
    @Inject lateinit var bleRepository: BleRepository

    private val TAG = "CorkForegroundService"
    private lateinit var beaconManager: BeaconManager
    private lateinit var bleManager: BleManager
    private lateinit var normalRegion: BeaconRegion
    private lateinit var alertRegion: BeaconRegion

    val corkNormalUuid = "D972C0CC-1C28-11E5-9A21-1697F925EC7B" //Cork Normal UUID
    val corkAlertUuid =  "AE0E94DD-1594-4873-977E-4D1BE0E4B6BC"

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")

        beaconManager = BeaconManager.getInstanceForApplication(this).apply {
            beaconParsers.clear()
            beaconParsers.add(
                BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
            )
            foregroundScanPeriod = 2000L
            foregroundBetweenScanPeriod = 0L
        }

        normalRegion = BeaconRegion(
            "CorkNormal",
            beaconManager.beaconParsers[0],
            corkNormalUuid.uppercase(),
            null,
            null
        )
        alertRegion = BeaconRegion(
            "CorkAlert",
            beaconManager.beaconParsers[0],
            corkAlertUuid.uppercase(),
            null,
            null
        )

        bleManager = BleManager(
            context = applicationContext,
            bleRepository = bleRepository,
            onStateChange = { state ->
                bleRepository.setConnectionState(state)
                bleRepository.log("BLE state changed to $state")
            },
            onLog = { msg ->
                bleRepository.log(msg)
            }
        )

        //bleManager = BleManager(applicationContext, bleViewModel)

        beaconManager.getRegionViewModel(normalRegion).rangedBeacons.observeForever(rangingObserver)
        beaconManager.getRegionViewModel(alertRegion).rangedBeacons.observeForever(rangingObserver)

        beaconManager.startRangingBeacons(normalRegion)
        beaconManager.startRangingBeacons(alertRegion)

        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bleManager.start()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "Service destroyed")
        // Stop ranging and remove observers
        beaconManager.stopRangingBeacons(normalRegion)
        beaconManager.stopRangingBeacons(alertRegion)

        beaconManager.getRegionViewModel(normalRegion).rangedBeacons.removeObserver(rangingObserver)
        beaconManager.getRegionViewModel(alertRegion).rangedBeacons.removeObserver(rangingObserver)

        beaconManager.removeAllRangeNotifiers()

        bleManager.stop()
        super.onDestroy()
    }

    private val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d(TAG, "Found ${beacons.size} beacons")
        for (beacon in beacons) {
            val uuid = beacon.id1.toString().uppercase()
            when (uuid) {
                corkNormalUuid.uppercase() -> {
                    Log.d(TAG,"NORMAL - Name: ${beacon.bluetoothName}, Address: ${beacon.bluetoothAddress}")
                    bleManager.connectNormal(beacon)
                }
                corkAlertUuid.uppercase() -> {
                    Log.d(TAG, "ALERT - Name: ${beacon.bluetoothName}, Address: ${beacon.bluetoothAddress}")
                    bleManager.connectAlert(beacon)
                }
                else -> Log.d(TAG, "UNKNOWN - $uuid")
            }
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "cork_bike_channel")
            .setContentTitle("Cork Bike Scanning")
            .setContentText("Running in background...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 42
    }
}
