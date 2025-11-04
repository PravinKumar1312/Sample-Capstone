package com.gladden.skillsyncai

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.gladden.skillsyncai.ui.theme.SkillSyncAITheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

// Design System Palette
val DarkBackgroundColor = Color(0xFF1C1C1E)
val SurfaceColor = Color(0xFF2C2C2E)
val PrimaryTextColor = Color(0xFFE0E0E0)
val SecondaryTextColor = Color.Gray
val AccentColor = Color(0xFFBB86FC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillSyncAITheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    // Assuming AuthViewModel and R.drawable.ic_launcher_foreground exist in your project
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.AuthViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    val isLoggedIn by viewModel.isLoggedIn.observeAsState(initial = false)

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackgroundColor) {
        if (isLoggedIn) {
            HomeScreen(onLogout = { viewModel.signOut() })
        } else {
            // Assuming LoginScreen() exists
            // LoginScreen()
            // Placeholder since LoginScreen code wasn't provided,
            // but the logic relies on AuthViewModel.
            Text("Login Screen Placeholder", color = PrimaryTextColor, modifier = Modifier.padding(16.dp))
        }
    }
}

// Updated Data structures
data class SkillBoxItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

sealed class Screen(val title: String, val icon: ImageVector) {
    object Home : Screen("Home", Icons.Filled.Home)
    object History : Screen("History", Icons.Filled.History)
    object UserDetails : Screen("User Details", Icons.Filled.Person)
    object Settings : Screen("Settings", Icons.Filled.Settings)
}

// HOME SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    // Assuming AuthViewModel exists
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.AuthViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    val profileImageUri by viewModel.profileImageUri.observeAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(currentScreen) {
        viewModel.clearErrorMessage()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1.minutes.inWholeMilliseconds)
            val currentTime = System.currentTimeMillis()
            // Keep the timeout logic, adjust time as needed
            if ((currentTime - lastInteractionTime) / (60 * 1000) >= 15) {
                // onLogout()
                break
            }
        }
    }

    val onUserInteraction = { lastInteractionTime = System.currentTimeMillis() }

    // Assuming R.drawable.ic_launcher_foreground exists
    val profilePainter: Painter = if (profileImageUri.isNullOrBlank()) {
        painterResource(id = R.drawable.ic_launcher_foreground)
    } else {
        rememberAsyncImagePainter(model = profileImageUri!!.toUri())
    }

    HomeScreenDisplay(
        profilePainter = profilePainter,
        currentScreen = currentScreen,
        onUserInteraction = onUserInteraction,
        onLogout = onLogout,
        onScreenChange = { screen ->
            currentScreen = screen
            scope.launch { drawerState.close() }
        },
        drawerState = drawerState,
        scope = scope
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenDisplay(
    profilePainter: Painter,
    currentScreen: Screen,
    onUserInteraction: () -> Unit,
    onLogout: () -> Unit,
    onScreenChange: (Screen) -> Unit,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = DarkBackgroundColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "SkillSyncAI",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTextColor
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                listOf(Screen.Home, Screen.History, Screen.UserDetails, Screen.Settings).forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = { onScreenChange(screen) },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = AccentColor.copy(alpha = 0.15f),
                            selectedIconColor = AccentColor,
                            selectedTextColor = AccentColor,
                            unselectedIconColor = SecondaryTextColor,
                            unselectedTextColor = SecondaryTextColor
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = onLogout,
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = SecondaryTextColor,
                        unselectedTextColor = SecondaryTextColor
                    )
                )
            }
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.title, color = PrimaryTextColor, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { onUserInteraction(); scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = PrimaryTextColor)
                        }
                    },
                    actions = {
                        IconButton(onClick = { onUserInteraction(); onScreenChange(Screen.UserDetails) }) {
                            Image(
                                painter = profilePainter,
                                contentDescription = "User Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, PrimaryTextColor, CircleShape)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackgroundColor)
                .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onUserInteraction)
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                ScreenContent(currentScreen = currentScreen, modifier = Modifier.fillMaxSize())

                if (currentScreen == Screen.Home) {
                    BottomQueryBar()
                }
            }
        }
    }
}

