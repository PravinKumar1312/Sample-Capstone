package com.gladden.skillsyncai

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    // SharedPreferences for persistent user data storage
    private val sharedPrefs = application.getSharedPreferences("user_data", Context.MODE_PRIVATE)

    private val _isLoginMode = MutableLiveData(true)
    val isLoginMode: LiveData<Boolean> = _isLoginMode

    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    // Get stored email and password from SharedPreferences
    private val registeredEmail: String?
        get() = sharedPrefs.getString("user_email", null)

    private val registeredPassword: String?
        get() = sharedPrefs.getString("user_password", null)

    init {
        // Pre-fill the email field if a user is already registered
        _email.value = registeredEmail ?: ""
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _errorMessage.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _errorMessage.value = null
    }

    fun toggleMode() {
        _isLoginMode.value = !_isLoginMode.value!!
        _email.value = if (_isLoginMode.value == true) registeredEmail ?: "" else ""
        _password.value = ""
        _errorMessage.value = null
    }

    fun registerUser() {
        if (email.value.isNullOrBlank() || password.value.isNullOrBlank()) {
            _errorMessage.value = "Please fill all fields"
            return
        }
        if (registeredEmail != null) {
            _errorMessage.value = "User already exists. Please log in."
            return
        }

        // Save new user details to SharedPreferences
        sharedPrefs.edit().apply {
            putString("user_email", email.value)
            putString("user_password", password.value)
            apply()
        }

        _errorMessage.value = "Registration successful! Please log in."
        _isLoginMode.value = true
    }

    fun loginUser(onLoginSuccess: () -> Unit) {
        if (email.value.isNullOrBlank() || password.value.isNullOrBlank()) {
            _errorMessage.value = "Please fill all fields"
            return
        }
        if (email.value == registeredEmail && password.value == registeredPassword) {
            onLoginSuccess()
        } else {
            _errorMessage.value = "Invalid email or password"
        }
    }

    // Making the factory public so other files can access it
    class AuthViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}