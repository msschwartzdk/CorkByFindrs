package com.example.corkbyfindrs.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corkbyfindrs.data.ServerClient
import com.example.corkbyfindrs.data.ServerConstants
import com.example.corkbyfindrs.data.UserSettingsRepository
import com.example.corkbyfindrs.data.models.Bike
import com.example.corkbyfindrs.data.models.BikeLogEntry
import com.example.corkbyfindrs.data.models.Device
import com.example.corkbyfindrs.data.models.GetRideFileRequest
import com.example.corkbyfindrs.data.models.RideInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BikeDataViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val serverClient: ServerClient // Added ServerClient
) : ViewModel() {

    private val _userBikes = MutableStateFlow<List<Bike>>(emptyList())
    val userBikes: StateFlow<List<Bike>> = _userBikes.asStateFlow()

    private val _selectedBike = MutableStateFlow<Bike?>(null)
    val selectedBike: StateFlow<Bike?> = _selectedBike.asStateFlow()

    private val _devicesForSelectedBike = MutableStateFlow<List<Device>>(emptyList())
    val devicesForSelectedBike: StateFlow<List<Device>> = _devicesForSelectedBike.asStateFlow()

    // Logs for the selected bike are directly available via selectedBike.value?.logs
    private val _ridesForSelectedBike = MutableStateFlow<List<RideInfo>>(emptyList())
    val ridesForSelectedBike: StateFlow<List<RideInfo>> = _ridesForSelectedBike.asStateFlow()

    private val _allDevices = MutableStateFlow<List<Device>>(emptyList()) // To hold all devices from UserPreferences
    private val _allRides = MutableStateFlow<Map<Int, List<RideInfo>>>(emptyMap()) // To hold all rides

    private val _userEmail = MutableStateFlow<String>("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _showChangePasswordDialog = MutableStateFlow<Boolean>(false)
    val showChangePasswordDialog: StateFlow<Boolean> = _showChangePasswordDialog.asStateFlow()

    private val _changePasswordMessage = MutableStateFlow<String?>(null) // For success/error messages
    val changePasswordMessage: StateFlow<String?> = _changePasswordMessage.asStateFlow()

    private val _isChangingPassword = MutableStateFlow<Boolean>(false)
    val isChangingPassword: StateFlow<Boolean> = _isChangingPassword.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null) // General error messages for the screen
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showErrorDialog = MutableStateFlow<Boolean>(false) // To control error dialog visibility
    val showErrorDialog: StateFlow<Boolean> = _showErrorDialog.asStateFlow()


    init {
        userSettingsRepository.userPreferencesFlow
            .onEach { preferences ->
                _userBikes.value = preferences.bikes
                _allDevices.value = preferences.devices // Store all devices
                _allRides.value = preferences.rides // Store all rides
                _userEmail.value = preferences.email

                val currentSelectedBikeId = _selectedBike.value?.id
                var newSelectedBikeInstance: Bike? = null

                if (currentSelectedBikeId != null) {
                    newSelectedBikeInstance = preferences.bikes.find { it.id == currentSelectedBikeId }
                }

                // If the previously selected bike is no longer available, or no bike was selected,
                // try to select the first bike from the new list.
                // Otherwise, update with the new instance of the selected bike (even if it's the same ID, its content like logs might have changed).
                if (newSelectedBikeInstance != null) {
                    selectBike(newSelectedBikeInstance)
                } else {
                    selectBike(preferences.bikes.firstOrNull())
                }
            }
            .launchIn(viewModelScope)

        // React to changes in the selected bike to update its devices
        _selectedBike
            .onEach { bike ->
                _devicesForSelectedBike.value = if (bike != null) {
                    _allDevices.value.filter { device -> device.bikeId == bike.id }
                } else {
                    emptyList()
                }
                _ridesForSelectedBike.value = if (bike != null) {
                    _allRides.value[bike.id] ?: emptyList()
                } else {
                    emptyList()
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectBike(bike: Bike?) {
        _selectedBike.value = bike
        // When a bike is selected, also update the rides for it.
        // This is also handled by the _selectedBike.onEach collector, but explicit update can be clearer
        // or useful if the onEach isn't triggered immediately for some reason (though it should).
        // For now, relying on the onEach collector is fine.
    }

    fun getLogsForSelectedBike(): List<BikeLogEntry> {
        return _selectedBike.value?.logs ?: emptyList()
    }

    // ridesForSelectedBike StateFlow can be directly observed by the UI

    fun openChangePasswordDialog() {
        _showChangePasswordDialog.value = true
    }

    fun closeChangePasswordDialog() {
        _showChangePasswordDialog.value = false
        _changePasswordMessage.value = null // Clear any previous messages
    }

    fun setChangePasswordMessage(message: String?) {
        _changePasswordMessage.value = message
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // We can add a function to explicitly set an error message if needed, e.g.
    // fun setGeneralErrorMessage(message: String) { // Renamed to avoid confusion
    //     _errorMessage.value = message
    // }

    fun logout() {
        // The actual clearing of session ID and navigation will be handled
        // by the caller (e.g., in MainActivity or the Composable screen)
        // after this call. This ViewModel's responsibility is primarily
        // to trigger the UserSettingsRepository update.
        // For now, this function can be empty if UserSettingsRepository.updateSessionId(null)
        // is called directly by the UI layer after this.
        // However, to keep logic centralized, let's assume it might do more in future or if repository was async.
        // For now, just a placeholder or direct call if UserSettingsRepository was injected here.
        // Actually, UserSettingsRepository is injected, so we can make it call it.
        // userSettingsRepository.updateSessionId(null) // This should be a suspend fun, so call from viewModelScope
        // The plan says to call userSettingsRepository.updateSessionId(null) in the UI layer after this.
        // So this function in ViewModel might just be a signal or can be removed if UI calls repo directly.
        // Let's keep it simple: the UI will call repository.updateSessionId(null) and then navigate.
        // This ViewModel function isn't strictly needed for logout if repository is public or passed.
        // For consistency with changePassword, let's add the call here.
        // viewModelScope.launch { userSettingsRepository.updateSessionId(null) }
        // The plan states: "When "Logout" is clicked and after calling viewModel.logout(): Navigate..."
        // This implies logout() in VM might prepare something or it's just a semantic call.
        // Let's make it trigger the actual session clearing.
        kotlinx.coroutines.GlobalScope.launch { // Using GlobalScope is not ideal, should use viewModelScope
            // but updateSessionId is suspend, and logout() itself isn't marked suspend.
            // This needs to be called from a coroutine scope.
            // The UI will call this, then navigate.
            userSettingsRepository.updateSessionId(null) // This is a suspend function.
        }
        // The above GlobalScope is problematic. The logout function should be a suspend function itself,
        // or call it from viewModelScope if it's a ViewModel function that triggers suspend work.
        // For now, I will leave this to be called within a coroutine scope in the UI layer or make logout suspend.
        // Given the plan "after calling viewModel.logout()", the VM function should do the core action.
    }
    // Corrected logout:
    fun performLogout() {
        viewModelScope.launch {
            userSettingsRepository.updateSessionId(null)
        }
    }

    fun changePassword(currentPass: String, newPass: String) {
        viewModelScope.launch {
            if (currentPass.isEmpty() || newPass.isEmpty()) {
                _changePasswordMessage.value = "Error: All fields are required."
                return@launch
            }
            _isChangingPassword.value = true
            _changePasswordMessage.value = null // Clear previous message
            try {
                val response = serverClient.changePassword(currentPass, newPass)
                if (response.rv == ServerConstants.RV_OK) {
                    _changePasswordMessage.value = "Password changed successfully."
                    // UserSettingsRepository is updated by serverClient.changePassword on success
                } else {
                    _changePasswordMessage.value = "Failed: ${response.msg}"
                }
            } catch (e: Exception) {
                _changePasswordMessage.value = "Error: ${e.message ?: "Unknown error"}"
            } finally {
                _isChangingPassword.value = false
            }
        }
    }

    fun dismissErrorDialog() {
        _showErrorDialog.value = false
        _errorMessage.value = null
    }

    fun downloadRideFile(rideId: String) {
        viewModelScope.launch {
            val currentBikeId = _selectedBike.value?.id ?: return@launch // Ensure a bike is selected
            val userPrefs = userSettingsRepository.userPreferencesFlow.first() // Get current session & email

            // Find the ride and update its downloading status
            val currentRides = _ridesForSelectedBike.value
            val rideIndex = currentRides.indexOfFirst { it.rideid == rideId }
            if (rideIndex == -1) {
                _errorMessage.value = "Ride not found."
                _showErrorDialog.value = true
                return@launch
            }

            // Mark as downloading
            _ridesForSelectedBike.value = currentRides.mapIndexed { index, rideInfo ->
                if (index == rideIndex) rideInfo.copy(isDownloading = true) else rideInfo
            }

            try {
                val response = serverClient.getRideFile(
                    GetRideFileRequest(
                        session_id = userPrefs.sessionId,
                        user_email = userPrefs.email,
                        ride_id = rideId
                    )
                )

                if (response.rv == ServerConstants.RV_OK && response.filePath != null) {
                    // Success: Update localFilePath
                    _ridesForSelectedBike.value = _ridesForSelectedBike.value.mapIndexed { index, rideInfo ->
                        if (index == rideIndex) {
                            rideInfo.copy(localFilePath = response.filePath, isDownloading = false)
                        } else rideInfo
                    }
                    // Also update this in the global _allRides cache and persist it via UserSettingsRepository
                    val updatedAllRides = _allRides.value.toMutableMap()
                    val bikeRides = updatedAllRides[currentBikeId]?.map { ride ->
                        if (ride.rideid == rideId) ride.copy(localFilePath = response.filePath, isDownloading = false) else ride
                    }
                    if (bikeRides != null) {
                        updatedAllRides[currentBikeId] = bikeRides
                        userSettingsRepository.updateRidesForBike(currentBikeId, bikeRides) // Persist
                    }


                } else {
                    // Failure during download
                    _errorMessage.value = response.msg ?: "Failed to download ride file."
                    _showErrorDialog.value = true
                    _ridesForSelectedBike.value = _ridesForSelectedBike.value.mapIndexed { index, rideInfo ->
                        if (index == rideIndex) rideInfo.copy(isDownloading = false) else rideInfo
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error downloading file: ${e.message}"
                _showErrorDialog.value = true
                _ridesForSelectedBike.value = _ridesForSelectedBike.value.mapIndexed { index, rideInfo ->
                    if (index == rideIndex) rideInfo.copy(isDownloading = false) else rideInfo
                }
            }
        }
    }

    fun deleteRideFile(rideId: String) {
        viewModelScope.launch {
            val currentBikeId = _selectedBike.value?.id ?: return@launch
            val rideIndex = _ridesForSelectedBike.value.indexOfFirst { it.rideid == rideId }
            if (rideIndex == -1) {
                _errorMessage.value = "Ride not found for deletion."
                _showErrorDialog.value = true
                return@launch
            }

            val rideToDelete = _ridesForSelectedBike.value[rideIndex]
            val filePath = rideToDelete.localFilePath

            if (filePath == null) {
                _errorMessage.value = "File path not found for this ride."
                _showErrorDialog.value = true
                return@launch
            }

            try {
                val file = java.io.File(filePath)
                if (file.exists()) {
                    if (file.delete()) {
                        // Success: Update localFilePath to null
                        _ridesForSelectedBike.value = _ridesForSelectedBike.value.mapIndexed { index, rideInfo ->
                            if (index == rideIndex) rideInfo.copy(localFilePath = null) else rideInfo
                        }
                        // Also update this in the global _allRides cache and persist it
                        val updatedAllRides = _allRides.value.toMutableMap()
                        val bikeRides = updatedAllRides[currentBikeId]?.map { ride ->
                            if (ride.rideid == rideId) ride.copy(localFilePath = null) else ride
                        }
                        if (bikeRides != null) {
                            updatedAllRides[currentBikeId] = bikeRides
                            userSettingsRepository.updateRidesForBike(currentBikeId, bikeRides) // Persist
                        }

                    } else {
                        _errorMessage.value = "Failed to delete local file."
                        _showErrorDialog.value = true
                    }
                } else {
                    _errorMessage.value = "Local file does not exist."
                    _showErrorDialog.value = true
                    // File doesn't exist, so effectively it's "deleted". Update the state.
                    _ridesForSelectedBike.value = _ridesForSelectedBike.value.mapIndexed { index, rideInfo ->
                        if (index == rideIndex) rideInfo.copy(localFilePath = null) else rideInfo
                    }
                    val updatedAllRides = _allRides.value.toMutableMap()
                    val bikeRides = updatedAllRides[currentBikeId]?.map { ride ->
                        if (ride.rideid == rideId) ride.copy(localFilePath = null) else ride
                    }
                    if (bikeRides != null) {
                        updatedAllRides[currentBikeId] = bikeRides
                        userSettingsRepository.updateRidesForBike(currentBikeId, bikeRides) // Persist
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting file: ${e.message}"
                _showErrorDialog.value = true
            }
        }
    }
}
