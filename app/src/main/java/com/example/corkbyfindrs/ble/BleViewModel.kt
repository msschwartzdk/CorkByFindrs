package com.example.corkbyfindrs.ble

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.corkbyfindrs.app.BleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BleViewModel @Inject constructor(
    bleRepository: BleRepository
) : ViewModel() {
    val connectionState: StateFlow<String> = bleRepository.connectionState
    val logMessages: StateFlow<List<String>> = bleRepository.logMessages
    val connectedDevices: StateFlow<Set<String>> = bleRepository.connectedDevices
}


