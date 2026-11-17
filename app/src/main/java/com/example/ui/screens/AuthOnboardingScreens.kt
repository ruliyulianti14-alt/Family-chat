package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.local.LocalRoomFamilyGateway
import com.example.data.repository.AuthManager
import com.example.ui.components.ThemePickerDialog
import com.example.ui.theme.AppThemeOption
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.WarmOrangeContainer
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(550))
        alpha.animateTo(1f, animationSpec = tween(450))
        delay(950)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SkyBluePrimary, SkyBlueDark, Color(0xFF0A4D82))
                )
            )
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(32.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                shadowElevation = 12.dp,
                modifier = Modifier.size(116.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_family_logo),
                    contentDescription = "Logo My Family komunikasi",
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = "My Family komunikasi",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = WarmOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "One Family, One Connection, One Love.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator(
                color = WarmOrange,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun LoginScreen(
    onEmailPasswordLogin: (email: String, password: String) -> Unit,
    onNavigateToRegister: (prefillEmail: String) -> Unit,
    onEnterDemoFamily: () -> Unit,
    onAuthSuccess: () -> Unit,
    loginErrorMessage: String? = null,
    isPreparatoryBusy: Boolean = false,
    currentTheme: AppThemeOption = AppThemeOption.SKY_WARM,
    onSelectTheme: (AppThemeOption) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val credentialManager = remember { CredentialManager.create(context) }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var googleErrorMessage by remember { mutableStateOf<String?>(null) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val anyBusy = isGoogleLoading || isPreparatoryBusy
    val displayedError = localValidationError ?: loginErrorMessage ?: googleErrorMessage

    fun submitEmailLogin() {
        focusManager.clearFocus()
        val trimmedEmail = email.trim()
        when {
            trimmedEmail.isEmpty() || !trimmedEmail.contains("@") -> {
                localValidationError = "Masukkan alamat email yang valid."
            }
            password.isEmpty() -> {
                localValidationError = "Kata sandi tidak boleh kosong."
            }
            else -> {
                localValidationError = null
                googleErrorMessage = null
                onEmailPasswordLogin(trimmedEmail, password)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    )
                )
            )
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Box {
                        Image(
                            painter = painterResource(id = R.drawable.img_family_banner),
                            contentDescription = "Ilustrasi keluarga bahagia",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xAA0C3559))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "My Family komunikasi",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "“One Family, One Connection, One Love.”",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFFFD8BA)
                            )
                        }
                        IconButton(
                            onClick = { showThemeDialog = true },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .background(Color.Black.copy(alpha = 0.35f), shape = RoundedCornerShape(50))
                                .testTag("login_theme_button")
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = "Ganti Tema", tint = Color.White)
                        }
                    }

                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Masuk ke Akun Keluarga",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Gunakan email & kata sandi Anda",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = SkyBlueContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Lokal SQLite",
                                    color = SkyBlueDark,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                localValidationError = null
                            },
                            label = { Text("Email") },
                            placeholder = { Text("contoh: ayah@keluargaharmoni.id") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = SkyBluePrimary)
                            },
                            singleLine = true,
                            isError = displayedError != null && email.trim().isEmpty(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                localValidationError = null
                            },
                            label = { Text("Kata Sandi") },
                            placeholder = { Text("Masukkan kata sandi") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = SkyBluePrimary)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.testTag("toggle_password_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Sembunyikan kata sandi" else "Tampilkan kata sandi"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            isError = displayedError != null && password.isEmpty(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { submitEmailLogin() }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        if (displayedError != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = displayedError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { submitEmailLogin() },
                            enabled = !anyBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("login_submit_button")
                        ) {
                            if (isPreparatoryBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Memverifikasi...")
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Masuk", style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                localValidationError = null
                                googleErrorMessage = null
                                onNavigateToRegister(email.trim())
                            },
                            enabled = !anyBusy,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_register_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = WarmOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Daftar / Registrasi Akun Baru", color = WarmOrange)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                        Surface(
                            color = WarmOrangeContainer.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Uji Coba Cepat Keluarga Contoh",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF7C2D12)
                                )
                                Text(
                                    text = "Demo: ${LocalRoomFamilyGateway.DEMO_ADMIN_EMAIL} • Sandi: ${LocalRoomFamilyGateway.DEMO_DEFAULT_PASSWORD}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF7C2D12)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AssistChip(
                                        onClick = {
                                            email = LocalRoomFamilyGateway.DEMO_ADMIN_EMAIL
                                            password = LocalRoomFamilyGateway.DEMO_DEFAULT_PASSWORD
                                            localValidationError = null
                                        },
                                        label = { Text("Isi Akun Demo") },
                                        modifier = Modifier.testTag("fill_demo_credentials_chip")
                                    )
                                    Button(
                                        onClick = {
                                            localValidationError = null
                                            onEnterDemoFamily()
                                        },
                                        enabled = !anyBusy,
                                        colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("local_demo_login_button")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Masuk Demo")
                                    }
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                localValidationError = null
                                googleErrorMessage = null
                                isGoogleLoading = true
                                AuthManager.onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isGoogleLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { msg ->
                                        isGoogleLoading = false
                                        googleErrorMessage = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = { isGoogleLoading = false }
                                )
                            },
                            enabled = !anyBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_sign_in_button")
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Menghubungkan ke Google Cloud...")
                            } else {
                                Icon(Icons.Default.CloudSync, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sinkronisasi Cloud dengan Google (Opsional)")
                            }
                        }

                        TextButton(
                            onClick = { showRecoveryDialog = true },
                            modifier = Modifier.testTag("account_recovery_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Info Akun & Pemulihan")
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Login Email/Sandi Lokal (SQLite) • Cloud Google Opsional",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            title = { Text("Info Login & Pemulihan Akun") },
            text = {
                Text(
                    "1. Login Email & Kata Sandi Lokal:\n" +
                        "• Daftar akun baru lewat tombol \"Daftar / Registrasi Akun Baru\" atau gunakan akun demo (${LocalRoomFamilyGateway.DEMO_ADMIN_EMAIL} / ${LocalRoomFamilyGateway.DEMO_DEFAULT_PASSWORD}).\n" +
                        "• Data tersimpan aman di Room SQLite pada perangkat ini (kata sandi di-hash SHA-256).\n\n" +
                        "2. Gabung Keluarga Contoh:\n" +
                        "• Gunakan kode undangan KELHARMONI saat mendaftar untuk langsung bergabung ke Keluarga Harmoni.\n\n" +
                        "3. Sinkronisasi Cloud (Opsional):\n" +
                        "• Gunakan tombol \"Sinkronisasi Cloud dengan Google\" bila ingin berbagi ruang keluarga lintas perangkat."
                )
            },
            confirmButton = {
                TextButton(onClick = { showRecoveryDialog = false }) {
                    Text("Mengerti")
                }
            }
        )
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            currentTheme = currentTheme,
            onSelectTheme = onSelectTheme,
            onDismiss = { showThemeDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistrationScreen(
    userEmail: String,
    isBusy: Boolean,
    statusMessage: String?,
    requireCredentials: Boolean = false,
    initialEmail: String = "",
    onCreateFamily: (familyName: String, displayName: String, relationship: String, phone: String, showPhone: Boolean, birthday: String) -> Unit = { _, _, _, _, _, _ -> },
    onJoinFamily: (inviteCode: String, displayName: String, relationship: String, phone: String, showPhone: Boolean, birthday: String) -> Unit = { _, _, _, _, _, _ -> },
    onRegisterAccountAndSetupFamily: (
        email: String,
        password: String,
        isCreateFamily: Boolean,
        familyNameOrInviteCode: String,
        displayName: String,
        relationship: String,
        phone: String,
        showPhone: Boolean,
        birthday: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onSignOut: () -> Unit
) {
    BackHandler(enabled = requireCredentials) {
        onSignOut()
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var regEmail by rememberSaveable { mutableStateOf(initialEmail) }
    var regPassword by rememberSaveable { mutableStateOf("") }
    var regPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var familyName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Ayah") }
    var phone by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var showPhone by remember { mutableStateOf(true) }

    val relationOptions = listOf("Kakek", "Nenek", "Ayah", "Ibu", "Paman", "Bibi", "Anak", "Cucu", "Saudara")
    val credentialsValid = !requireCredentials || (regEmail.trim().contains("@") && regPassword.length >= 6)
    val familyTargetValid = if (selectedTab == 0) familyName.isNotBlank() else inviteCode.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Registrasi Akun & Keluarga", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (requireCredentials) {
                            "Buat akun lokal baru tanpa perlu akun Google"
                        } else {
                            userEmail.ifBlank { "Mode Uji Coba Lokal (Tanpa Google)" }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onSignOut) {
                    Text("Kembali")
                }
            }

            if (requireCredentials) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Kredensial Akun", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email") },
                            placeholder = { Text("nama@email.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_email_input")
                        )
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Kata Sandi (minimal 6 karakter)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        imageVector = if (regPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (regPasswordVisible) "Sembunyikan kata sandi" else "Tampilkan kata sandi"
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_password_input")
                        )
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Buat Keluarga Baru") },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                    modifier = Modifier.testTag("tab_create_family")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Gabung Kode") },
                    icon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
                    modifier = Modifier.testTag("tab_join_family")
                )
            }

            Surface(
                color = if (selectedTab == 0) SkyBlueContainer else WarmOrangeContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.FamilyRestroom else Icons.Default.GroupAdd,
                        contentDescription = null,
                        tint = if (selectedTab == 0) SkyBlueDark else WarmOrange
                    )
                    Text(
                        text = if (selectedTab == 0) {
                            "Anda akan menjadi Admin Keluarga dan dapat membuat grup serta mengundang anggota lain."
                        } else {
                            "Masukkan kode undangan keluarga (contoh demo: KELHARMONI). Anda akan bergabung sebagai Member."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (selectedTab == 0) {
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("Nama Keluarga (mis. Keluarga Harmoni)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_family_name_input")
                )
            } else {
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it.uppercase() },
                    label = { Text("Kode Undangan (mis. KELHARMONI)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_invite_code_input")
                )
            }

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Nama Panggilan Anda") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_display_name_input")
            )

            Text("Hubungan dalam Keluarga", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                relationOptions.forEach { rel ->
                    FilterChip(
                        selected = relationship == rel,
                        onClick = { relationship = rel },
                        label = { Text(rel) }
                    )
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor Telepon (Opsional)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = birthday,
                onValueChange = { birthday = it },
                label = { Text("Tanggal Lahir (mis. 17 Agu 1980)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tampilkan Nomor ke Keluarga", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Kontrol privasi kontak di daftar anggota",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = showPhone, onCheckedChange = { showPhone = it })
            }

            if (!statusMessage.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    if (requireCredentials) {
                        val targetValue = if (selectedTab == 0) familyName.trim() else inviteCode.trim()
                        onRegisterAccountAndSetupFamily(
                            regEmail.trim(),
                            regPassword,
                            selectedTab == 0,
                            targetValue,
                            displayName.trim(),
                            relationship,
                            phone.trim(),
                            showPhone,
                            birthday.trim()
                        )
                    } else if (selectedTab == 0) {
                        onCreateFamily(familyName, displayName, relationship, phone, showPhone, birthday)
                    } else {
                        onJoinFamily(inviteCode, displayName, relationship, phone, showPhone, birthday)
                    }
                },
                enabled = !isBusy && displayName.isNotBlank() && familyTargetValid && credentialsValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 0) SkyBluePrimary else WarmOrange
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("reg_submit_button")
            ) {
                if (isBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (selectedTab == 0) "Daftar & Buat Keluarga" else "Daftar & Gabung Keluarga",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
