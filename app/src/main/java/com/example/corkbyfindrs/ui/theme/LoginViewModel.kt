package com.example.corkbyfindrs.ui.theme

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.corkbyfindrs.utils.SecurePrefs
import com.example.corkbyfindrs.data.ServerClient
import com.example.corkbyfindrs.data.ServerConstants
import com.example.corkbyfindrs.data.UserSettingsRepository
import com.example.corkbyfindrs.data.models.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

//class LoginViewModel(application: Application) : AndroidViewModel(application) {
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val serverClient: ServerClient,
    private val application: Application
) : ViewModel() {

    companion object {
        private val TAG = "LoginViewModel"
    }

    var showLoginDialog by mutableStateOf(false)
        private set

    var permissionStatus = mutableStateOf("")
        private set

    var prefetchedEmail by mutableStateOf("")
        private set

    var prefetchedPassword by mutableStateOf("")
        private set

    var navigateToBikeListTrigger by mutableStateOf(0)
        private set

    private var autoLoginAttempted = false

    fun attemptAutoLoginOrShowManualLoginInternal() {
        if (autoLoginAttempted){
            Log.i(TAG,"Auto Login Ateempted: ${autoLoginAttempted}")
            return
        }
        autoLoginAttempted = true

        viewModelScope.launch {
            val prefs = userSettingsRepository.userPreferencesFlow.first()
            prefetchedEmail = prefs.email
            prefetchedPassword = prefs.password


            if (prefs.email.isNotBlank() && prefs.password.isNotBlank()) {
                Log.i(TAG,"Try auto Login for: ${prefetchedEmail}")
                permissionStatus.value = "Attempting auto-login..."
                try {
                    val loginResponse = serverClient.login(LoginRequest(prefs.email, prefs.password))
                    Log.i(TAG,"Auto Login server response: ${loginResponse.rv}, ${loginResponse.msg}")
                    if (loginResponse.rv == ServerConstants.RV_OK && loginResponse.sessionId != null) {
                        Log.i(TAG,"Auto Login Successful!")
                        permissionStatus.value = "Auto-login successful!"
                        fetchUserDataInBackground()
                        navigateToBikeListTrigger++ // triggers navigation
                    } else {
                        Log.i(TAG,"Auto Login failed!")
                        permissionStatus.value = "Auto-login failed."
                        showLoginDialog = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG,"Auto Login error: ${e.message}")
                    permissionStatus.value = "Auto-login error: ${e.message}"
                    showLoginDialog = true
                }
            } else {
                Log.i(TAG,"email blank - Auto Login failed!")
                permissionStatus.value = "No stored credentials."
                showLoginDialog = true
            }
        }
    }

    fun manualLogin(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                Log.i(TAG,"Try manual login...")
                permissionStatus.value = "Logging in..."
                val response = serverClient.login(LoginRequest(email, password))
                Log.i(TAG,"Manuel login server response: ${response.rv}, ${response.msg}")
                if (response.rv == ServerConstants.RV_OK && response.sessionId != null) {
                    Log.i(TAG,"Manual Login Successful!")
                    userSettingsRepository.updateEmail(email)
                    userSettingsRepository.updatePassword(password)
                    permissionStatus.value = "Login successful!"
                    showLoginDialog = false
                    onSuccess()
                } else {
                    Log.i(TAG,"Manual Login failed!")
                    permissionStatus.value = "Login failed: ${response.msg.joinToString()}"
                }
            } catch (e: Exception) {
                Log.e(TAG,"Manual Login error: ${e.message}")
                permissionStatus.value = "Error: ${e.message}"
            }
        }
    }


    private fun fetchUserDataInBackground() {
        viewModelScope.launch(Dispatchers.IO) {
            var fetchError = false

            try {
                val devices = serverClient.getUsersDevices()
                if (devices.rv != ServerConstants.RV_OK) {
                    Log.w("LoginVM", "Devices fetch failed: ${devices.msg.joinToString()}")
                    fetchError = true
                }
                else{
                    Log.i("LoginVM", "Devices fetched successfully!")
                }
            } catch (e: Exception) {
                Log.e("LoginVM", "Devices fetch error", e)
                fetchError = true
            }

            try {
                val bikes = serverClient.getUsersBikes()
                if (bikes.rv != ServerConstants.RV_OK) {
                    Log.w("LoginVM", "Bikes fetch failed: ${bikes.msg.joinToString()}")
                    fetchError = true
                }
                else{
                    Log.i("LoginVM", "Bikes fetched successfully!")
                }
            } catch (e: Exception) {
                Log.e("LoginVM", "Bikes fetch error", e)
                fetchError = true
            }

            withContext(Dispatchers.Main) {
                if (fetchError) {
                    permissionStatus.value = "Data fetch failed after login"
                    Toast.makeText(application, "Some data may be outdated.", Toast.LENGTH_LONG).show()
                } else {
                    permissionStatus.value = "User data updated."
                }
            }
        }
    }


}
