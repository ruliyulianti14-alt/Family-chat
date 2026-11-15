package com.example.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.local.FamilyLocalDatabase
import com.example.data.local.LocalRoomFamilyGateway
import com.example.data.repository.AuthManager
import com.example.data.repository.FamilyCommsRepository
import com.example.data.repository.FamilyCoreRepository
import com.example.data.repository.FamilyDataGateway
import com.example.data.repository.FamilyLifeRepository
import com.example.data.repository.FirestoreFamilyGateway
import com.example.ui.components.FamilyDetailTopBar
import com.example.ui.screens.ActiveRoomTarget
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AppDestination
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CallsScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.ChatRoomScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FamilyGpsScreen
import com.example.ui.screens.FamilyTreeScreen
import com.example.ui.screens.FundScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MembersScreen
import com.example.ui.screens.MomentsScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SafetyScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.AppThemeOption
import com.example.ui.viewmodel.FamilyViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

private data class NavTabItem(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

private val PrimaryTabs = listOf(
    NavTabItem(AppDestination.DASHBOARD, "Beranda", Icons.Default.Home, "nav_dashboard"),
    NavTabItem(AppDestination.CHAT_LIST, "Chat", Icons.AutoMirrored.Filled.Chat, "nav_chat"),
    NavTabItem(AppDestination.MOMENTS, "Momen", Icons.Default.AutoAwesome, "nav_moments"),
    NavTabItem(AppDestination.SAFETY, "Aman", Icons.Default.Shield, "nav_safety"),
    NavTabItem(AppDestination.PROFILE, "Profil", Icons.Default.Person, "nav_profile")
)

@Composable
fun MyFamilyChatApp(
    currentTheme: AppThemeOption = AppThemeOption.SKY_WARM,
    onSelectTheme: (AppThemeOption) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val currentUser by AuthManager.authStateFlow().collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    var splashDone by rememberSaveable { mutableStateOf(false) }
    var localTrialUid by rememberSaveable { mutableStateOf<String?>(null) }
    var localTrialEmail by rememberSaveable { mutableStateOf("") }
    var isOpeningRegistration by rememberSaveable { mutableStateOf(false) }
    var prefillRegEmail by rememberSaveable { mutableStateOf("") }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var regStatusMessage by remember { mutableStateOf<String?>(null) }
    var isPreparingLocal by remember { mutableStateOf(false) }

    if (!splashDone) {
        SplashScreen(onFinished = { splashDone = true })
        return
    }

    val user = currentUser
    if (user == null && localTrialUid == null) {
        if (isOpeningRegistration) {
            RegistrationScreen(
                userEmail = "",
                isBusy = isPreparingLocal,
                statusMessage = regStatusMessage,
                requireCredentials = true,
                initialEmail = prefillRegEmail,
                onRegisterAccountAndSetupFamily = { email, password, isCreateFamily, targetVal, dispName, rel, phone, showPhone, birthday ->
                    scope.launch {
                        isPreparingLocal = true
                        regStatusMessage = null
                        val dao = FamilyLocalDatabase.getInstance(context.applicationContext).familyDao()
                        val newUid = "local_user_${UUID.randomUUID().toString().replace("-", "").take(8)}"
                        val regResult = LocalRoomFamilyGateway.registerLocalAccount(dao, email, password, newUid)
                        if (regResult.isFailure) {
                            regStatusMessage = regResult.exceptionOrNull()?.localizedMessage ?: "Gagal mendaftarkan akun."
                            isPreparingLocal = false
                            return@launch
                        }
                        val normalizedEmail = regResult.getOrThrow().email
                        val gateway = LocalRoomFamilyGateway(dao, newUid)
                        gateway.ensureDemoSeeded()
                        val setupResult = if (isCreateFamily) {
                            gateway.createFamilyAndAdminProfile(
                                familyName = targetVal,
                                displayName = dispName,
                                relationship = rel,
                                phone = phone,
                                showPhone = showPhone,
                                birthday = birthday,
                                email = normalizedEmail
                            )
                        } else {
                            gateway.joinFamilyWithInviteCode(
                                inviteCodeInput = targetVal,
                                displayName = dispName,
                                relationship = rel,
                                phone = phone,
                                showPhone = showPhone,
                                birthday = birthday,
                                email = normalizedEmail
                            )
                        }
                        if (setupResult.isFailure) {
                            dao.deleteAccountByEmail(normalizedEmail)
                            regStatusMessage = setupResult.exceptionOrNull()?.localizedMessage ?: "Gagal menyiapkan keluarga."
                            isPreparingLocal = false
                            return@launch
                        }
                        isOpeningRegistration = false
                        localTrialEmail = normalizedEmail
                        localTrialUid = newUid
                        isPreparingLocal = false
                    }
                },
                onSignOut = {
                    regStatusMessage = null
                    isOpeningRegistration = false
                }
            )
        } else {
            LoginScreen(
                loginErrorMessage = authErrorMessage,
                isPreparatoryBusy = isPreparingLocal,
                onEmailPasswordLogin = { email, password ->
                    scope.launch {
                        isPreparingLocal = true
                        authErrorMessage = null
                        val dao = FamilyLocalDatabase.getInstance(context.applicationContext).familyDao()
                        val authResult = LocalRoomFamilyGateway.authenticateLocalAccount(dao, email, password)
                        authResult.onSuccess { account ->
                            localTrialEmail = account.email
                            localTrialUid = account.userId
                        }.onFailure { err ->
                            authErrorMessage = err.localizedMessage ?: "Email atau kata sandi salah."
                        }
                        isPreparingLocal = false
                    }
                },
                onNavigateToRegister = { typedEmail ->
                    authErrorMessage = null
                    regStatusMessage = null
                    prefillRegEmail = typedEmail
                    isOpeningRegistration = true
                },
                onEnterDemoFamily = {
                    scope.launch {
                        isPreparingLocal = true
                        authErrorMessage = null
                        val dao = FamilyLocalDatabase.getInstance(context.applicationContext).familyDao()
                        LocalRoomFamilyGateway(dao, LocalRoomFamilyGateway.DEMO_ADMIN_UID).ensureDemoSeeded()
                        localTrialEmail = LocalRoomFamilyGateway.DEMO_ADMIN_EMAIL
                        localTrialUid = LocalRoomFamilyGateway.DEMO_ADMIN_UID
                        isPreparingLocal = false
                    }
                },
                currentTheme = currentTheme,
                onSelectTheme = onSelectTheme,
                onAuthSuccess = { }
            )
        }
        return
    }

    val isLocalTrialMode = user == null
    val activeUid = user?.uid ?: localTrialUid!!
    val activeEmail = user?.email ?: localTrialEmail.ifBlank { "Mode Uji Coba Lokal (SQLite)" }
    val sessionKey = if (user != null) "cloud_${user.uid}" else "local_$activeUid"

    androidx.compose.runtime.key(sessionKey) {
        val appContext = context.applicationContext
        val familyViewModel: FamilyViewModel = viewModel(
            key = sessionKey,
            factory = viewModelFactory {
                initializer {
                    val gateway: FamilyDataGateway = if (user != null) {
                        val dbId = appContext.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(dbId)
                        val auth = Firebase.auth
                        FirestoreFamilyGateway(
                            coreRepo = FamilyCoreRepository(db, auth),
                            commsRepo = FamilyCommsRepository(db, auth),
                            lifeRepo = FamilyLifeRepository(db, auth)
                        )
                    } else {
                        val dao = FamilyLocalDatabase.getInstance(appContext).familyDao()
                        LocalRoomFamilyGateway(dao = dao, activeUid = activeUid)
                    }
                    FamilyViewModel(
                        gateway = gateway,
                        currentUserId = activeUid,
                        currentUserEmail = activeEmail
                    )
                }
            }
        )

        AuthenticatedFamilyRoot(
            viewModel = familyViewModel,
            userEmail = activeEmail,
            isLocalTrialMode = isLocalTrialMode,
            activeLocalUid = activeUid,
            currentTheme = currentTheme,
            onSelectTheme = onSelectTheme,
            onSwitchLocalUser = { newUid ->
                localTrialUid = newUid
                localTrialEmail = when (newUid) {
                    LocalRoomFamilyGateway.DEMO_ADMIN_UID -> LocalRoomFamilyGateway.DEMO_ADMIN_EMAIL
                    LocalRoomFamilyGateway.DEMO_TREASURER_UID -> LocalRoomFamilyGateway.DEMO_TREASURER_EMAIL
                    LocalRoomFamilyGateway.DEMO_MEMBER_UID -> LocalRoomFamilyGateway.DEMO_MEMBER_EMAIL
                    else -> localTrialEmail
                }
            },
            onSignOut = {
                if (isLocalTrialMode) {
                    localTrialUid = null
                    localTrialEmail = ""
                    authErrorMessage = null
                } else {
                    AuthManager.signOut(
                        credentialManager = credentialManager,
                        scope = scope
                    )
                }
            }
        )
    }
}

@Composable
private fun AuthenticatedFamilyRoot(
    viewModel: FamilyViewModel,
    userEmail: String,
    isLocalTrialMode: Boolean,
    activeLocalUid: String,
    currentTheme: AppThemeOption,
    onSelectTheme: (AppThemeOption) -> Unit,
    onSwitchLocalUser: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val actionStatus by viewModel.actionStatus.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    when (val state = userProfileState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Menghubungkan ruang keluarga...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Gagal Memuat Data Keluarga",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(onClick = onSignOut) {
                        Text("Keluar / Ganti Akun")
                    }
                }
            }
        }
        is UiState.Success -> {
            val profile = state.data
            if (profile == null || profile.familyId.isBlank()) {
                RegistrationScreen(
                    userEmail = userEmail,
                    isBusy = isBusy,
                    statusMessage = actionStatus,
                    onCreateFamily = { famName, dispName, rel, phone, showPhone, birthday ->
                        viewModel.clearStatus()
                        viewModel.createFamily(famName, dispName, rel, phone, showPhone, birthday)
                    },
                    onJoinFamily = { code, dispName, rel, phone, showPhone, birthday ->
                        viewModel.clearStatus()
                        viewModel.joinFamily(code, dispName, rel, phone, showPhone, birthday)
                    },
                    onSignOut = onSignOut
                )
            } else {
                FamilyMainHub(
                    viewModel = viewModel,
                    userEmail = userEmail,
                    isLocalTrialMode = isLocalTrialMode,
                    activeLocalUid = activeLocalUid,
                    currentTheme = currentTheme,
                    onSelectTheme = onSelectTheme,
                    onSwitchLocalUser = onSwitchLocalUser,
                    onSignOut = onSignOut
                )
            }
        }
    }
}

