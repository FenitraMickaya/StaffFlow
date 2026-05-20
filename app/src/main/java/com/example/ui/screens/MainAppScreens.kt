package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.Attendance
import com.example.database.Employee
import com.example.database.Payment
import com.example.database.ScanResult
import com.example.database.PayrollCalculation
import com.example.ui.composables.QrCodeGenerator
import com.example.ui.theme.*
import com.example.viewmodel.StaffFlowViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainAppScreens(viewModel: StaffFlowViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    var isSplashActive by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1800)
        isSplashActive = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isSplashActive) {
            SplashScreen()
        } else {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "home" -> WelcomeScreen(viewModel)
                    "login_admin" -> AdminLoginScreen(viewModel)
                    "login_employee" -> EmployeeLoginScreen(viewModel)
                    "admin_dashboard" -> AdminDashboardScreen(viewModel)
                    "employee_dashboard" -> EmployeeDashboardScreen(viewModel)
                    else -> WelcomeScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "SplashScale"
    )
    val opacity by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "SplashOpacity"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SlateBackground, SlateSurfaceVariant)
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = opacity)
                .size(160.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(EmeraldPrimary, EmeraldTertiary)
                    ),
                    shape = RoundedCornerShape(36.dp)
                )
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SlateSurface, RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "StaffFlow Splash Logo",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "StaffFlow",
            style = MaterialTheme.typography.displayLarge,
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.graphicsLayer(alpha = opacity)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Système Intelligent d'Émargement QR",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .graphicsLayer(alpha = opacity)
        )

        Spacer(modifier = Modifier.height(48.dp))

        CircularProgressIndicator(
            color = EmeraldPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(alpha = opacity)
        )
    }
}

// 1. WELCOME SCREEN
@Composable
fun WelcomeScreen(viewModel: StaffFlowViewModel) {
    val context = LocalContext.current
    var isLogoVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isLogoVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SlateBackground, SlateSurfaceVariant)
                )
            )
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Large glowing branding card
        AnimatedVisibility(
            visible = isLogoVisible,
            enter = scaleIn(initialScale = 0.8f) + fadeIn()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(EmeraldPrimary, EmeraldTertiary)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(4.dp)
            ) {
                // Subtle clean logo replacement matching adaptive branding
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SlateSurface, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = "StaffFlow Logo",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "StaffFlow",
            style = MaterialTheme.typography.displayLarge,
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("app_title_text")
        )

        Text(
            text = "Smart Employee & Attendance QR System",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )

        // Options
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .padding(vertical = 8.dp)
                .clickable { viewModel.navigateTo("login_admin") }
                .testTag("welcome_admin_card"),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SlateSurfaceVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Espace Administration",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gérer les employés, valider présences et salaires.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .padding(vertical = 8.dp)
                .clickable { viewModel.navigateTo("login_employee") }
                .testTag("welcome_employee_card"),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SlateSurfaceVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Employee",
                        tint = EmeraldSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Espace Employé",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Consultez votre badge QR unique, vos fiches de paie.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