@Composable
fun ScreenContent(currentScreen: Screen, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(bottom = if (currentScreen == Screen.Home) 80.dp else 0.dp)

        when (currentScreen) {
            is Screen.Home -> SkillGridScreen(modifier = contentModifier)
            is Screen.History -> HistoryScreen(modifier = contentModifier)
            is Screen.UserDetails -> UserDetailsScreen(modifier = contentModifier)
            is Screen.Settings -> SettingsScreen(modifier = contentModifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomQueryBar() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background with blur (Frosted Glass Effect)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(28.dp))
                    .blur(20.dp)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            )
            // TextField on top
            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Ask SkillSync AI...", color = SecondaryTextColor) },
                leadingIcon = { Icon(Icons.Filled.Mic, contentDescription = "Voice Input", tint = SecondaryTextColor) },
                trailingIcon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = AccentColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = PrimaryTextColor,
                    unfocusedTextColor = PrimaryTextColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

// HOME SCREEN COMPONENTS
@Composable
fun SkillGridScreen(modifier: Modifier = Modifier) {
    val skillBoxes = listOf(
        SkillBoxItem("AI Chat", "Ask questions and get insights.", Icons.Filled.AutoAwesome, Color(0xFF64B5F6)),
        SkillBoxItem("Skill Matching", "Find skills that match you.", Icons.Filled.PersonSearch, Color(0xFF81C784)),
        SkillBoxItem("Learning Paths", "Discover new learning paths.", Icons.Filled.Timeline, Color(0xFFFFB74D)),
        SkillBoxItem("Progress Tracker", "Track your skill progress.",
            Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFE57373))
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
            .fillMaxWidth()
            .height(150.dp)
            .clickable { /* Handle box click logic here */ },
        shape = RoundedCornerShape(16.dp),
        // Subtle elevation for depth
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = item.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryTextColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.description, fontSize = 12.sp, color = SecondaryTextColor, maxLines = 2)
        }
    }
}

// HISTORY SCREEN
@Composable
fun HistoryScreen(modifier: Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Activity History", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryTextColor, modifier = Modifier.padding(bottom = 16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(5) { index ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Description, contentDescription = "Query", tint = SecondaryTextColor)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Query $index: 'What is Kotlin Coroutines?'", color = PrimaryTextColor, fontWeight = FontWeight.Medium)
                            Text("2 hours ago | Path: Learning Paths", color = SecondaryTextColor, fontSize = 12.sp)
                        }
                    }
                    HorizontalDivider(color = PrimaryTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

// SETTINGS SCREEN
@Composable
fun SettingsScreen(modifier: Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Application Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryTextColor, modifier = Modifier.padding(bottom = 24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsItem(Icons.Filled.Notifications, "Notifications", "Receive weekly updates")
                HorizontalDivider(color = PrimaryTextColor.copy(alpha = 0.1f))
                SettingsItem(Icons.Filled.VpnKey, "Privacy Policy", "Review our data usage")
                HorizontalDivider(color = PrimaryTextColor.copy(alpha = 0.1f))
                SettingsItem(Icons.Filled.Security, "Security Preferences", "Manage account login")
            }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle settings click */ }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = PrimaryTextColor, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PrimaryTextColor, fontWeight = FontWeight.Medium)
            Text(subtitle, color = SecondaryTextColor, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Go", tint = SecondaryTextColor)
    }
}

