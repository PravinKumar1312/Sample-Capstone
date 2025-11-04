package com.gladden.skillsyncai

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.AuthViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    val isLoginMode by viewModel.isLoginMode.observeAsState(true)
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val errorMessage by viewModel.errorMessage.observeAsState(null)

    // Design Palette
    val backgroundColor = Color(0xFF1C1C1E)
    val primaryTextColor = Color(0xFFE0E0E0)
    val secondaryTextColor = Color.Gray
    val accentColor = Color(0xFFBB86FC)
    val errorColor = Color(0xFFCF6679)

    var nameInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var skillsInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val onRegisterClicked = {
        viewModel.registerUser(
            name = nameInput,
            age = ageInput,
            skills = skillsInput
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isLoginMode) "Welcome Back" else "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryTextColor
            )

            AuthOutlinedTextField(
                value = email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = "Email Address",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            AuthOutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = "Password",
                keyboardType = KeyboardType.Password,
                imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle visibility", tint = secondaryTextColor)
                    }
                }
            )

            if (!isLoginMode) {
                AuthOutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = "Full Name",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
                AuthOutlinedTextField(
                    value = ageInput,
                    onValueChange = { ageInput = it },
                    label = "Age",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
                AuthOutlinedTextField(
                    value = skillsInput,
                    onValueChange = { skillsInput = it },
                    label = "Main Skills (e.g., Kotlin, Java)",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = errorColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isLoginMode) {
                        viewModel.loginUser()
                    } else {
                        onRegisterClicked()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = primaryTextColor
                )
            ) {
                Text(
                    text = if (isLoginMode) "Sign In" else "Register Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(onClick = {
                viewModel.toggleMode()
                nameInput = ""
                ageInput = ""
                skillsInput = ""
            }) {
                Text(
                    text = if (isLoginMode) "Don't have an account? Register" else "Already have an account? Sign In",
                    color = secondaryTextColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isLoginMode) {
                TextButton(onClick = { viewModel.sendPasswordResetEmail() }) {
                    Text(
                        text = "Forgot Password?",
                        color = secondaryTextColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Gray.copy(alpha = 0.2f),
            unfocusedContainerColor = Color.Gray.copy(alpha = 0.1f),
            focusedLabelColor = Color.White.copy(alpha = 0.7f),
            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFFBB86FC),
            focusedBorderColor = Color.White.copy(alpha = 0.8f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
        )
    )
}
