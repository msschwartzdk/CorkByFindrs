package com.example.corkbyfindrs.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.corkbyfindrs.utils.SecurePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val securePrefs = SecurePrefs(application)

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    private val _password = MutableStateFlow<String?>(null)
    val password: StateFlow<String?> = _password

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        checkIfAlreadyLoggedIn()
    }

    fun login(user: String, pass: String) {
        securePrefs.saveCredentials(user, pass)
        _username.value = user
        _password.value = pass
        _isLoggedIn.value = true
    }

    fun checkIfAlreadyLoggedIn() {
        val user = securePrefs.getUsername()
        val pass = securePrefs.getPassword()
        _username.value = user
        _password.value = pass
        _isLoggedIn.value = user != null && pass != null
    }

    fun logout() {
        securePrefs.clear()
        _username.value = null
        _password.value = null
        _isLoggedIn.value = false
    }
}
