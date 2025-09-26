// Option 1: Add this to your MainActivity.kt file below the main class

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData

class PreviewAuthViewModel : ViewModel() {
    // Mock LiveData values for preview
    val isLoggedIn: LiveData<Boolean> = MutableLiveData(true)
    val profileImageUri: LiveData<String?> = MutableLiveData(null)
    val currentUserName: LiveData<String?> = MutableLiveData("preview.user@mock.com")
    val errorMessage: LiveData<String?> = MutableLiveData(null)

    // Mock functions (empty body is okay for preview)
    fun signOut() {}
    fun saveProfileImageUri(uri: android.net.Uri) {}
    fun updateEmail(newEmail: String) {}
    fun simulateLocalUpdateSuccess() {}
    fun sendPasswordResetEmail() {}
    fun registerUser() {}
    fun loginUser() {}
}