// USER DETAILS SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    // Assuming AuthViewModel exists
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModel.AuthViewModelFactory(application))

    val currentEmail by viewModel.currentUserName.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val profileImageUri by viewModel.profileImageUri.observeAsState()

    val initialName by viewModel.userNameDetail.observeAsState("Pravin Kumar")
    val initialAge by viewModel.userAgeDetail.observeAsState("25")
    val initialSkills by viewModel.userSkillsDetail.observeAsState("Kotlin, Compose, Firebase")
    val initialLocation by viewModel.userLocationDetail.observeAsState("India")

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.saveProfileImageUri(it) }
    }

    var isEditing by remember { mutableStateOf(false) }

    var editableName by remember { mutableStateOf(initialName) }
    var editableAge by remember { mutableStateOf(initialAge) }
    var editableEmail by remember { mutableStateOf(currentEmail ?: "") }
    var editableSkills by remember { mutableStateOf(initialSkills) }
    var editableLocation by remember { mutableStateOf(initialLocation) }

    val isAgeValid = remember(editableAge) { editableAge?.all { it.isDigit() } == true && editableAge?.isNotBlank() == true }

    LaunchedEffect(initialName, initialAge, initialSkills, initialLocation, currentEmail) {
        editableName = initialName
        editableAge = initialAge
        editableSkills = initialSkills
        editableLocation = initialLocation
        editableEmail = currentEmail ?: ""
    }

    val onSaveDetails = {
        if (!isAgeValid) {
            viewModel.setErrorMessage("Age must be a valid number.")
        } else {
            viewModel.updateEmail(editableEmail)
            viewModel.setUserNameDetail(editableName)
            viewModel.setUserAgeDetail(editableAge)
            viewModel.setUserSkillsDetail(editableSkills)
            viewModel.setUserLocationDetail(editableLocation)
            viewModel.clearErrorMessage()
            // viewModel.simulateLocalUpdateSuccess() // Assuming this function exists for success message
            isEditing = false
        }
    }

    // Assuming R.drawable.ic_launcher_foreground exists
    val profilePainter: Painter = if (profileImageUri.isNullOrBlank()) {
        painterResource(id = R.drawable.ic_launcher_foreground)
    } else {
        rememberAsyncImagePainter(model = profileImageUri!!.toUri())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = profilePainter,
                contentDescription = "User Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, PrimaryTextColor, CircleShape)
            )
            if (isEditing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(AccentColor)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(8.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Change Picture", tint = DarkBackgroundColor, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(initialName ?: "User", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryTextColor)
        Spacer(Modifier.height(32.dp))

        // Details Card
        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Account Configuration", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryTextColor)
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            if (isEditing) Icons.Filled.Close else Icons.Filled.Edit,
                            contentDescription = if (isEditing) "Cancel" else "Edit",
                            tint = PrimaryTextColor
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                EditableProfileRow(
                    label = "Name",
                    value = editableName,
                    isEditing = isEditing,
                    keyboardType = KeyboardType.Text,
                    onValueChange = { editableName = it }
                )
                EditableProfileRow(
                    label = "Age",
                    value = editableAge,
                    isEditing = isEditing,
                    keyboardType = KeyboardType.Number,
                    onValueChange = { editableAge = it }
                )
                EditableProfileRow(
                    label = "Email",
                    value = editableEmail,
                    isEditing = isEditing,
                    keyboardType = KeyboardType.Email,
                    onValueChange = { editableEmail = it },
                    isEmail = true // Flag to handle non-editable look
                )
                EditableProfileRow(
                    label = "Location",
                    value = editableLocation,
                    isEditing = isEditing,
                    keyboardType = KeyboardType.Text,
                    onValueChange = { editableLocation = it }
                )

                // Skills Section
                Text("Skills", color = SecondaryTextColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                if (isEditing) {
                    AuthOutlinedTextField(
                        value = editableSkills ?: "",
                        onValueChange = { editableSkills = it },
                        label = "Skills (comma-separated)",
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        singleLine = false // Allow multiline for better skill input
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items((editableSkills ?: "").split(",").map { it.trim() }.filter { it.isNotBlank() }) { skill ->
                            SkillPill(skill = skill)
                        }
                    }
                }

                if (isEditing) {
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onSaveDetails, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentColor)) {
                        Text("Save Changes", fontWeight = FontWeight.Bold, color = DarkBackgroundColor)
                    }
                }

                if (errorMessage != null) {
                    Text(
                        // Check for 'success' or just rely on your view model logic for color
                        text = errorMessage!!,
                        color = if (errorMessage!!.contains("success")) Color.Green else Color.Red,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

// **REPLACEMENT for ProfileDetailRow**
@Composable
fun EditableProfileRow(
    label: String,
    value: String?,
    isEditing: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    isEmail: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = SecondaryTextColor, fontWeight = FontWeight.Medium)

            if (!isEditing) {
                // Display mode: Show value on the same line
                Text(value ?: "N/A", color = PrimaryTextColor, fontSize = 16.sp)
            } else if (isEmail) {
                // Edit mode for Email: Display as simple text (often non-editable)
                Text(value ?: "N/A", color = SecondaryTextColor, fontSize = 14.sp)
            }
        }

        if (isEditing && !isEmail) {
            // Edit mode for Name, Age, Location: Show compact TextField on a new line
            Spacer(Modifier.height(4.dp))
            AuthOutlinedTextField(
                value = value ?: "",
                onValueChange = onValueChange,
                label = "", // No floating label needed
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            )
        }

        // Divider placement remains consistent
        HorizontalDivider(color = PrimaryTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun SkillPill(skill: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AccentColor.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, AccentColor.copy(alpha = 0.5f)),
    ) {
        Text(text = skill, color = PrimaryTextColor, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 14.sp)
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
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true // Added this parameter for multiline support
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = if (label.isNotBlank()) { { Text(label) } } else null, // Conditional label
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        singleLine = singleLine, // Use the new parameter
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Gray.copy(alpha = 0.2f),
            unfocusedContainerColor = Color.Gray.copy(alpha = 0.1f),
            focusedLabelColor = Color.White.copy(alpha = 0.7f),
            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = AccentColor,
            focusedBorderColor = Color.White.copy(alpha = 0.8f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun FrostedGlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .blur(20.dp) // Apply blur effect
                .background(Color.White.copy(alpha = 0.1f)) // Translucent color
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        Column(modifier = Modifier, content = content)
    }
}