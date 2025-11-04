package com.gladden.skillsyncai

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.edit

// Tag for logging to help debug crashes related to file I/O
private const val TAG = "AuthViewModel"

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("local_user_data", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth by lazy { Firebase.auth }

    // LiveData for Auth State
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    // Authentication UI State
    private val _isLoginMode = MutableLiveData(true)
    val isLoginMode: LiveData<Boolean> = _isLoginMode
    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email
    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    // User State
    private val _currentUserName = MutableLiveData<String?>(null)
    val currentUserName: LiveData<String?> = _currentUserName

    // User Details (Loaded from SharedPreferences)
    private val _profileImageUri = MutableLiveData<String?>(
        sharedPrefs.getString("profile_image_uri", null)
    )
    val profileImageUri: LiveData<String?> = _profileImageUri

    private val _userNameDetail = MutableLiveData<String?>(sharedPrefs.getString("user_name_detail", null))
    val userNameDetail: LiveData<String?> = _userNameDetail

    private val _userAgeDetail = MutableLiveData<String?>(sharedPrefs.getString("user_age_detail", null))
    val userAgeDetail: LiveData<String?> = _userAgeDetail

    private val _userSkillsDetail = MutableLiveData<String?>(sharedPrefs.getString("user_skills_detail", null))
    val userSkillsDetail: LiveData<String?> = _userSkillsDetail

    private val _userLocationDetail = MutableLiveData<String?>(sharedPrefs.getString("user_location_detail", null))
    val userLocationDetail: LiveData<String?> = _userLocationDetail


    init {
        // --- FIX FOR LOGIN SKIP CLARIFICATION ---
        // PROBLEM 1: This correctly identifies a persistent Firebase session.
        // If you want to see the Login screen, you MUST use the Logout button or clear app data.
        _isLoggedIn.value = (auth.currentUser != null)
        _currentUserName.value = auth.currentUser?.email
        Log.d(TAG, "Initial Auth State: Logged In: ${_isLoggedIn.value}, User: ${auth.currentUser?.uid}")
    }

    // --- Core Authentication Methods ---

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        clearErrorMessage()
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        clearErrorMessage()
    }

    fun toggleMode() {
        _isLoginMode.value = !_isLoginMode.value!!
        _email.value = ""
        _password.value = ""
        clearErrorMessage()
    }

    fun registerUser(name: String, age: String, skills: String) {
        if (email.value.isNullOrBlank() || password.value.isNullOrBlank()) {
            setErrorMessage("Please enter both email and password.")
            return
        }

        auth.createUserWithEmailAndPassword(email.value!!, password.value!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveLocalUserDetails(
                        name = name.ifBlank { "User" },
                        age = age.ifBlank { "N/A" },
                        skills = skills.ifBlank { "Beginner" },
                        location = "Unknown"
                    )

                    setErrorMessage("Registration successful! Please log in.")
                    _isLoginMode.value = true
                    _email.value = ""
                    _password.value = ""
                } else {
                    Log.e(TAG, "Registration failed: ", task.exception)
                    setErrorMessage(when (task.exception) {
                        is FirebaseAuthUserCollisionException -> "This email is already in use."
                        is FirebaseAuthInvalidCredentialsException -> "The email address is badly formatted."
                        else -> task.exception?.message ?: "Registration failed."
                    })
                }
            }
    }

    fun loginUser() {
        if (email.value.isNullOrBlank() || password.value.isNullOrBlank()) {
            setErrorMessage("Please fill both email and password fields.")
            return
        }

        auth.signInWithEmailAndPassword(email.value!!, password.value!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isLoggedIn.value = true
                    _currentUserName.value = auth.currentUser?.email
                    Log.d(TAG, "Login successful for user: ${auth.currentUser?.uid}")

                    // Ensure default local details exist on login if they were cleared
                    if (sharedPrefs.getString("user_name_detail", null).isNullOrBlank()) {
                        saveLocalUserDetails(
                            name = auth.currentUser?.email ?: "User",
                            age = "N/A",
                            skills = "Beginner",
                            location = "Unknown"
                        )
                    }
                } else {
                    Log.e(TAG, "Login failed: ", task.exception)
                    setErrorMessage(when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password. Please try again."
                        else -> task.exception?.message ?: "Login failed."
                    })
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _isLoggedIn.value = false
        _currentUserName.value = null
        clearErrorMessage()
        Log.d(TAG, "User signed out.")
    }

    override fun onCleared() {
        super.onCleared()
        signOut()
    }

    // --- User Details & Image Handlers (FIXES FOR PROFILE CRASH) ---

    // Function to save all details (used only for registration/initial setup)
    fun saveLocalUserDetails(name: String?, age: String, skills: String, location: String) {
        sharedPrefs.edit().apply {
            putString("user_name_detail", name)
            putString("user_age_detail", age)
            putString("user_skills_detail", skills)
            putString("user_location_detail", location)
            apply()
        }

        // Update LiveData to reflect changes immediately
        _userNameDetail.value = name
        _userAgeDetail.value = age
        _userSkillsDetail.value = skills
        _userLocationDetail.value = location

        if (_errorMessage.value?.contains("failed") != true) {
            setErrorMessage("Local user details saved.")
        }
    }

    // ⭐ FIX: Individual setters used by UserDetailsScreen to save data upon editing.
    fun setUserNameDetail(name: String?) {
        _userNameDetail.value = name
        sharedPrefs.edit { putString("user_name_detail", name) }
    }

    fun setUserAgeDetail(age: String?) {
        _userAgeDetail.value = age
        sharedPrefs.edit { putString("user_age_detail", age) }
    }

    fun setUserSkillsDetail(skills: String?) {
        _userSkillsDetail.value = skills
        sharedPrefs.edit { putString("user_skills_detail", skills) }
    }

    fun setUserLocationDetail(location: String?) {
        _userLocationDetail.value = location
        sharedPrefs.edit { putString("user_location_detail", location) }
    }
    // ⭐ END FIX: Individual setters

    // Function to handle local image saving (where the crash is likely occurring)
    fun saveProfileImageUri(uri: Uri) {
        val timeStamp = System.currentTimeMillis()
        val destinationFile = File(application.filesDir, "profile_image_$timeStamp.jpg")

        try {
            // Use application.contentResolver to safely open the input stream from the URI
            application.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Store the URI string of the file saved locally in the app's internal storage
            val localFileUri = destinationFile.toURI().toString()
            sharedPrefs.edit { putString("profile_image_uri", localFileUri) }
            _profileImageUri.value = localFileUri
            setErrorMessage("Profile image updated successfully.")
            Log.d(TAG, "Image saved successfully to: $localFileUri")

        } catch (e: Exception) {
            // CRASH DEBUG: Log the full stack trace if file I/O fails (often due to permissions)
            Log.e(TAG, "FATAL IMAGE SAVE ERROR: Check Manifest Permissions", e)
            setErrorMessage("Failed to save image. Check Logcat (FATAL IMAGE SAVE ERROR) and Manifest permissions.")
        }
    }

    fun sendPasswordResetEmail() {
        if (email.value.isNullOrBlank()) {
            setErrorMessage("Please enter your email to reset the password.")
            return
        }

        auth.sendPasswordResetEmail(email.value!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    setErrorMessage("Password reset email sent. Check your inbox.")
                } else {
                    Log.e(TAG, "Password reset failed: ", task.exception)
                    setErrorMessage(task.exception?.message ?: "Failed to send reset email.")
                }
            }
    }

    // --- Message Management Methods ---

    // MODIFIED: Improved error handling for email update
    fun updateEmail(newEmail: String) {
        val user = auth.currentUser
        if (user != null && newEmail.isNotBlank()) {
            user.updateEmail(newEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _currentUserName.value = user.email
                        setErrorMessage("Email updated successfully. You may need to sign in again soon.")
                    } else {
                        Log.e(TAG, "Email update failed: ", task.exception)
                        // This often fails if the user hasn't logged in recently (requires re-authentication)
                        setErrorMessage("Failed to update email. Please sign out, sign back in immediately, and try again.")
                    }
                }
        } else {
            setErrorMessage("Invalid email provided for update.")
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

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
