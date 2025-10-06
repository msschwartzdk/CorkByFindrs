package com.example.corkbyfindrs.ui.theme

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val app: Application
) : ViewModel() {

    private val _permissionState = MutableStateFlow(false)
    val permissionState: StateFlow<Boolean> = _permissionState

    private val _deniedPermissions = MutableStateFlow<List<String>>(emptyList())
    val deniedPermissions: StateFlow<List<String>> = _deniedPermissions

    fun checkPermissions() {
        val foregroundPermissions = listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
        )

        /*
        val required = getRequiredPermissions()
        val denied = required.filter {
            ContextCompat.checkSelfPermission(app, it) != PackageManager.PERMISSION_GRANTED
        }
        _permissionState.value = denied.isEmpty()
        _deniedPermissions.value = denied.filter {
            !ActivityCompat.shouldShowRequestPermissionRationale(app as Activity, it)
        }*/

    }
}
