package com.gladden.skillsyncai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gladden.skillsyncai.ui.theme.SkillSyncAITheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillSyncAITheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                if (isLoggedIn) {
                    HomeScreen(onLogout = { isLoggedIn = false })
                } else {
                    LoginScreen(onLoginSuccess = { isLoggedIn = true })
                }
            }
        }
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // State to track the last user interaction time
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // LaunchedEffect to manage the inactivity timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1.minutes.inWholeMilliseconds) // Check every 1 minute
            val currentTime = System.currentTimeMillis()
            val minutesSinceLastInteraction = (currentTime - lastInteractionTime) / (60 * 1000)

            if (minutesSinceLastInteraction >= 15) { // Set your desired inactivity time here (e.g., 15 minutes)
                onLogout()
                break
            }
        }
    }

    // Function to update interaction time
    val onUserInteraction = {
        lastInteractionTime = System.currentTimeMillis()
    }

    val darkPurple = Color(0xFF483D8B)
    val lightPurple = darkPurple.copy(alpha = 0.6f)
    val gradientColors = listOf(darkPurple, lightPurple)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
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
                    label = { Text(Screen.Home.title, color = Color.DarkGray) },
                    selected = currentScreen == Screen.Home,
                    onClick = {
                        currentScreen = Screen.Home
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(Screen.History.title, color = Color.DarkGray) },
                    selected = currentScreen == Screen.History,
                    onClick = {
                        currentScreen = Screen.History
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(Screen.UserDetails.title, color = Color.DarkGray) },
                    selected = currentScreen == Screen.UserDetails,
                    onClick = {
                        currentScreen = Screen.UserDetails
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(Screen.Settings.title, color = Color.DarkGray) },
                    selected = currentScreen == Screen.Settings,
                    onClick = {
                        currentScreen = Screen.Settings
                        scope.launch { drawerState.close() }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Logout", color = Color.DarkGray) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    icon = { Icon(Icons.Filled.ExitToApp, contentDescription = "Logout", tint = Color.DarkGray) }
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
                        IconButton(onClick = { onUserInteraction() /* Handle profile click */ }) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
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

// Placeholder composables for each new screen
@Composable
fun HistoryScreen() {
    Text(text = "History Page", fontSize = 24.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
}

@Composable
fun UserDetailsScreen() {
    Text(text = "User Details Page", fontSize = 24.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
}

@Composable
fun SettingsScreen() {
    Text(text = "Settings Page", fontSize = 24.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SkillSyncAITheme {
        HomeScreen(onLogout = {})
    }
}