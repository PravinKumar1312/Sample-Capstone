package com.gladden.skillsyncai

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.gladden.skillsyncai.ui.theme.SkillSyncAITheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillSyncAITheme {
                // The actual ViewModel initialization is moved to a dedicated function
                MainAppContent()
            }
        }
    }
}

/**
 * NEW: Dedicated Composable to house the ViewModel initialization logic,
 * separating it from the core Activity entry point for better Preview stability.
 */
@Composable
fun MainAppContent() {
    // This code runs only during the actual runtime, not in the Preview.
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.AuthViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    val isLoggedIn by viewModel.isLoggedIn.observeAsState(initial = false)

    if (isLoggedIn) {
        HomeScreen(onLogout = { viewModel.signOut() })
    } else {
        LoginScreen()
    }
}

data class SkillBoxItem(
    val title: String,
    val description: String,
    val color: Color
)

sealed class Screen(val title: String) {
    object Home : Screen("Home")
    object History : Screen("History")
    object UserDetails : Screen("User Details")
    object Settings : Screen("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    // We access the ViewModel within HomeScreen, relying on the Compose runtime scope.
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.AuthViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    val profileImageUri by viewModel.profileImageUri.observeAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // State to track the last user interaction time
    // FIX APPLIED: Using standard mutableStateOf(Long) to fix the ClassCastException and compilation issues.
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // LaunchedEffect to manage the inactivity timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1.minutes.inWholeMilliseconds) // Check every 1 minute
            val currentTime = System.currentTimeMillis()
            // FIX APPLIED: Accessing directly, without .longValue
            val minutesSinceLastInteraction = (currentTime - lastInteractionTime) / (60 * 1000)

            if (minutesSinceLastInteraction >= 15) { // Set your desired inactivity time here (e.g., 15 minutes)
                onLogout()
                break
            }
        }
    }

    // Function to update interaction time
    val onUserInteraction = {
        lastInteractionTime = System.currentTimeMillis() // FIX APPLIED: Direct assignment
    }

    val darkPurple = Color(0xFF483D8B)
    val lightPurple = darkPurple.copy(alpha = 0.6f)
    val gradientColors = listOf(darkPurple, lightPurple)

    // Logic to select the correct painter for the profile icon
    val profilePainter: Painter = if (profileImageUri.isNullOrBlank()) {
        painterResource(id = R.drawable.ic_launcher_foreground) // Default icon if no URI is set
    } else {
        rememberAsyncImagePainter(model = profileImageUri!!.toUri())
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(250.dp), // Set a fixed width here
                drawerContainerColor = Color.LightGray.copy(alpha = 0.9f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Menu",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black // Make menu text dark
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                NavigationDrawerItem(
                    label = { Text(Screen.Home.title, color = Color.Black) }, // Improved contrast
                    selected = currentScreen == Screen.Home,
                    onClick = {
                        currentScreen = Screen.Home
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(Screen.History.title, color = Color.Black) }, // Improved contrast
                    selected = currentScreen == Screen.History,
                    onClick = {
                        currentScreen = Screen.History
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(Screen.UserDetails.title, color = Color.Black) }, // Improved contrast
                    selected = currentScreen == Screen.UserDetails,
                    onClick = {
                        currentScreen = Screen.UserDetails
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(Screen.Settings.title, color = Color.Black) }, // Improved contrast
                    selected = currentScreen == Screen.Settings,
                    onClick = {
                        currentScreen = Screen.Settings
                        scope.launch { drawerState.close() }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Logout", color = Color.Black) }, // Improved contrast
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Black) } // Improved contrast
                )
            }
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        // Main content and Scaffold with the new inactivity detection
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SkillSyncAI", color = Color.White) }, // Keep title white for contrast
                    navigationIcon = {
                        IconButton(onClick = {
                            onUserInteraction() // Reset timer on navigation icon click
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        // Profile Icon action to navigate to User Details
                        IconButton(onClick = {
                            onUserInteraction()
                            currentScreen = Screen.UserDetails // NAVIGATE TO USER DETAILS
                        }) {
                            Image(
                                painter = profilePainter, // LOAD CUSTOM IMAGE
                                contentDescription = "User Profile",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onUserInteraction() // Reset timer on any click on the background
                }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = gradientColors))
                    .padding(paddingValues)
            ) {
                ScreenContent(currentScreen = currentScreen, modifier = Modifier.fillMaxSize())

                if (currentScreen == Screen.Home) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        TextField(
                            value = "",
                            onValueChange = {},
                            label = { Text("Enter your query...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black, // Input text
                                unfocusedTextColor = Color.Black, // Input text
                                focusedLabelColor = Color.DarkGray, // Label text
                                unfocusedLabelColor = Color.DarkGray, // Label text
                                cursorColor = Color.Black
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenContent(currentScreen: Screen, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentScreen) {
            is Screen.Home -> SkillGridScreen()
            is Screen.History -> HistoryScreen()
            is Screen.UserDetails -> UserDetailsScreen()
            is Screen.Settings -> SettingsScreen()
        }
    }
}

@Composable
fun SkillGridScreen() {
    val skillBoxes = listOf(
        SkillBoxItem("AI Chat", "Ask questions and get insights.", Color(0xFF64B5F6)),
        SkillBoxItem("Skill Matching", "Find skills that match you.", Color(0xFF81C784)),
        SkillBoxItem("Learning Paths", "Discover new learning paths.", Color(0xFFFFB74D)),
        SkillBoxItem("Progress Tracker", "Track your skill progress.", Color(0xFFE57373))
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        items(skillBoxes) { item ->
            SkillBox(item = item)
        }
    }
}

@Composable
fun SkillBox(item: SkillBoxItem) {
    Card(
        modifier = Modifier
            .size(150.dp)
            .clickable { /* Handle box click logic here */ },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = item.color.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black // Change text color to a darker shade
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                fontSize = 12.sp,
                color = Color.DarkGray // Change text color to a darker shade
            )
        }
    }
}

@Composable
fun HistoryScreen() {
    Text(text = "History Page", fontSize = 24.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen() {
    // This section is the source of the preview crash due to the ClassCastException.
    // The solution is to leave it as-is for the runtime, but acknowledge the preview will fail.
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModel.AuthViewModelFactory(application))

    // Data from ViewModel
    val currentEmail by viewModel.currentUserName.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val profileImageUri by viewModel.profileImageUri.observeAsState()

    // Activity Result Launcher for image selection (Only PNG/JPG files)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            // Pass the actual Uri object to the ViewModel for copying/saving
            viewModel.saveProfileImageUri(uri)
        }
    }

    // --- LOCAL STATE MANAGEMENT ---
    var isEditing by remember { mutableStateOf(false) }

    // Placeholder Data (In a real app, these would come from a database/repository)
    val initialName = "Pravin Kumar"
    val initialAge = "25"
    val initialSkills = "Kotlin, Compose, Firebase"

    // Editable Field States (Use mutableStateOf to track current input)
    var editableName by remember { mutableStateOf(initialName) }
    var editableAge by remember { mutableStateOf(initialAge) }
    var editableEmail by remember { mutableStateOf(currentEmail ?: "") }
    var editableSkills by remember { mutableStateOf(initialSkills) }
    var editableLocation by remember { mutableStateOf("India") }


    // Update editableEmail when the Firebase email loads
    LaunchedEffect(currentEmail) {
        editableEmail = currentEmail ?: ""
    }

    // Determine if the email field specifically changed
    val isEmailChanged = remember {
        derivedStateOf {
            isEditing && editableEmail != (currentEmail ?: "")
        }
    }

    // Determine if any NON-EMAIL field changed
    val isLocalDataChanged = remember {
        derivedStateOf {
            isEditing && (
                    editableName != initialName ||
                            editableAge != initialAge ||
                            editableSkills != initialSkills ||
                            editableLocation != "India"
                    )
        }
    }

    // Determine if any field, including email, has changed to enable the Save button
    val isDataChanged = remember {
        derivedStateOf {
            isEmailChanged.value || isLocalDataChanged.value
        }
    }

    // Handles the Save button click
    val onSaveDetails = {
        // 1. Handle Firebase (Email) update if changed
        if (isEmailChanged.value) {
            viewModel.updateEmail(editableEmail)
        }

        // 2. Handle local/placeholder data update if changed
        if (isLocalDataChanged.value) {
            // Call the ViewModel function to simulate local data save success
            viewModel.simulateLocalUpdateSuccess()
        }

        // Exit edit mode
        isEditing = false
    }

    // Logic to select the correct painter for the profile image
    val profilePainter: Painter = if (profileImageUri.isNullOrBlank()) {
        painterResource(id = R.drawable.ic_launcher_foreground)
    } else {
        rememberAsyncImagePainter(model = profileImageUri!!.toUri())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()), // Enables scrolling for keyboard/long content
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // --- SECTION 1: PROFILE HEADER AND EDIT BUTTON ---

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.width(40.dp)) // Spacer for centering

            // User Profile Image - Clickable for upload in edit mode
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable(enabled = isEditing) {
                        // Launch file picker restricted to images
                        imagePickerLauncher.launch("image/jpeg,image/png")
                    }
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = profilePainter, // LOAD CUSTOM IMAGE
                    contentDescription = "User Profile",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Edit/Done Button
            IconButton(onClick = {
                // If exiting edit mode with the button, revert changes unless saved
                if (isEditing) {
                    editableName = initialName
                    editableAge = initialAge
                    editableEmail = currentEmail ?: ""
                    editableSkills = initialSkills
                    editableLocation = "India"
                }
                isEditing = !isEditing
            }) {
                Icon(
                    imageVector = if (isEditing) Icons.Filled.Done else Icons.Filled.Edit,
                    contentDescription = if (isEditing) "Done Editing" else "Edit Details",
                    tint = Color.Black
                )
            }
        }

        Text(
            text = "User Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- SECTION 2: USER DETAILS FORM FIELDS ---

        ProfileDetailField(label = "User Name", value = editableName, onValueChange = { editableName = it }, isEditing = isEditing, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
        ProfileDetailField(label = "Age", value = editableAge, onValueChange = { editableAge = it }, isEditing = isEditing, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))
        ProfileDetailField(label = "Email", value = editableEmail, onValueChange = { editableEmail = it }, isEditing = isEditing, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
        ProfileDetailField(label = "Skills", value = editableSkills, onValueChange = { editableSkills = it }, isEditing = isEditing, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
        ProfileDetailField(label = "Location", value = editableLocation, onValueChange = { editableLocation = it }, isEditing = isEditing, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done))

        // --- SECTION 3: SAVE BUTTON & MESSAGES ---

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button (Only visible and active in edit mode and if data has changed)
        if (isEditing) {
            Button(
                onClick = onSaveDetails,
                enabled = isDataChanged.value, // Activated only if data has changed
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text("Save Changes", fontSize = 18.sp)
            }
        }

        // Error/Success Message
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = if (errorMessage!!.contains("successfully")) Color(0xFF1B5E20) else Color.Red,
                modifier = Modifier.padding(top = 16.dp).padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp)) // Extra space at bottom for scrolling
    }
}

