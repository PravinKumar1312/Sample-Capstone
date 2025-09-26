package com.gladden.skillsyncai

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // SharedPreferences for local data storage (Image URI)
    private val sharedPrefs = application.getSharedPreferences("local_user_data", Context.MODE_PRIVATE)

    // We'll use this LiveData to track if the user is logged in
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _isLoginMode = MutableLiveData(true)
    val isLoginMode: LiveData<Boolean> = _isLoginMode

    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    // LiveData to hold the current user's email for display in UserDetailsScreen
    private val _currentUserName = MutableLiveData<String?>(null)
    val currentUserName: LiveData<String?> = _currentUserName

    // LiveData to hold the local profile image URI, loaded from SharedPreferences on startup
    private val _profileImageUri = MutableLiveData<String?>(
        sharedPrefs.getString("profile_image_uri", null)
    )
    val profileImageUri: LiveData<String?> = _profileImageUri

    // FIX: Use 'lazy' to initialize FirebaseAuth only when it is first accessed.
    private val auth: FirebaseAuth by lazy { Firebase.auth }

    init {
        // Check if a user is already signed in on app launch
        _isLoggedIn.value = (auth.currentUser != null)
        // If a user is logged in, set their email for display
        _currentUserName.value = auth.currentUser?.email
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
        // Clear fields when toggling between login and register
        _email.value = ""
        _password.value = ""
        _errorMessage.value = null
    }

    fun registerUser() {
        if (email.value.isNullOrBlank() || password.value.isNullOrBlank()) {
            _errorMessage.value = "Please fill all fields"
            return
        }

        auth.createUserWithEmailAndPassword(email.value!!, password.value!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _errorMessage.value = "Registration successful! Please log in."
                    _isLoginMode.value = true
                    _email.value = "" // Clear fields on successful registration
                    _password.value = ""
                } else {
                    _errorMessage.value = task.exception?.message ?: "Registration failed."
                }
            }
    }

    fun loginUser() {
        if (email.value.isNullOrBlank() || password.value.isNullOrBlank()) {
            _errorMessage.value = "Please fill all fields"
            return
        }

        auth.signInWithEmailAndPassword(email.value!!, password.value!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isLoggedIn.value = true
                    // Set current user email on successful login
                    _currentUserName.value = auth.currentUser?.email
                } else {
                    _errorMessage.value = task.exception?.message ?: "Login failed."
                }
            }
    }

    // CORRECTED: Function to copy the content URI to a local file and save the new URI
    fun saveProfileImageUri(uri: Uri) {
        // 1. Create a file in the app's private files directory
        val timeStamp = System.currentTimeMillis()
        val destinationFile = File(application.filesDir, "profile_image_$timeStamp.jpg")

        try {
            // 2. Open an InputStream for the temporary content URI
            application.contentResolver.openInputStream(uri)?.use { inputStream ->
                // 3. Open an OutputStream for the new local file
                FileOutputStream(destinationFile).use { outputStream ->
                    // 4. Copy the data (the actual "upload" to local storage)
                    inputStream.copyTo(outputStream)
                }
            }

            // 5. Get the permanent local file URI and save it to SharedPreferences
            val localFileUri = destinationFile.toURI().toString()
            sharedPrefs.edit().putString("profile_image_uri", localFileUri).apply()
            _profileImageUri.value = localFileUri
            _errorMessage.value = "Profile image updated."

        } catch (e: Exception) {
            // Handle errors like permission failure, IO errors, etc.
            _errorMessage.value = "Failed to save image: ${e.message}"
        }
    }

    fun sendPasswordResetEmail() {
        if (email.value.isNullOrBlank()) {
            _errorMessage.value = "Please enter your email to reset the password."
            return
        }

        auth.sendPasswordResetEmail(email.value!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _errorMessage.value = "Password reset email sent. Check your inbox."
                } else {
                    _errorMessage.value = task.exception?.message ?: "Failed to send reset email."
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _isLoggedIn.value = false
        _currentUserName.value = null
    }

    // MODIFIED: Generic success message applied after a successful email update.
    fun updateEmail(newEmail: String) {
        val user = auth.currentUser
        if (user != null && newEmail.isNotBlank()) {
            user.updateEmail(newEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _currentUserName.value = user.email // Update LiveData immediately
                        _errorMessage.value = "User details updated successfully."
                    } else {
                        _errorMessage.value = task.exception?.message ?: "Failed to update email. Re-login may be required."
                    }
                }
        } else {
            _errorMessage.value = "Please enter a valid email."
        }
    }

    // NEW SIMULATION FUNCTION: To be called when non-email fields are updated
    fun simulateLocalUpdateSuccess() {
        _errorMessage.value = "User details updated successfully."
    }

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