// 2. ADMIN LOGIN SCREEN
@Composable
fun AdminLoginScreen(viewModel: StaffFlowViewModel) {
    var isSignUpMode by remember { mutableStateOf(false) }

    // States for login prefilled with remembered credentials
    var username by remember { mutableStateOf(viewModel.rememberedAdminUsername) }
    var password by remember { mutableStateOf(viewModel.rememberedAdminPassword) }
    var rememberMe by remember { mutableStateOf(viewModel.rememberAdminEnabled) }

    // States for register
    var signUpUsername by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpFullName by remember { mutableStateOf("") }
    var signUpRole by remember { mutableStateOf("Admin") }

    val authError by viewModel.authError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(SlateBackground)
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = { 
                    viewModel.clearAuthError()
                    viewModel.navigateBack() 
                },
                modifier = Modifier.background(SlateSurface, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSignUpMode) "Créer un Compte Admin" else "Authentification Admin",
            style = MaterialTheme.typography.headlineMedium,
            color = PureWhite,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (isSignUpMode) "Enregistrez un nouvel administrateur système" else "Entrez vos codes d'accès administrateur.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                if (!isSignUpMode) {
                    // LOGIN FORM
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Utilisateur (ex: admin)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = SlateSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_username_field"),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe (ex: admin)", color = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = SlateSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_field"),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remember Me Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rememberMe = !rememberMe }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = EmeraldPrimary,
                                uncheckedColor = TextMuted,
                                checkmarkColor = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Se souvenir de moi", color = TextLight, fontSize = 14.sp)
                    }

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authError ?: "",
                            color = ErrorAbsent,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { 
                            viewModel.saveAdminRemembered(rememberMe, username, password)
                            viewModel.loginAdmin(username, password) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Se Connecter",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { 
                            viewModel.clearAuthError()
                            isSignUpMode = true 
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Pas de compte ? Créer un compte admin",
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                } else {
                    // SIGN UP FORM
                    OutlinedTextField(
                        value = signUpFullName,
                        onValueChange = { signUpFullName = it },
                        label = { Text("Nom complet", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = SlateSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TextMuted) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = signUpUsername,
                        onValueChange = { signUpUsername = it },
                        label = { Text("Nom d'utilisateur (Identifiant)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = SlateSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = signUpPassword,
                        onValueChange = { signUpPassword = it },
                        label = { Text("Mot de passe", color = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = SlateSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Rôle :", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Super Admin", "Admin", "Manager").forEach { r ->
                            FilterChip(
                                selected = signUpRole == r,
                                onClick = { signUpRole = r },
                                label = { Text(r, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = SlateSurfaceVariant,
                                    labelColor = TextLight
                                )
                            )
                        }
                    }

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authError ?: "",
                            color = ErrorAbsent,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.registerAdmin(signUpUsername, signUpPassword, signUpFullName, signUpRole) {
                                username = signUpUsername
                                password = signUpPassword
                                isSignUpMode = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Enregistrer & Se Connecter",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { 
                            viewModel.clearAuthError()
                            isSignUpMode = false 
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Déjà inscrit ? Connexion",
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hint helper box for demo purposes
        Box(
            modifier = Modifier
                .background(SlateSurface, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "💡 Identifiants démo : admin / admin",
                color = EmeraldSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// 3. EMPLOYEE LOGIN SCREEN
@Composable
fun EmployeeLoginScreen(viewModel: StaffFlowViewModel) {
    var matricule by remember { mutableStateOf(viewModel.rememberedEmployeeMatricule) }
    var pin by remember { mutableStateOf(viewModel.rememberedEmployeePin) }
    var rememberMe by remember { mutableStateOf(viewModel.rememberEmployeeEnabled) }
    val authError by viewModel.authError.collectAsState()
    val allEmployeesList by viewModel.allEmployees.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(SlateBackground)
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = { 
                    viewModel.clearAuthError()
                    viewModel.navigateBack() 
                },
                modifier = Modifier.background(SlateSurface, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = EmeraldSecondary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Espace Salariés",
            style = MaterialTheme.typography.headlineMedium,
            color = PureWhite,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Identifiez-vous à l'aide de votre Matricule & code PIN.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                OutlinedTextField(
                    value = matricule,
                    onValueChange = { matricule = it },
                    label = { Text("Matricule Salarié (ex: SF-0103)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = EmeraldSecondary,
                        unfocusedBorderColor = SlateSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_matricule_field"),
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TextMuted) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Code PIN", color = TextMuted) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = EmeraldSecondary,
                        unfocusedBorderColor = SlateSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_pin_field"),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Remember Me
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { rememberMe = !rememberMe }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = EmeraldSecondary,
                            uncheckedColor = TextMuted,
                            checkmarkColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Se souvenir de moi", color = TextLight, fontSize = 14.sp)
                }

                if (authError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = authError ?: "",
                        color = ErrorAbsent,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { 
                        viewModel.saveEmployeeRemembered(rememberMe, matricule, pin)
                        viewModel.loginEmployee(matricule, pin) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("employee_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Accéder à mon Espace",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Interactive Sandbox Fast Connect for quick testing/evaluation
        Text(
            text = "Accès rapide d'évaluation :",
            color = TextLight,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            allEmployeesList.take(3).forEach { emp ->
                Card(
                    modifier = Modifier
                        .clickable {
                            matricule = emp.matricule
                            pin = emp.pin
                        }
                        .width(180.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = emp.fullName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = emp.matricule, color = EmeraldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "PIN: ${emp.pin}", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// 4. ADMIN DASHBOARD SCREEN
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: StaffFlowViewModel) {
    val adminSession by viewModel.adminSession.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf(
        TabItem("Tableau", Icons.Default.Dashboard, Icons.Outlined.Dashboard),
        TabItem("Employés", Icons.Default.People, Icons.Outlined.People),
        TabItem("Scanner QR", Icons.Default.QrCodeScanner, Icons.Outlined.QrCodeScanner),
        TabItem("Paiements", Icons.Default.Payments, Icons.Outlined.Payments),
        TabItem("Rapports", Icons.Default.Analytics, Icons.Outlined.Analytics)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SlateSurface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(tab.title, fontSize = 11.sp, color = if (selectedTab == index) EmeraldPrimary else TextMuted) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (selectedTab == index) EmeraldPrimary else TextMuted
                            )
                        }
                    )
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountTree, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("StaffFlow — Admin", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = "DÉPOSE", tint = ErrorAbsent)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SlateBackground)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SlateBackground)
        ) {
            when (selectedTab) {
                0 -> AdminOverviewTab(viewModel)
                1 -> AdminEmployeesTab(viewModel)
                2 -> AdminQrScannerTab(viewModel)
                3 -> AdminPaymentsTab(viewModel)
                4 -> AdminReportsTab(viewModel)
            }
        }
    }
}

data class TabItem(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

// TAB 1: OVERVIEW (DASHBOARD METRICS)
@Composable
fun AdminOverviewTab(viewModel: StaffFlowViewModel) {
    val employees by viewModel.activeEmployees.collectAsState()
    val attendance by viewModel.allAttendance.collectAsState()
    val payments by viewModel.allPayments.collectAsState()

    val totalPaid = payments.sumOf { it.netPaid }
    val totalHours = attendance.sumOf { it.workedHours }
    val latesTodayCount = attendance.filter { it.isLate }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner and time
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bienvenue, Administrateur",
                            style = MaterialTheme.typography.titleLarge,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aujourd'hui : ${SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault()).format(Date())}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldSecondary
                        )
                    }
                    Icon(
                        Icons.Default.WavingHand,
                        contentDescription = null,
                        tint = WarningLate,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Beautiful Grid Cards Metrics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        title = "Effectifs Actifs",
                        value = "${employees.size}",
                        subtitle = "Employés",
                        icon = Icons.Default.People,
                        color = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Masse Salariale",
                        value = String.format(Locale.getDefault(), "%,.1f Ar", totalPaid),
                        subtitle = "Cumulé",
                        icon = Icons.Default.Payments,
                        color = EmeraldSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        title = "Heures Cumulées",
                        value = String.format(Locale.getDefault(), "%.1f h", totalHours),
                        subtitle = "Total travaillé",
                        icon = Icons.Default.Alarm,
                        color = WarningLate,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Retards Signalés",
                        value = "$latesTodayCount",
                        subtitle = "Absents / Retards",
                        icon = Icons.Default.CancelPresentation,
                        color = ErrorAbsent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Attendance Stats Graphic Visualizer
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Volume d'activité hebdomadaire (Heures)",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Simple custom bar graph rendered using Canvas / standard composable columns
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val days = listOf("Lun" to 32.5, "Mar" to 42.0, "Mer" to 38.5, "Jeu" to 44.2, "Ven" to 39.0, "Sam" to 12.0)
                        days.forEach { (day, hours) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                val percentage = (hours / 50.0).coerceAtMost(1.0).toFloat()
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(percentage)
                                        .fillMaxWidth()
                                        .background(Brush.verticalGradient(listOf(EmeraldSecondary, EmeraldPrimary)), RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = day, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Recent Logs
        item {
            Text(
                text = "Activités Récentes",
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val sortedAttendance = attendance.take(4)
        if (sortedAttendance.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Aucun pointage récent.", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(sortedAttendance) { log ->
                val emp = employees.firstOrNull { it.matricule == log.employeeMatricule }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(if (log.checkOutTime == null) EmeraldPrimary else WarningLate, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (log.checkOutTime == null) Icons.Default.Login else Icons.Default.Logout,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = emp?.fullName ?: log.employeeMatricule,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Date: ${log.dateString} • Enregistrement: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.checkInTime))}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    if (log.checkOutTime == null) EmeraldPrimary.copy(alpha = 0.2f) else SlateSurfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (log.checkOutTime == null) "Entrée" else "Sortie",
                                fontSize = 11.sp,
                                color = if (log.checkOutTime == null) EmeraldSecondary else WarningLate,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Server and database storage explanation card (Offline-first SQLite & Room)
        item {
            val serverIp by viewModel.serverIpAddress.collectAsState()
            val isSyncing by viewModel.isSyncing.collectAsState()
            val syncResult by viewModel.syncResultMessage.collectAsState()

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Synchronisation PC / Serveur Central",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Les données sont stockées de manière autonome et sécurisée dans l'appareil (SQLite & Room). Vous pouvez à tout moment synchroniser vos employés, pointages de présence et fiches de paie avec la base de données centrale du serveur de votre PC connecté au même réseau WiFi local.",
                        color = TextLight,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Input for PC Server URL
                    Text(
                        text = "Adresse IP du serveur de votre PC (ex: http://192.168.1.50:3000)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    OutlinedTextField(
                        value = serverIp,
                        onValueChange = { viewModel.updateServerIpAddress(it) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = PureWhite, fontSize = 14.sp),
                        placeholder = { Text("http://192.168.1.100:3000", color = TextMuted, fontSize = 14.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = SlateSurfaceVariant,
                            focusedContainerColor = SlateBackground,
                            unfocusedContainerColor = SlateBackground.copy(alpha = 0.5f),
                            cursorColor = EmeraldPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Sync status message
                    syncResult?.let { message ->
                        val isSuccess = message.startsWith("Synchronisation")
                        val containerColor = if (isSuccess) EmeraldPrimary.copy(alpha = 0.15f) else WarningLate.copy(alpha = 0.15f)
                        val borderColor = if (isSuccess) EmeraldPrimary else WarningLate
                        val textCol = if (isSuccess) EmeraldSecondary else Color(0xFFFFB3B3)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(containerColor, RoundedCornerShape(10.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = borderColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSuccess) "Statut Synchro" else "Information / Échec",
                                        color = PureWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearSyncResultMessage() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Fermer",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message,
                                    color = textCol,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Launch sync button
                    Button(
                        onClick = { viewModel.triggerMasterSync() },
                        enabled = !isSyncing && serverIp.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = SlateSurface,
                            disabledContainerColor = SlateSurfaceVariant,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = SlateSurface,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Synchronisation en cours...", color = SlateSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = SlateSurface
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Synchroniser Maintenant", color = SlateSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 Tout fonctionne à 100% hors ligne de manière autonome. La connexion réseau sert uniquement aux transferts volontaires de synchro.",
                        color = EmeraldSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EmployeeAvatar(
    photoUrl: String?,
    fullName: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    if (!photoUrl.isNullOrBlank()) {
        coil.compose.AsyncImage(
            model = photoUrl,
            contentDescription = "Photo de $fullName",
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(2.dp, EmeraldPrimary, CircleShape),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(EmeraldPrimary.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fullName.take(2).uppercase(),
                color = EmeraldPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.33f).sp
            )
        }
    }
}

@Composable
fun AdminEmployeesTab(viewModel: StaffFlowViewModel) {
    val employees by viewModel.allEmployees.collectAsState()
    val searchQuery by viewModel.employeeSearchQuery.collectAsState()
    
    var openAddDialog by remember { mutableStateOf(false) }
    var selectedEmployeeForDetail by remember { mutableStateOf<Employee?>(null) }
    var openEditDialog by remember { mutableStateOf(false) }
    var openDeleteConfirmDialog by remember { mutableStateOf(false) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    // Add employee text fields
    var fullName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Technique") }
    var poste by remember { mutableStateOf("") }
    var hourlySalary by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("1234") }
    var photoUrl by remember { mutableStateOf("") }

    // Edit employee text fields
    var editMatricule by remember { mutableStateOf("") }
    var editFullName by remember { mutableStateOf("") }
    var editDepartment by remember { mutableStateOf("Technique") }
    var editPoste by remember { mutableStateOf("") }
    var editHourlySalary by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editPinCode by remember { mutableStateOf("1234") }
    var editPhotoUrl by remember { mutableStateOf("") }
    var editStatus by remember { mutableStateOf("Active") }
    var editPaymentMode by remember { mutableStateOf("Virement Mobile") }

    val departments = listOf("Technique", "Ressources Humaines", "Design", "Marketing", "Administration")

    val presetAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200", // Woman 1
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200", // Man 1
        "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&q=80&w=200", // Woman 2
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=200", // Man 2
        "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&q=80&w=200", // Woman 3
        "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=200"  // Man 3
    )

    val filteredList = employees.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.matricule.contains(searchQuery, ignoreCase = true) ||
                it.department.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateEmployeeSearch(it) },
                label = { Text("Rechercher un salarié / matricule...", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = TextLight,
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = SlateSurfaceVariant
                ),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateEmployeeSearch("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("employee_search_bar")
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Aucun salarié trouvé.", color = TextMuted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { emp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedEmployeeForDetail = emp }
                                .testTag("employee_item_${emp.matricule}"),
                            colors = CardDefaults.cardColors(containerColor = SlateSurface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                EmployeeAvatar(
                                    photoUrl = emp.photoUrl,
                                    fullName = emp.fullName,
                                    size = 48.dp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = emp.fullName, color = PureWhite, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = emp.matricule, color = EmeraldSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = " • ", color = TextMuted, fontSize = 12.sp)
                                        Text(text = emp.poste, color = TextMuted, fontSize = 12.sp)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "${emp.hourlySalary} Ar/h", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (emp.status == "Active") SuccessCheck.copy(alpha = 0.2f) else ErrorAbsent.copy(alpha = 0.2f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = emp.status,
                                            fontSize = 9.sp,
                                            color = if (emp.status == "Active") EmeraldSecondary else ErrorAbsent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Employee FAB
        FloatingActionButton(
            onClick = {
                fullName = ""
                poste = ""
                hourlySalary = ""
                phone = ""
                pinCode = "1234"
                photoUrl = ""
                department = "Technique"
                openAddDialog = true
            },
            containerColor = EmeraldPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_employee_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Employee", tint = Color.Black)
        }

        // Add Employee Dialog Modal
        if (openAddDialog) {
            AlertDialog(
                onDismissRequest = { openAddDialog = false },
                title = { Text("Ajouter un Employé", color = PureWhite, fontWeight = FontWeight.Bold) },
                containerColor = SlateSurface,
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nom complet", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Département :", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            departments.forEach { dept ->
                                FilterChip(
                                    selected = department == dept,
                                    onClick = { department = dept },
                                    label = { Text(dept, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = Color.Black,
                                        containerColor = SlateSurfaceVariant,
                                        labelColor = TextLight
                                    )
                                )
                            }
                        }

                        // Avatar Portrait Selection Row
                        Text("Photo de Profil (Sélectionner un avatar) :", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Empty avatar choice
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (photoUrl.isEmpty()) EmeraldPrimary.copy(alpha = 0.3f) else SlateSurfaceVariant
                                    )
                                    .border(
                                        if (photoUrl.isEmpty()) 2.dp else 1.dp,
                                        if (photoUrl.isEmpty()) EmeraldPrimary else SlateSurfaceVariant,
                                        CircleShape
                                    )
                                    .clickable { photoUrl = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "None", tint = PureWhite)
                            }

                            presetAvatars.forEach { av ->
                                coil.compose.AsyncImage(
                                    model = av,
                                    contentDescription = "Avatar Options",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .border(
                                            if (photoUrl == av) 2.5.dp else 0.dp,
                                            if (photoUrl == av) EmeraldPrimary else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { photoUrl = av },
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }

                        OutlinedTextField(
                            value = photoUrl,
                            onValueChange = { photoUrl = it },
                            label = { Text("Ou coller URL d'image personnalisée", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = poste,
                            onValueChange = { poste = it },
                            label = { Text("Poste / Rôle", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = hourlySalary,
                            onValueChange = { hourlySalary = it },
                            label = { Text("Salaire Horaire (Ar)", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Téléphone", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it },
                            label = { Text("Code PIN (connexion)", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        onClick = {
                            if (fullName.isNotEmpty()) {
                                val salaryDouble = hourlySalary.toDoubleOrNull() ?: 15000.0
                                val randNum = (1000..9999).random()
                                val matriculeNew = "SF-$randNum"
                                val hireDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                
                                val newAgent = Employee(
                                    matricule = matriculeNew,
                                    fullName = fullName,
                                    department = department,
                                    poste = poste.ifEmpty { "Salarié" },
                                    hourlySalary = salaryDouble,
                                    phone = phone.ifEmpty { "+261 34 00 000 00" },
                                    hireDate = hireDate,
                                    status = "Active",
                                    paymentMode = "Virement Mobile",
                                    photoUrl = photoUrl.ifBlank { null },
                                    pin = pinCode
                                )
                                viewModel.addEmployee(newAgent)
                                openAddDialog = false
                            }
                        }
                    ) {
                        Text("Enregistrer", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { openAddDialog = false }) {
                        Text("Annuler", color = TextMuted)
                    }
                }
            )
        }

        // Employee detailed bottom sheet view
        selectedEmployeeForDetail?.let { emp ->
            var showQrCodeInsteadOfPhoto by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { selectedEmployeeForDetail = null },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Détail Salarié", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SlateSurface,
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateSurfaceVariant, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (showQrCodeInsteadOfPhoto) {
                                    // SHOW QR CODE CARD
                                    QrCodeGenerator(
                                        content = emp.qrCodeContent,
                                        modifier = Modifier.size(140.dp)
                                    )
                                } else {
                                    // SHOW BEAUTIFUL PORTRAIT PHOTO BY DEFAULT
                                    EmployeeAvatar(
                                        photoUrl = emp.photoUrl,
                                        fullName = emp.fullName,
                                        size = 140.dp
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(emp.fullName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(emp.matricule, color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Beautiful dynamic toggle icon button
                                Button(
                                    onClick = { showQrCodeInsteadOfPhoto = !showQrCodeInsteadOfPhoto },
                                    colors = ButtonDefaults.buttonColors(containerColor = SlateSurface),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showQrCodeInsteadOfPhoto) Icons.Default.Person else Icons.Default.QrCode,
                                        contentDescription = "Toggle View",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (showQrCodeInsteadOfPhoto) "Voir Photo de Profil" else "Voir Badge QR d'Entrée",
                                        color = EmeraldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        DetailRow("Département :", emp.department)
                        DetailRow("Rôle / Poste :", emp.poste)
                        DetailRow("Salaire horaire :", "${emp.hourlySalary} Ar / heure")
                        DetailRow("Téléphone :", emp.phone)
                        DetailRow("Date d'embauche :", emp.hireDate)
                        DetailRow("Statut :", emp.status)
                        DetailRow("Mode de paiement :", emp.paymentMode)
                        DetailRow("PIN accès :", emp.pin)
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Edit modifier button
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                                onClick = {
                                    editMatricule = emp.matricule
                                    editFullName = emp.fullName
                                    editDepartment = emp.department
                                    editPoste = emp.poste
                                    editHourlySalary = emp.hourlySalary.toString()
                                    editPhone = emp.phone
                                    editPinCode = emp.pin
                                    editPhotoUrl = emp.photoUrl ?: ""
                                    editStatus = emp.status
                                    editPaymentMode = emp.paymentMode
                                    openEditDialog = true
                                    selectedEmployeeForDetail = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Modifier", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            if (emp.status == "Active") {
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant),
                                    onClick = {
                                        viewModel.archiveEmployeeDetails(emp.matricule)
                                        selectedEmployeeForDetail = null
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Archiver", color = PureWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Hard delete button with confirm
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorAbsent),
                                onClick = {
                                    employeeToDelete = emp
                                    openDeleteConfirmDialog = true
                                    selectedEmployeeForDetail = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Supprimer", color = PureWhite, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                onClick = { selectedEmployeeForDetail = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Fermer", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )
        }

        // Edit Employee Modal
        if (openEditDialog) {
            AlertDialog(
                onDismissRequest = { openEditDialog = false },
                title = { Text("Modifier l'Employé ($editMatricule)", color = PureWhite, fontWeight = FontWeight.Bold) },
                containerColor = SlateSurface,
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editFullName,
                            onValueChange = { editFullName = it },
                            label = { Text("Nom complet", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Département :", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            departments.forEach { dept ->
                                FilterChip(
                                    selected = editDepartment == dept,
                                    onClick = { editDepartment = dept },
                                    label = { Text(dept, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = Color.Black,
                                        containerColor = SlateSurfaceVariant,
                                        labelColor = TextLight
                                    )
                                )
                            }
                        }

                        // Avatar Selection Row
                        Text("Photo de Profil (Sélectionner un avatar) :", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (editPhotoUrl.isEmpty()) EmeraldPrimary.copy(alpha = 0.3f) else SlateSurfaceVariant
                                    )
                                    .border(
                                        if (editPhotoUrl.isEmpty()) 2.dp else 1.dp,
                                        if (editPhotoUrl.isEmpty()) EmeraldPrimary else SlateSurfaceVariant,
                                        CircleShape
                                    )
                                    .clickable { editPhotoUrl = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "None", tint = PureWhite)
                            }

                            presetAvatars.forEach { av ->
                                coil.compose.AsyncImage(
                                    model = av,
                                    contentDescription = "Avatar Options",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .border(
                                            if (editPhotoUrl == av) 2.5.dp else 0.dp,
                                            if (editPhotoUrl == av) EmeraldPrimary else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { editPhotoUrl = av },
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }

                        OutlinedTextField(
                            value = editPhotoUrl,
                            onValueChange = { editPhotoUrl = it },
                            label = { Text("Ou coller URL d'image personnalisée", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editPoste,
                            onValueChange = { editPoste = it },
                            label = { Text("Poste / Rôle", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editHourlySalary,
                            onValueChange = { editHourlySalary = it },
                            label = { Text("Salaire Horaire (Ar)", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Téléphone", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editPinCode,
                            onValueChange = { editPinCode = it },
                            label = { Text("Code PIN (connexion)", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Statut du salarié :", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Active", "Archivé").forEach { st ->
                                FilterChip(
                                    selected = editStatus == st,
                                    onClick = { editStatus = st },
                                    label = { Text(st, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (st == "Active") EmeraldPrimary else ErrorAbsent,
                                        selectedLabelColor = if (st == "Active") Color.Black else PureWhite,
                                        containerColor = SlateSurfaceVariant,
                                        labelColor = TextLight
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        onClick = {
                            if (editFullName.isNotEmpty()) {
                                val salaryDouble = editHourlySalary.toDoubleOrNull() ?: 15000.0
                                val originalAgent = employees.firstOrNull { it.matricule == editMatricule }
                                val hireDate = originalAgent?.hireDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val paymentModeFinal = originalAgent?.paymentMode ?: "Virement Mobile"

                                val updatedAgent = Employee(
                                    matricule = editMatricule,
                                    fullName = editFullName,
                                    department = editDepartment,
                                    poste = editPoste.ifEmpty { "Salarié" },
                                    hourlySalary = salaryDouble,
                                    phone = editPhone,
                                    hireDate = hireDate,
                                    status = editStatus,
                                    paymentMode = paymentModeFinal,
                                    photoUrl = editPhotoUrl.ifBlank { null },
                                    pin = editPinCode
                                )
                                viewModel.updateEmployeeDetails(updatedAgent)
                                openEditDialog = false
                            }
                        }
                    ) {
                        Text("Enregistrer modifications", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { openEditDialog = false }) {
                        Text("Annuler", color = TextMuted)
                    }
                }
            )
        }

        // Hard Delete Confirmation Dialog
        if (openDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { openDeleteConfirmDialog = false },
                title = { Text("Confirmer Suppression", color = ErrorAbsent, fontWeight = FontWeight.Bold) },
                text = { Text("Voulez-vous vraiment supprimer définitivement l'employé ${employeeToDelete?.fullName} (${employeeToDelete?.matricule}) de la base de données ? Cette action est irréversible.", color = PureWhite) },
                containerColor = SlateSurface,
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorAbsent),
                        onClick = {
                            employeeToDelete?.let {
                                viewModel.deleteEmployeeDetails(it.matricule)
                            }
                            openDeleteConfirmDialog = false
                            employeeToDelete = null
                        }
                    ) {
                        Text("Oui, Supprimer", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        openDeleteConfirmDialog = false
                        employeeToDelete = null
                    }) {
                        Text("Annuler", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}


// TAB 3: QR ATTENDANCE SCANNER SIMULATOR
@Composable
fun AdminQrScannerTab(viewModel: StaffFlowViewModel) {
    val employees by viewModel.activeEmployees.collectAsState()
    val scanResult by viewModel.scanResultStatus.collectAsState()

    var selectedEmployeeMatriculeForScan by remember { mutableStateOf("") }
    var manualQrInputScanned by remember { mutableStateOf("") }
    var mockLocation by remember { mutableStateOf("Siège Principal") }

    val locations = listOf("Siège Principal", "Bureau Paris", "Terrain Nord")

    // Laser scan bar animation
    val infiniteTransition = rememberInfiniteTransition(label = "Laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserLaser"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Console de Pointage QR",
            style = MaterialTheme.typography.titleLarge,
            color = PureWhite,
            fontWeight = FontWeight.Bold
        )

        // Holographic-style glowing mock scanner viewfinder
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(Color(0xFF070E0B), RoundedCornerShape(24.dp))
                .border(2.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Retro green focus indicators
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Drawing corner bracket markers on mock camera lens overlay
                        val bracketLen = 20.dp.toPx()
                        val thickness = 3.dp.toPx()
                        val color = EmeraldPrimary

                        // Top Left
                        drawLine(color, Offset(0f, 0f), Offset(bracketLen, 0f), thickness)
                        drawLine(color, Offset(0f, 0f), Offset(0f, bracketLen), thickness)

                        // Top Right
                        drawLine(color, Offset(size.width, 0f), Offset(size.width - bracketLen, 0f), thickness)
                        drawLine(color, Offset(size.width, 0f), Offset(size.width, bracketLen), thickness)

                        // Bottom Left
                        drawLine(color, Offset(0f, size.height), Offset(bracketLen, size.height), thickness)
                        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - bracketLen), thickness)

                        // Bottom Right
                        drawLine(color, Offset(size.width, size.height), Offset(size.width - bracketLen, size.height), thickness)
                        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - bracketLen), thickness)
                    }
            ) {
                // Animated red laser sweep scan line representation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.04f)
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            translationY = size.height * laserOffset
                        }
                        .background(
                            Brush.verticalGradient(
                                listOf(ErrorAbsent, ErrorAbsent.copy(alpha = 0.2f))
                            )
                        )
                )

                // Render placeholder animated QR scan focus
                Icon(
                    Icons.Default.QrCode,
                    contentDescription = null,
                    tint = EmeraldPrimary.copy(alpha = 0.4f),
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        // Action selectors
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Simuler l'arrivée d'un Salarié :",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Grid of fast buttons for active employees to instantly trigger pointage calculations!
                if (employees.isEmpty()) {
                    Text(text = "Aucun salarié actif disponible pour pointer.", color = TextMuted, fontSize = 12.sp)
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        employees.forEach { emp ->
                            FilterChip(
                                selected = selectedEmployeeMatriculeForScan == emp.matricule,
                                onClick = { selectedEmployeeMatriculeForScan = emp.matricule },
                                label = { Column {
                                    Text(emp.fullName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(emp.matricule, fontSize = 9.sp, color = textMutedGreen)
                                }},
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = SlateSurfaceVariant,
                                    labelColor = TextLight
                                )
                            )
                        }
                    }
                }

                Text(
                    text = "Lieu d'enregistrement :",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    locations.forEach { loc ->
                        FilterChip(
                            selected = mockLocation == loc,
                            onClick = { mockLocation = loc },
                            label = { Text(loc, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = SlateSurfaceVariant,
                                labelColor = TextLight
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (selectedEmployeeMatriculeForScan.isNotEmpty()) {
                            viewModel.triggerQrScanSimulated(selectedEmployeeMatriculeForScan, mockLocation)
                        }
                    },
                    enabled = selectedEmployeeMatriculeForScan.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, disabledContainerColor = SlateSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("execute_simulated_scan_button")
                ) {
                    Text(
                        text = "Émuler Scan Badge QR",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedEmployeeMatriculeForScan.isNotEmpty()) Color.Black else TextMuted
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = SlateSurfaceVariant)

                Text(
                    text = "Lecteur Physique ou Simulateur de Saisie QR :",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = manualQrInputScanned,
                    onValueChange = { manualQrInputScanned = it },
                    label = { Text("Contenu du Code QR (Format: STAFF_FLOW_QR:SF-XXXX ou code simple)", color = TextMuted) },
                    placeholder = { Text("Ex: STAFF_FLOW_QR:SF-4523", color = TextMuted.copy(alpha = 0.4f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = SlateSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (manualQrInputScanned.isNotEmpty()) {
                                    viewModel.triggerQrScanSimulated(manualQrInputScanned, mockLocation)
                                    manualQrInputScanned = ""
                                }
                            },
                            enabled = manualQrInputScanned.isNotEmpty()
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanne", tint = if (manualQrInputScanned.isNotEmpty()) EmeraldPrimary else TextMuted)
                        }
                    }
                )

                Button(
                    onClick = {
                        if (manualQrInputScanned.isNotEmpty()) {
                            viewModel.triggerQrScanSimulated(manualQrInputScanned, mockLocation)
                            manualQrInputScanned = ""
                        }
                    },
                    enabled = manualQrInputScanned.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, disabledContainerColor = SlateSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Valider Émargement par Code QR",
                        fontWeight = FontWeight.Bold,
                        color = if (manualQrInputScanned.isNotEmpty()) Color.Black else TextMuted
                    )
                }
            }
        }

        // Live Scan Outcomes Overlay Alert Dialogs
        scanResult?.let { res ->
            AlertDialog(
                onDismissRequest = { viewModel.clearScanResult() },
                containerColor = SlateSurface,
                icon = {
                    Icon(
                        imageVector = when (res) {
                            is ScanResult.CheckIn -> Icons.Default.CheckCircle
                            is ScanResult.CheckOut -> Icons.Default.CheckCircleOutline
                            is ScanResult.Error -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when (res) {
                            is ScanResult.CheckIn -> SuccessCheck
                            is ScanResult.CheckOut -> EmeraldSecondary
                            is ScanResult.Error -> ErrorAbsent
                        },
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = when (res) {
                            is ScanResult.CheckIn -> "Entrée Enregistrée !"
                            is ScanResult.CheckOut -> "Sortie Enregistrée !"
                            is ScanResult.Error -> "Pointage Refusé"
                        },
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        when (res) {
                            is ScanResult.CheckIn -> {
                                Text(res.employee.fullName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Matricule : ${res.employee.matricule}", color = EmeraldSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Heure d'arrivée : ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(res.checkInTime))}", color = TextLight, fontSize = 14.sp)
                                Text("Lieu : $mockLocation", color = TextMuted, fontSize = 12.sp)
                                if (res.isLate) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(WarningLate.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("⏰ RETARD (Standard: v8:30)", color = WarningLate, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                            is ScanResult.CheckOut -> {
                                Text(res.employee.fullName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Matricule : ${res.employee.matricule}", color = EmeraldSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Heure de départ : ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(res.checkOutTime))}", color = TextLight, fontSize = 14.sp)
                                Text("Durée du poste : ${res.hours} heures", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (res.attendance.overtimeHours > 0) {
                                    Text("Heures Sup. : +${res.attendance.overtimeHours} h", color = WarningLate, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("Lieu : $mockLocation", color = TextMuted, fontSize = 12.sp)
                            }
                            is ScanResult.Error -> {
                                Text(res.message, color = TextLight, textAlign = TextAlign.Center)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        onClick = { viewModel.clearScanResult() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Terminer", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

val textMutedGreen = Color(0xFFA7F3D0)


// TAB 4: PAYMENTS & PAYROLL SALARY
@Composable
fun AdminPaymentsTab(viewModel: StaffFlowViewModel) {
    val employees by viewModel.activeEmployees.collectAsState()
    val payrollCalculation by viewModel.calculatedPayroll.collectAsState()
    val paymentsLedger by viewModel.allPayments.collectAsState()

    var selectedEmployeeMatricule by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("Mai 2026") }

    val periods = listOf("Mai 2026", "Avril 2026", "Mars 2026")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Gestion de la Paie",
                style = MaterialTheme.typography.titleLarge,
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
        }

        // Calculation Picker Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1. Sélectionner un Salarié :", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        employees.forEach { emp ->
                            FilterChip(
                                selected = selectedEmployeeMatricule == emp.matricule,
                                onClick = { selectedEmployeeMatricule = emp.matricule },
                                label = { Text(emp.fullName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = SlateSurfaceVariant,
                                    labelColor = TextLight
                                )
                            )
                        }
                    }

                    Text("2. Période de paie :", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periods.forEach { per ->
                            FilterChip(
                                selected = selectedPeriod == per,
                                onClick = { selectedPeriod = per },
                                label = { Text(per, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = SlateSurfaceVariant,
                                    labelColor = TextLight
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (selectedEmployeeMatricule.isNotEmpty()) {
                                viewModel.selectEmployeeForPayrollCheck(selectedEmployeeMatricule, selectedPeriod)
                            }
                        },
                        enabled = selectedEmployeeMatricule.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, disabledContainerColor = SlateSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("calculate_payroll_button")
                    ) {
                        Text("Calculer Salaire", fontWeight = FontWeight.Bold, color = if (selectedEmployeeMatricule.isNotEmpty()) Color.Black else TextMuted)
                    }
                }
            }
        }

        // Calculated Payroll Outcome Sheet
        payrollCalculation?.let { calc ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("RÉSULTAT CALCUL DE PAIE", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(calc.period, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Text(calc.employeeName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Matricule: ${calc.employeeMatricule} (Taux horaire: ${calc.hourlyRate} Ar/h)", color = TextMuted, fontSize = 12.sp)

                        Divider(color = SlateSurface, thickness = 1.dp)

                        DetailRow("Jours pointés :", "${calc.daysWorked} jours")
                        DetailRow("Cumul Heures Travaillées :", "${calc.totalHours} h")
                        DetailRow("Dont Heures Majorées (+25%) :", "${calc.overtimeHours} h")

                        Divider(color = SlateSurface, thickness = 1.dp)

                        DetailRow("Salaire de Base Brut :", "${calc.baseSalary} Ar")
                        DetailRow("Majoration Heures Sup :", "+${calc.overtimeAmount} Ar")
                        DetailRow("Prime de Présence (Assiduité) :", "+${calc.bonus} Ar")
                        DetailRow("Déductions de Paie :", "-${calc.deduction} Ar")

                        Divider(color = EmeraldPrimary, thickness = 2.dp)

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NET À PAYER (Virement) :", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${calc.netPaid} Ar", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }

                        Button(
                            onClick = { viewModel.executePayrollPayout(calc) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("disburse_payment_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.SendToMobile, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Valider & Émettre Virement", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }

        // Payments History ledger list
        item {
            Text(
                text = "Registre Historique des Paiements",
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (paymentsLedger.isEmpty()) {
            item {
                Box(modifier = Modifier.padding(24.dp)) {
                    Text("Aucun paiement émis.", color = TextMuted)
                }
            }
        } else {
            items(paymentsLedger) { pay ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = pay.employeeMatricule, color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = pay.periodString, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(text = "Réf Virement: ${pay.transactionId}", color = TextMuted, fontSize = 11.sp)
                                Text(
                                    text = "Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(pay.paymentDate))}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(text = String.format(Locale.getDefault(), "%,.1f Ar", pay.netPaid), color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        
                        Divider(color = SlateSurfaceVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "🔐 Signature numérique : " + pay.digitalSignature,
                            color = WarningLate,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}


// TAB 5: REPORTS & DIGESTS (PRINT READY METRICS)
@Composable
fun AdminReportsTab(viewModel: StaffFlowViewModel) {
    val employees by viewModel.activeEmployees.collectAsState()
    val attendance by viewModel.allAttendance.collectAsState()
    val payments by viewModel.allPayments.collectAsState()

    var showPrintReceiptData by remember { mutableStateOf<Payment?>(null) }
    var showOverallCompanyReportSummary by remember { mutableStateOf(false) }

    val totalSalaryDisbursed = payments.sumOf { it.netPaid }
    val totalHoursWorked = attendance.sumOf { it.workedHours }
    val avgWage = if (employees.isEmpty()) 0.0 else employees.sumOf { it.hourlySalary } / employees.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Rapports d'Administration",
                style = MaterialTheme.typography.titleLarge,
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Générer les synthèses d'entreprise :", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    Button(
                        onClick = { showOverallCompanyReportSummary = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Imprimer Bilan Mensuel de Présence & Paie", fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Text("Imprimer Fiches de Paie Salariés :", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    if (payments.isEmpty()) {
                        Text("Aucune fiche de paie émise dans le registre.", color = TextMuted, fontSize = 11.sp)
                    } else {
                        payments.forEach { pay ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPrintReceiptData = pay },
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${pay.employeeMatricule} - ${pay.periodString}", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Net: ${pay.netPaid} Ar", color = EmeraldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = EmeraldPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Print preview dialogue for Overall Corporate Report
        if (showOverallCompanyReportSummary) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),// Mimic paper!
                    border = BorderStroke(2.dp, Color.Black)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Corporate Header
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("STAFFFLOW - RAPPORT FINANCIER GLOBAL", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
                            Text("Système Intelligent de Gestion d'Activité & Pointage QR", color = Color.Gray, fontSize = 10.sp)
                            Text("Émis le: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}", color = Color.Gray, fontSize = 9.sp)
                            Divider(color = Color.Black, thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))
                        }

                        // Statistics grid block
                        Text("1. STATISTIQUES DES EFFECTIFS ET RENDEMENT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Nombre d'employés actifs :", color = Color.DarkGray, fontSize = 11.sp)
                            Text("${employees.size}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cumul total temps d'activité :", color = Color.DarkGray, fontSize = 11.sp)
                            Text(String.format(Locale.getDefault(), "%,.2f heures", totalHoursWorked), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taux horaire moyen :", color = Color.DarkGray, fontSize = 11.sp)
                            Text(String.format(Locale.getDefault(), "%,.1f Ar/h", avgWage), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Financial ledger
                        Text("2. COMPTABILITÉ DES PAYEMENTS ÉMIS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Masse salariale totale décaissée :", color = Color.DarkGray, fontSize = 11.sp)
                            Text(String.format(Locale.getDefault(), "%,.1f Ar", totalSalaryDisbursed), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Nombre total virements effectués :", color = Color.DarkGray, fontSize = 11.sp)
                            Text("${payments.size}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                        // Signature digital stamps
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Le Directeur des Operations", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(30.dp))
                                Text("Sceau Officiel StaffFlow", color = Color.Gray, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Validation Numérique", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(30.dp))
                                Text("APPROUVÉ SECURE SHA-256", color = Color(0xFF047857), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            }
                        }

                        Button(
                            onClick = { showOverallCompanyReportSummary = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text("Fermer l'Aperçu Papier", color = Color.White)
                        }
                    }
                }
            }
        }

        // Print preview dialog for Specific individual Payslip receipt
        showPrintReceiptData?.let { pay ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(2.dp, Color.Black)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("FICHE DE PAIE OFFICIELLE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp)
                            Text("StaffFlow — Cabinet d'Administration", color = Color.Gray, fontSize = 10.sp)
                            Divider(color = Color.Black, thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))
                        }

                        DetailInvoiceRow("Identifiant Salarié :", pay.employeeMatricule)
                        DetailInvoiceRow("Période comptable :", pay.periodString)
                        DetailInvoiceRow("Date d'émission :", SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(pay.paymentDate)))
                        DetailInvoiceRow("Référence Virement :", pay.transactionId)

                        Divider(color = Color.LightGray, thickness = 1.dp)

                        DetailInvoiceRow("Salaire de Base Brut :", "${pay.baseSalaryPaid} Ar")
                        DetailInvoiceRow("Majoration Heures Sup :", "+${pay.overtimeAmountPaid} Ar")
                        DetailInvoiceRow("Bonuses d'Assiduité :", "+${pay.bonusPaid} Ar")
                        DetailInvoiceRow("Déductions de Paie :", "-${pay.deductionPaid} Ar")

                        Divider(color = Color.Black, thickness = 1.5f.dp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("NET VIREMENT COMMANDE :", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${pay.netPaid} Ar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Divider(color = Color.LightGray, thickness = 1.dp)

                        Text("Registre Validation :", color = Color.Gray, fontSize = 9.sp)
                        Text(pay.digitalSignature, color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)

                        Button(
                            onClick = { showPrintReceiptData = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text("Fermer la Fiche", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailInvoiceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.DarkGray, fontSize = 11.sp)
        Text(text = value, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}


// 5. EMPLOYEE DASHBOARD SPACE SCREEN
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboardScreen(viewModel: StaffFlowViewModel) {
    val employeeSession by viewModel.employeeSession.collectAsState()
    val allAttendanceList by viewModel.allAttendance.collectAsState()
    val allPaymentsList by viewModel.allPayments.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    val emp = employeeSession ?: return

    // Filter personal records
    val myAttendance = allAttendanceList.filter { it.employeeMatricule == emp.matricule }
    val myPayments = allPaymentsList.filter { it.employeeMatricule == emp.matricule }

    val myWorkedHours = myAttendance.sumOf { it.workedHours }
    val myTotalEarnings = myPayments.sumOf { it.netPaid }

    val tabs = listOf(
        TabItem("Mon QR Badge", Icons.Default.QrCode, Icons.Outlined.QrCode),
        TabItem("Mes Pointages", Icons.Default.CalendarToday, Icons.Outlined.CalendarToday),
        TabItem("Mes Fiches", Icons.Default.Receipt, Icons.Outlined.Receipt)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SlateSurface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(tab.title, fontSize = 11.sp, color = if (selectedTab == index) EmeraldSecondary else TextMuted) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (selectedTab == index) EmeraldSecondary else TextMuted
                            )
                        }
                    )
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = EmeraldSecondary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(emp.fullName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "DÉPRISE", tint = ErrorAbsent)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SlateBackground)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SlateBackground)
        ) {
            when (selectedTab) {
                0 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Header card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            EmployeeAvatar(
                                photoUrl = emp.photoUrl,
                                fullName = emp.fullName,
                                size = 64.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(emp.fullName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(emp.poste, color = EmeraldSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Département: ${emp.department}", color = TextMuted, fontSize = 12.sp)
                        }
                    }

                    // QR Card Badge - To show to scanner!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("BADGE STAFFFLOW", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            QrCodeGenerator(
                                content = emp.qrCodeContent,
                                modifier = Modifier.size(180.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(emp.matricule, color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text("Présentez ce QR code à l'arrivée / départ.", color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center)
                        }
                    }

                    // Self checkout clock emulation for remote workers!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Simulateur auto-enregistrement (Télétravail) :", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Pour tester instantanément le pointage sans scanner admin.", color = TextMuted, fontSize = 11.sp)
                            Button(
                                onClick = { viewModel.triggerQrScanSimulated(emp.matricule, "Télétravail Distant") },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pointer Entrée/Sortie auto", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Stats indicators
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SlateSurface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Jours pointés", color = TextMuted, fontSize = 11.sp)
                                Text("${myAttendance.size} j", color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SlateSurface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Heures totales", color = TextMuted, fontSize = 11.sp)
                                Text(String.format(Locale.getDefault(), "%.1f h", myWorkedHours), color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SlateSurface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Gains cumulés", color = TextMuted, fontSize = 11.sp)
                                Text(String.format(Locale.getDefault(), "%,.1f Ar", myTotalEarnings), color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                1 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Historique de mes Présences", color = PureWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    
                    if (myAttendance.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Aucun pointage enregistré.", color = TextMuted)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(myAttendance) { log ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(log.dateString, color = PureWhite, fontWeight = FontWeight.Bold)
                                            Text("Arrivée: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.checkInTime))} • Lieu: ${log.checkInLocation}", color = TextMuted, fontSize = 11.sp)
                                            if (log.checkOutTime != null) {
                                                Text("Départ: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.checkOutTime))}", color = TextMuted, fontSize = 11.sp)
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${log.workedHours} h", color = EmeraldSecondary, fontWeight = FontWeight.Bold)
                                            if (log.isLate) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(WarningLate.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Retard", color = WarningLate, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Historique de mes Fiches de Paie", color = PureWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

                    if (myPayments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Aucun paiement n'a encore été émis.", color = TextMuted)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(myPayments) { pay ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(pay.periodString, color = PureWhite, fontWeight = FontWeight.Bold)
                                            Text("${pay.netPaid} Ar", color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                        Text("Date de Versement : ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(pay.paymentDate))}", color = TextMuted, fontSize = 11.sp)
                                        Text("Référence : ${pay.transactionId}", color = TextMuted, fontSize = 11.sp)
                                        Divider(color = SlateSurfaceVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                                        Text("🔐 Clé d'archivage sécurisée :", color = TextMuted, fontSize = 9.sp)
                                        Text(pay.digitalSignature, color = WarningLate, fontSize = 8.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