// Helper Composable for clean presentation of fields
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
) {
    val fieldModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp, horizontal = 16.dp)

    if (isEditing) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.DarkGray) },
            modifier = fieldModifier,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.DarkGray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black
            )
        )
    } else {
        Column(modifier = fieldModifier.padding(vertical = 8.dp)) {
            Text(label, color = Color.DarkGray, fontSize = 14.sp)
            Text(
                text = value,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
        }
    }
}


@Composable
fun SettingsScreen() {
    Text(text = "Settings Page", fontSize = 24.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // FINAL FIX: Calling the mock display for safe rendering.
    SkillSyncAITheme {
        MockHomeScreenDisplay()
    }
}

@Composable
fun PreviewUserDetailsContent(
    isEditing: Boolean = false,
    email: String = "preview.user@mock.com"
) {
    // This mocks the UI state for rendering purposes only.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // --- Mocked Profile Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.width(40.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "User Profile",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterVertically)
            )
            // Mocked Edit/Done Button for visibility
            Icon(
                imageVector = if (isEditing) Icons.Filled.Done else Icons.Filled.Edit,
                contentDescription = if (isEditing) "Done Editing" else "Edit Details",
                tint = Color.Black,
                modifier = Modifier.size(24.dp).clickable { /* Mock action */ }
            )
        }

        Text(
            text = "User Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- Mocked Form Fields (Using the existing helper for display) ---
        // We call ProfileDetailField directly with mock/static data
        ProfileDetailField(label = "User Name", value = "Pravin Kumar (Mock)", onValueChange = {}, isEditing = isEditing)
        ProfileDetailField(label = "Age", value = "25", onValueChange = {}, isEditing = isEditing, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        ProfileDetailField(label = "Email", value = email, onValueChange = {}, isEditing = isEditing, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        ProfileDetailField(label = "Skills", value = "Kotlin, Compose, Firebase", onValueChange = {}, isEditing = isEditing)
        ProfileDetailField(label = "Location", value = "India", onValueChange = {}, isEditing = isEditing)

        Spacer(modifier = Modifier.height(32.dp))

        // Mocked Save Button for visibility
        if (isEditing) {
            Button(
                onClick = { /* Mock save */ },
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 16.dp)
            ) {
                Text("Save Changes", fontSize = 18.sp)
            }
        }
        // Mocked success message
        if (!isEditing) {
            Text(
                text = "Render successful: Mock data shown.",
                color = Color.Green.copy(red = 0.3f),
                modifier = Modifier.padding(top = 16.dp).padding(horizontal = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun MockHomeScreenDisplay() {
    // This display is safe for preview as it avoids the crashing ViewModel factory.
    val mockProfilePainter: Painter = painterResource(id = R.drawable.ic_launcher_foreground)

    HomeScreenDisplay(
        profilePainter = mockProfilePainter,
        currentScreen = Screen.Home,
        onUserInteraction = {},
        onLogout = {}
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenDisplay(
    profilePainter: Painter,
    currentScreen: Screen,
    onUserInteraction: () -> Unit,
    onLogout: () -> Unit
) {
    // NOTE: This body is extracted from your original HomeScreen,
    // now accepting necessary parameters instead of accessing ViewModel directly.

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // Mock state management for preview navigation
    var mockCurrentScreen by remember { mutableStateOf(currentScreen) }

    val darkPurple = Color(0xFF483D8B)
    val lightPurple = darkPurple.copy(alpha = 0.6f)
    val gradientColors = listOf(darkPurple, lightPurple)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // (Drawer content remains the same - copy this part from your full HomeScreen)
            ModalDrawerSheet(
                modifier = Modifier.width(250.dp),
                drawerContainerColor = Color.LightGray.copy(alpha = 0.9f)
            ) {
                // ... [Full Drawer Content, replace the navigation items with the ones below] ...

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Menu", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                NavigationDrawerItem(label = { Text(Screen.Home.title, color = Color.Black) }, selected = mockCurrentScreen == Screen.Home, onClick = { mockCurrentScreen = Screen.Home; scope.launch { drawerState.close() } })
                NavigationDrawerItem(label = { Text(Screen.History.title, color = Color.Black) }, selected = mockCurrentScreen == Screen.History, onClick = { mockCurrentScreen = Screen.History; scope.launch { drawerState.close() } })
                NavigationDrawerItem(label = { Text(Screen.UserDetails.title, color = Color.Black) }, selected = mockCurrentScreen == Screen.UserDetails, onClick = { mockCurrentScreen = Screen.UserDetails; scope.launch { drawerState.close() } })
                NavigationDrawerItem(label = { Text(Screen.Settings.title, color = Color.Black) }, selected = mockCurrentScreen == Screen.Settings, onClick = { mockCurrentScreen = Screen.Settings; scope.launch { drawerState.close() } })

                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Logout", color = Color.Black) },
                    selected = false,
                    onClick = onLogout,
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Black) }
                )
            }
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SkillSyncAI", color = Color.White) },
                    navigationIcon = { IconButton(onClick = { onUserInteraction(); scope.launch { drawerState.open() } }) { Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White) } },
                    actions = { IconButton(onClick = { onUserInteraction(); mockCurrentScreen = Screen.UserDetails }) { Image(painter = profilePainter, contentDescription = "User Profile", modifier = Modifier.size(40.dp).clip(CircleShape)) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White)
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onUserInteraction() }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = gradientColors))
                    .padding(paddingValues)
            ) {
                ScreenContent(currentScreen = mockCurrentScreen, modifier = Modifier.fillMaxSize())

                if (mockCurrentScreen == Screen.Home) {
                    Box(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(16.dp)
                    ) {
                        TextField(value = "", onValueChange = {}, label = { Text("Enter your query...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedLabelColor = Color.DarkGray, unfocusedLabelColor = Color.DarkGray, cursorColor = Color.Black))
                    }
                }
            }
        }
    }
}