@Composable
private fun FamilyMainHub(
    viewModel: FamilyViewModel,
    userEmail: String,
    isLocalTrialMode: Boolean,
    activeLocalUid: String,
    currentTheme: AppThemeOption,
    onSelectTheme: (AppThemeOption) -> Unit,
    onSwitchLocalUser: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val family by viewModel.familyState.collectAsStateWithLifecycle()
    val members by viewModel.membersState.collectAsStateWithLifecycle()
    val groups by viewModel.groupsState.collectAsStateWithLifecycle()
    val privateChats by viewModel.privateChatsState.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessagesState.collectAsStateWithLifecycle()
    val moments by viewModel.momentsState.collectAsStateWithLifecycle()
    val momentComments by viewModel.momentCommentsState.collectAsStateWithLifecycle()
    val calls by viewModel.callsState.collectAsStateWithLifecycle()
    val treeNodes by viewModel.treeNodesState.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.calendarEventsState.collectAsStateWithLifecycle()
    val galleryAlbums by viewModel.galleryAlbumsState.collectAsStateWithLifecycle()
    val fundTransactions by viewModel.fundTransactionsState.collectAsStateWithLifecycle()
    val safetyAlerts by viewModel.safetyAlertsState.collectAsStateWithLifecycle()
    val actionStatus by viewModel.actionStatus.collectAsStateWithLifecycle()

    var activeRoom by remember { mutableStateOf<ActiveRoomTarget?>(null) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(family?.memberUids, members.size) {
        viewModel.syncAccessIfAdmin()
    }

    LaunchedEffect(actionStatus) {
        val msg = actionStatus
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatus()
        }
    }

    val currentMember = members.firstOrNull { it.userId == viewModel.currentUserId }
    val isAdmin = currentMember?.isAdmin == true || family?.adminUid == viewModel.currentUserId
    val canManageFund = isAdmin || currentMember?.fundManager == true

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppDestination.DASHBOARD.route
    val isPrimaryTab = PrimaryTabs.any { it.destination.route == currentRoute }
    val isChatRoom = currentRoute == AppDestination.CHAT_ROOM.route

    fun navigateTo(destination: AppDestination) {
        if (PrimaryTabs.any { it.destination == destination }) {
            navController.navigate(destination.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navController.navigate(destination.route) {
                launchSingleTop = true
            }
        }
    }

    fun openRoom(target: ActiveRoomTarget) {
        activeRoom = target
        viewModel.selectChatRoom(target.roomId)
        navigateTo(AppDestination.CHAT_ROOM)
    }

    val secondaryTitle = when (currentRoute) {
        AppDestination.GROUPS.route -> "Grup Keluarga"
        AppDestination.CALLS.route -> "Panggilan Keluarga"
        AppDestination.MEMBERS.route -> "Anggota Keluarga"
        AppDestination.FAMILY_TREE.route -> "Silsilah Keluarga"
        AppDestination.CALENDAR.route -> "Kalender Keluarga"
        AppDestination.GALLERY.route -> "Galeri Keluarga"
        AppDestination.FUND.route -> "Kas Keluarga"
        AppDestination.GPS.route -> "GPS & Lokasi Keluarga"
        AppDestination.ADMIN.route -> "Panel Admin"
        else -> null
    }

    val forwardTargets = remember(groups, privateChats, members, activeRoom) {
        val groupTargets = groups.map { g ->
            ActiveRoomTarget(g.id, "group", g.name, "${g.memberUids.size} anggota", g.memberUids)
        }
        val chatTargets = privateChats.map { c ->
            val otherUid = c.memberUids.firstOrNull { it != viewModel.currentUserId }
            val otherName = members.firstOrNull { it.userId == otherUid }?.displayName
                ?: c.participantNames.joinToString(" & ")
            ActiveRoomTarget(c.id, "private", otherName, "Chat Pribadi", c.memberUids)
        }
        (groupTargets + chatTargets).filter { it.roomId != activeRoom?.roomId }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavigationRail && !isChatRoom) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryTabs.forEach { tab ->
                        val selected = currentRoute == tab.destination.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTo(tab.destination) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    if (!isPrimaryTab && !isChatRoom && secondaryTitle != null) {
                        FamilyDetailTopBar(
                            title = secondaryTitle,
                            subtitle = family?.name,
                            onBack = { navController.popBackStack() }
                        )
                    }
                },
                bottomBar = {
                    if (!useNavigationRail && isPrimaryTab) {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            PrimaryTabs.forEach { tab ->
                                val selected = currentRoute == tab.destination.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(tab.destination) },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) },
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = if (useNavigationRail) 960.dp else 600.dp)
                            .fillMaxSize()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = AppDestination.DASHBOARD.route
                        ) {
                            composable(AppDestination.DASHBOARD.route) {
                                DashboardScreen(
                                    family = family,
                                    currentMember = currentMember,
                                    members = members,
                                    groups = groups,
                                    moments = moments,
                                    events = calendarEvents,
                                    safetyAlerts = safetyAlerts,
                                    onNavigate = { navigateTo(it) },
                                    isLocalTrialMode = isLocalTrialMode,
                                    activeLocalUid = activeLocalUid,
                                    onSwitchLocalUser = onSwitchLocalUser,
                                    currentTheme = currentTheme,
                                    onSelectTheme = onSelectTheme
                                )
                            }
                            composable(AppDestination.CHAT_LIST.route) {
                                ChatListScreen(
                                    currentUserId = viewModel.currentUserId,
                                    members = members,
                                    privateChats = privateChats,
                                    groups = groups,
                                    onOpenPrivateChatWithMember = { other ->
                                        viewModel.openOrCreatePrivateChat(other) { chatId ->
                                            openRoom(
                                                ActiveRoomTarget(
                                                    roomId = chatId,
                                                    roomType = "private",
                                                    title = other.displayName,
                                                    subtitle = other.relationship,
                                                    memberUids = listOf(viewModel.currentUserId, other.userId).sorted()
                                                )
                                            )
                                        }
                                    },
                                    onOpenRoom = { openRoom(it) },
                                    onOpenGroupsScreen = { navigateTo(AppDestination.GROUPS) }
                                )
                            }
                            composable(AppDestination.CHAT_ROOM.route) {
                                val room = activeRoom
                                if (room == null) {
                                    LaunchedEffect(Unit) {
                                        navController.popBackStack()
                                    }
                                } else {
                                    ChatRoomScreen(
                                        room = room,
                                        currentUserId = viewModel.currentUserId,
                                        messages = activeMessages,
                                        availableRoomsToForward = forwardTargets,
                                        onBack = {
                                            viewModel.selectChatRoom(null)
                                            navController.popBackStack()
                                        },
                                        onMarkRead = { viewModel.markRoomMessagesRead(it) },
                                        onSendMessage = { rId, rType, text, mType, media, repText, repSender, fwd, uids ->
                                            viewModel.sendMessage(rId, rType, text, mType, media, repText, repSender, fwd, uids)
                                        },
                                        onStartVoiceCall = { targetName ->
                                            viewModel.startCall("voice", targetName) {
                                                navigateTo(AppDestination.CALLS)
                                            }
                                        },
                                        onStartVideoCall = { targetName ->
                                            viewModel.startCall("video", targetName) {
                                                navigateTo(AppDestination.CALLS)
                                            }
                                        }
                                    )
                                }
                            }
                            composable(AppDestination.GROUPS.route) {
                                BackHandler { navController.popBackStack() }
                                GroupsScreen(
                                    isAdmin = isAdmin,
                                    groups = groups,
                                    members = members,
                                    onOpenGroupChat = { grp ->
                                        openRoom(
                                            ActiveRoomTarget(
                                                roomId = grp.id,
                                                roomType = "group",
                                                title = grp.name,
                                                subtitle = "${grp.memberUids.size} anggota • ${grp.category}",
                                                memberUids = grp.memberUids
                                            )
                                        )
                                    },
                                    onCreateGroup = { name, desc, cat, uids ->
                                        viewModel.createGroup(name, desc, cat, uids)
                                    },
                                    onUpdateGroup = { gId, name, desc, cat, uids ->
                                        viewModel.updateGroup(gId, name, desc, cat, uids)
                                    },
                                    onDeleteGroup = { viewModel.deleteGroup(it) }
                                )
                            }
                            composable(AppDestination.MOMENTS.route) {
                                MomentsScreen(
                                    currentUserId = viewModel.currentUserId,
                                    moments = moments,
                                    comments = momentComments,
                                    onSelectMomentForComments = { viewModel.selectMomentForComments(it) },
                                    onCreateMoment = { cap, mType, label ->
                                        viewModel.createMoment(cap, mType, label)
                                    },
                                    onToggleLike = { viewModel.toggleMomentLike(it) },
                                    onAddComment = { mId, txt -> viewModel.addMomentComment(mId, txt) }
                                )
                            }
                            composable(AppDestination.CALLS.route) {
                                BackHandler { navController.popBackStack() }
                                CallsScreen(
                                    calls = calls,
                                    members = members,
                                    groups = groups,
                                    onStartCall = { cType, targetName ->
                                        viewModel.startCall(cType, targetName)
                                    },
                                    onUpdateCallStatus = { callId, status, join ->
                                        viewModel.updateCallStatus(callId, status, join)
                                    }
                                )
                            }
                            composable(AppDestination.MEMBERS.route) {
                                BackHandler { navController.popBackStack() }
                                MembersScreen(
                                    family = family,
                                    currentUserId = viewModel.currentUserId,
                                    members = members,
                                    onStartChat = { other ->
                                        viewModel.openOrCreatePrivateChat(other) { chatId ->
                                            openRoom(
                                                ActiveRoomTarget(
                                                    roomId = chatId,
                                                    roomType = "private",
                                                    title = other.displayName,
                                                    subtitle = other.relationship,
                                                    memberUids = listOf(viewModel.currentUserId, other.userId).sorted()
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                            composable(AppDestination.FAMILY_TREE.route) {
                                BackHandler { navController.popBackStack() }
                                FamilyTreeScreen(
                                    nodes = treeNodes,
                                    onAddTreeNode = { fullName, relTitle, gen, parent, spouse, notes ->
                                        viewModel.addTreeNode(fullName, relTitle, gen, parent, spouse, notes)
                                    }
                                )
                            }
                            composable(AppDestination.CALENDAR.route) {
                                BackHandler { navController.popBackStack() }
                                CalendarScreen(
                                    events = calendarEvents,
                                    onAddEvent = { title, cat, date, desc, rem ->
                                        viewModel.addCalendarEvent(title, cat, date, desc, rem)
                                    }
                                )
                            }
                            composable(AppDestination.GALLERY.route) {
                                BackHandler { navController.popBackStack() }
                                GalleryScreen(
                                    isAdmin = isAdmin,
                                    albums = galleryAlbums,
                                    onCreateAlbum = { title, evt, yr, mType, access, items ->
                                        viewModel.createGalleryAlbum(title, evt, yr, mType, access, items)
                                    },
                                    onAddMediaToAlbum = { aId, label ->
                                        viewModel.addMediaToAlbum(aId, label)
                                    }
                                )
                            }
                            composable(AppDestination.FUND.route) {
                                BackHandler { navController.popBackStack() }
                                FundScreen(
                                    canManageFund = canManageFund,
                                    transactions = fundTransactions,
                                    onAddTransaction = { title, txType, amt, cat, note ->
                                        viewModel.addFundTransaction(title, txType, amt, cat, note)
                                    },
                                    onDeleteTransaction = { viewModel.deleteFundTransaction(it) }
                                )
                            }
                            composable(AppDestination.SAFETY.route) {
                                SafetyScreen(
                                    currentMember = currentMember,
                                    alerts = safetyAlerts,
                                    members = members,
                                    onSendSafetyAlert = { type, msg, lat, lng, consent ->
                                        viewModel.sendSafetyAlert(type, msg, lat, lng, consent)
                                    },
                                    onResolveAlert = { viewModel.resolveSafetyAlert(it) },
                                    onOpenGpsScreen = { navigateTo(AppDestination.GPS) }
                                )
                            }
                            composable(AppDestination.GPS.route) {
                                BackHandler { navController.popBackStack() }
                                FamilyGpsScreen(
                                    currentMember = currentMember,
                                    members = members,
                                    groups = groups,
                                    onUpdateOwnLocation = { lat, lng, enabled, silent ->
                                        viewModel.updateOwnGpsLocation(lat, lng, enabled, silent)
                                    },
                                    onShareGpsToGroup = { group, lat, lng, address ->
                                        viewModel.shareGpsToGroup(group, lat, lng, address)
                                    }
                                )
                            }
                            composable(AppDestination.PROFILE.route) {
                                ProfileSettingsScreen(
                                    currentMember = currentMember,
                                    userEmail = userEmail,
                                    isLocalTrialMode = isLocalTrialMode,
                                    currentTheme = currentTheme,
                                    onSelectTheme = onSelectTheme,
                                    onUpdateProfile = { dispName, rel, phone, showPhone, bday, loc ->
                                        viewModel.updateProfile(dispName, rel, phone, showPhone, bday, loc)
                                    },
                                    onOpenAdminPanel = {
                                        if (isAdmin) navigateTo(AppDestination.ADMIN)
                                    },
                                    onSignOut = onSignOut
                                )
                            }
                            composable(AppDestination.ADMIN.route) {
                                BackHandler { navController.popBackStack() }
                                if (isAdmin) {
                                    AdminPanelScreen(
                                        family = family,
                                        currentUserId = viewModel.currentUserId,
                                        members = members,
                                        groups = groups,
                                        onUpdateFamilyName = { viewModel.updateFamilyName(it) },
                                        onRegenerateInvite = { viewModel.regenerateInviteCode() },
                                        onUpdateMemberRole = { tUid, role, fundMgr ->
                                            viewModel.updateMemberRole(tUid, role, fundMgr)
                                        },
                                        onRemoveMember = { viewModel.removeMember(it) },
                                        onCreateGroup = { name, desc, cat, uids ->
                                            viewModel.createGroup(name, desc, cat, uids)
                                        },
                                        onDeleteGroup = { viewModel.deleteGroup(it) }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text("Khusus Admin Keluarga", style = MaterialTheme.typography.titleLarge)
                                            Text(
                                                "Hanya Admin keluarga yang diizinkan mengakses panel manajemen.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center
                                            )
                                            Button(onClick = { navController.popBackStack() }) {
                                                Text("Kembali")
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
    }
}
