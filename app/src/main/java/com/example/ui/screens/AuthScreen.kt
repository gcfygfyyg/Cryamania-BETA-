package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserAccount
import com.example.ui.components.DevYellowHammerBadge
import com.example.ui.components.RobloxBeveledButton
import com.example.ui.components.RobloxBeveledCard
import com.example.ui.theme.RobloxBlack
import com.example.ui.theme.RobloxBorder
import com.example.ui.theme.RobloxDarkGray
import com.example.ui.theme.RobloxLightGray
import com.example.ui.theme.RobloxMidGray
import com.example.ui.theme.RobloxPureWhite
import com.example.ui.theme.RobloxSilver

@Composable
fun AuthScreen(
    allUsers: List<UserAccount>,
    onRegister: (username: String, password: String, avatarColor: String) -> Unit,
    onLogin: (username: String, password: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Create Account, 1: Log In
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordConfirmInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Minimalistic Black & Yellow + Tone Options
    val colorOptions = listOf(
        "#FFD600", // Vibrant Yellow
        "#FFFFFF", // Pure White
        "#D6D6D6", // Light Silver
        "#757575", // Slate Gray
        "#262626"  // Obsidian Black
    )
    var selectedColor by remember { mutableStateOf(colorOptions[0]) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RobloxBlack)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        RobloxBeveledCard(
            backgroundColor = RobloxDarkGray,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Minimalistic Black & Yellow App Logo Header
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RobloxBlack)
                        .border(2.dp, RobloxPureWhite, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_by_1790444477261),
                        contentDescription = "CryaMania Black and Yellow Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "CRYAMANIA",
                    color = RobloxPureWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "3D CAFE HANGOUT • BLACK & YELLOW",
                    color = RobloxSilver,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Classic Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(RobloxMidGray)
                        .border(1.5.dp, RobloxBorder, RoundedCornerShape(3.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (selectedTab == 0) RobloxPureWhite else Color.Transparent)
                            .clickable {
                                selectedTab = 0
                                errorMessage = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register",
                            color = if (selectedTab == 0) RobloxBlack else RobloxLightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (selectedTab == 1) RobloxPureWhite else Color.Transparent)
                            .clickable {
                                selectedTab = 1
                                errorMessage = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log In",
                            color = if (selectedTab == 1) RobloxBlack else RobloxLightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Banner
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RobloxMidGray)
                            .border(1.dp, RobloxPureWhite)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠ ${errorMessage!!}",
                            color = RobloxPureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (selectedTab == 0) {
                    // --- 2009 SIGN UP ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RobloxMidGray)
                            .border(1.dp, RobloxBorder)
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🔨", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2009 Security & Unique Username",
                                    color = RobloxPureWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Every account requires a password. Usernames are unique. The account named 'CRYA' holds the classic 2009 Hammer badge.",
                                color = RobloxLightGray,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Username Input
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("Username", color = RobloxLightGray) },
                        placeholder = { Text("e.g. CRYA, Builderman, 1x1x1x1", color = RobloxSilver) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RobloxPureWhite,
                            unfocusedBorderColor = RobloxBorder,
                            focusedTextColor = RobloxPureWhite,
                            unfocusedTextColor = RobloxLightGray,
                            focusedContainerColor = RobloxMidGray,
                            unfocusedContainerColor = RobloxMidGray
                        ),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_username_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password", color = RobloxLightGray) },
                        placeholder = { Text("Enter account password", color = RobloxSilver) },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = RobloxLightGray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RobloxPureWhite,
                            unfocusedBorderColor = RobloxBorder,
                            focusedTextColor = RobloxPureWhite,
                            unfocusedTextColor = RobloxLightGray,
                            focusedContainerColor = RobloxMidGray,
                            unfocusedContainerColor = RobloxMidGray
                        ),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Confirm Password Input
                    OutlinedTextField(
                        value = passwordConfirmInput,
                        onValueChange = { passwordConfirmInput = it },
                        label = { Text("Confirm Password", color = RobloxLightGray) },
                        placeholder = { Text("Re-enter password", color = RobloxSilver) },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RobloxPureWhite,
                            unfocusedBorderColor = RobloxBorder,
                            focusedTextColor = RobloxPureWhite,
                            unfocusedTextColor = RobloxLightGray,
                            focusedContainerColor = RobloxMidGray,
                            unfocusedContainerColor = RobloxMidGray
                        ),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_confirm_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Avatar Color Picker (Monochrome)
                    Text(
                        text = "Avatar Monochrome Tone",
                        color = RobloxLightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(colorOptions) { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = selectedColor == hex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(color)
                                    .border(
                                        if (isSelected) 2.5.dp else 1.dp,
                                        if (isSelected) RobloxPureWhite else RobloxBorder,
                                        RoundedCornerShape(3.dp)
                                    )
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (hex == "#FFFFFF" || hex == "#EAEAEA" || hex == "#CCCCCC") Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Submit Register Beveled Button
                    RobloxBeveledButton(
                        onClick = {
                            when {
                                usernameInput.isBlank() -> {
                                    errorMessage = "Please enter a username."
                                }
                                usernameInput.trim().length < 3 -> {
                                    errorMessage = "Username must be at least 3 characters."
                                }
                                passwordInput.isEmpty() -> {
                                    errorMessage = "Password cannot be empty."
                                }
                                passwordInput.length < 4 -> {
                                    errorMessage = "Password must be at least 4 characters."
                                }
                                passwordInput != passwordConfirmInput -> {
                                    errorMessage = "Passwords do not match."
                                }
                                else -> {
                                    errorMessage = null
                                    onRegister(usernameInput.trim(), passwordInput, selectedColor)
                                }
                            }
                        },
                        isPrimary = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("create_account_submit_button")
                    ) {
                        Text(
                            text = "CREATE ACCOUNT (+1,000 CRIN)",
                            color = RobloxBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    // --- 2009 LOG IN ---
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("Username", color = RobloxLightGray) },
                        placeholder = { Text("Enter account username", color = RobloxSilver) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RobloxPureWhite,
                            unfocusedBorderColor = RobloxBorder,
                            focusedTextColor = RobloxPureWhite,
                            unfocusedTextColor = RobloxLightGray,
                            focusedContainerColor = RobloxMidGray,
                            unfocusedContainerColor = RobloxMidGray
                        ),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password", color = RobloxLightGray) },
                        placeholder = { Text("Enter account password", color = RobloxSilver) },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = RobloxLightGray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RobloxPureWhite,
                            unfocusedBorderColor = RobloxBorder,
                            focusedTextColor = RobloxPureWhite,
                            unfocusedTextColor = RobloxLightGray,
                            focusedContainerColor = RobloxMidGray,
                            unfocusedContainerColor = RobloxMidGray
                        ),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    RobloxBeveledButton(
                        onClick = {
                            when {
                                usernameInput.isBlank() -> {
                                    errorMessage = "Please enter your username."
                                }
                                passwordInput.isEmpty() -> {
                                    errorMessage = "Please enter your password."
                                }
                                else -> {
                                    errorMessage = null
                                    onLogin(usernameInput.trim(), passwordInput)
                                }
                            }
                        },
                        isPrimary = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("login_submit_button")
                    ) {
                        Text(
                            text = "LOG IN",
                            color = RobloxBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (allUsers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Saved Accounts on this Device:",
                            color = RobloxSilver,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        allUsers.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(RobloxMidGray)
                                    .border(1.dp, RobloxBorder, RoundedCornerShape(3.dp))
                                    .clickable {
                                        usernameInput = user.username
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = user.username,
                                                color = RobloxPureWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            if (user.isDeveloper) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                DevYellowHammerBadge(sizeDp = 16f)
                                            }
                                        }
                                        Text(
                                            text = "${user.crinBalance} CRIN • Password Protected",
                                            color = RobloxSilver,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "Select →",
                                    color = RobloxPureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            }
        }
    }
}
