package com.example.corkbyfindrs.ble

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import org.altbeacon.beacon.*

class BeaconScannerManager(
    private val app: Application,
    private val lifecycleOwner: LifecycleOwner,
    private val corkNormalUuid: String,
    private val corkAlertUuid: String
) {

    private val TAG = "BeaconScanner"

    private val beaconManager: BeaconManager by lazy {
        BeaconManager.getInstanceForApplication(app).apply {
            beaconParsers.clear()
            beaconParsers.add(
                BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
            )
            foregroundBetweenScanPeriod = 2000L
            foregroundScanPeriod = 2000L
        }
    }

    fun start() {
        Log.i(TAG, "Starting beacon scanner")

        val normalRegion = BeaconRegion(
            "CorkNormal",
            beaconManager.beaconParsers[0],
            corkNormalUuid.uppercase(),
            null,
            null
        )

        val alertRegion = BeaconRegion(
            "CorkAlert",
            beaconManager.beaconParsers[0],
            corkAlertUuid.uppercase(),
            null,
            null
        )

        beaconManager.getRegionViewModel(normalRegion).rangedBeacons.observe(lifecycleOwner, rangingObserver)
        beaconManager.getRegionViewModel(alertRegion).rangedBeacons.observe(lifecycleOwner, rangingObserver)

        beaconManager.startRangingBeacons(normalRegion)
        beaconManager.startRangingBeacons(alertRegion)
    }

    private val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d(TAG, "Found ${beacons.size} beacons")
        for (beacon in beacons) {
            val uuid = beacon.id1.toString().uppercase()
            when (uuid) {
                corkNormalUuid.uppercase() -> Log.d(TAG, "NORMAL - Name: ${beacon.bluetoothName}, Address: ${beacon.bluetoothAddress}")
                corkAlertUuid.uppercase() -> Log.d(TAG, "ALERT - Name: ${beacon.bluetoothName}, Address: ${beacon.bluetoothAddress}")
                else -> Log.d(TAG, "UNKNOWN - $uuid")
            }
        }
    }
}
