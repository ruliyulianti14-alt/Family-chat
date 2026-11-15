package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CalendarEvent
import com.example.data.model.Family
import com.example.data.model.FamilyGroup
import com.example.data.model.FamilyMember
import com.example.data.model.FamilyMoment
import com.example.data.model.SafetyAlert
import com.example.ui.components.FamilyAvatar
import com.example.ui.components.QuickFeatureCard
import com.example.ui.components.RoleBadge
import com.example.ui.components.SectionTitleRow
import com.example.ui.components.ThemePickerDialog
import com.example.ui.theme.AppThemeOption
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.FamilyMint
import com.example.ui.theme.FamilyMintContainer
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.WarmOrangeContainer

enum class AppDestination(val route: String) {
    DASHBOARD("dashboard"),
    CHAT_LIST("chat_list"),
    CHAT_ROOM("chat_room"),
    GROUPS("groups"),
    MOMENTS("moments"),
    CALLS("calls"),
    MEMBERS("members"),
    FAMILY_TREE("family_tree"),
    CALENDAR("calendar"),
    GALLERY("gallery"),
    FUND("fund"),
    SAFETY("safety"),
    GPS("gps"),
    PROFILE("profile"),
    ADMIN("admin")
}

private data class QuickTileData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val container: Color,
    val destination: AppDestination,
    val tag: String
)

@Composable
fun DashboardScreen(
    family: Family?,
    currentMember: FamilyMember?,
    members: List<FamilyMember>,
    groups: List<FamilyGroup>,
    moments: List<FamilyMoment>,
    events: List<CalendarEvent>,
    safetyAlerts: List<SafetyAlert>,
    onNavigate: (AppDestination) -> Unit,
    isLocalTrialMode: Boolean = false,
    activeLocalUid: String = "",
    onSwitchLocalUser: (String) -> Unit = {},
    currentTheme: AppThemeOption = AppThemeOption.SKY_WARM,
    onSelectTheme: (AppThemeOption) -> Unit = {}
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    val activeSos = safetyAlerts.filter { it.status == "active" && it.alertType == "sos" }
    val isAdmin = currentMember?.isAdmin == true || family?.adminUid == currentMember?.userId

    val quickTiles = buildList {
        add(QuickTileData("Chat Pribadi", "${members.size} anggota", Icons.AutoMirrored.Filled.Chat, SkyBluePrimary, SkyBlueContainer, AppDestination.CHAT_LIST, "quick_chat"))
        add(QuickTileData("Grup Keluarga", "${groups.size} grup aktif", Icons.Default.Groups, WarmOrange, WarmOrangeContainer, AppDestination.GROUPS, "quick_groups"))
        add(QuickTileData("Family Moments", "${moments.size} momen", Icons.Default.AutoAwesome, FamilyMint, FamilyMintContainer, AppDestination.MOMENTS, "quick_moments"))
        add(QuickTileData("Panggilan", "Suara & Video", Icons.Default.Call, SkyBlueDark, SkyBlueContainer, AppDestination.CALLS, "quick_calls"))
        add(QuickTileData("Anggota", "Direktori & Undangan", Icons.Default.People, WarmOrange, WarmOrangeContainer, AppDestination.MEMBERS, "quick_members"))
        add(QuickTileData("Silsilah", "Pohon Keluarga", Icons.Default.AccountTree, FamilyMint, FamilyMintContainer, AppDestination.FAMILY_TREE, "quick_tree"))
        add(QuickTileData("Kalender", "${events.size} agenda", Icons.Default.CalendarMonth, SkyBluePrimary, SkyBlueContainer, AppDestination.CALENDAR, "quick_calendar"))
        add(QuickTileData("Galeri", "Album Foto & Video", Icons.Default.PhotoLibrary, WarmOrange, WarmOrangeContainer, AppDestination.GALLERY, "quick_gallery"))
        add(QuickTileData("Kas Keluarga", "Transparansi Dana", Icons.Default.AccountBalanceWallet, FamilyMint, FamilyMintContainer, AppDestination.FUND, "quick_fund"))
        add(QuickTileData("Keamanan", "Tombol SOS & Darurat", Icons.Default.Shield, EmergencyRed, EmergencyRedContainer, AppDestination.SAFETY, "quick_safety"))
        add(QuickTileData("GPS Keluarga", "Peta & Lokasi Live", Icons.Default.MyLocation, SkyBluePrimary, SkyBlueContainer, AppDestination.GPS, "quick_gps"))
        add(QuickTileData("Profil Saya", "Privasi & Akun", Icons.Default.Person, SkyBlueDark, SkyBlueContainer, AppDestination.PROFILE, "quick_profile"))
        if (isAdmin) {
            add(QuickTileData("Panel Admin", "Kelola Keluarga & Grup", Icons.Default.AdminPanelSettings, WarmOrange, WarmOrangeContainer, AppDestination.ADMIN, "quick_admin"))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Family Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_family_banner),
                            contentDescription = "Foto banner keluarga",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xCC0C3559))
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { showThemeDialog = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.88f))
                                    .testTag("dashboard_theme_button")
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = "Pilih Tema", tint = SkyBlueDark)
                            }
                            IconButton(
                                onClick = { onNavigate(AppDestination.PROFILE) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.88f))
                                    .testTag("dashboard_profile_button")
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Profil", tint = SkyBlueDark)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FamilyAvatar(
                                name = family?.name ?: "Keluarga",
                                colorIndex = 0L,
                                size = 50.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = family?.name ?: "Keluarga Harmoni",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = family?.slogan ?: "One Family, One Connection, One Love.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Halo, ${currentMember?.displayName ?: "Keluarga"} (${currentMember?.relationship ?: "Anggota"})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${members.size} Anggota • ${groups.size} Grup Keluarga",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RoleBadge(
                            label = if (isAdmin) "ADMIN" else if (currentMember?.fundManager == true) "BENDAHARA" else "MEMBER",
                            containerColor = if (isAdmin) WarmOrangeContainer else SkyBlueContainer,
                            contentColor = if (isAdmin) WarmOrange else SkyBlueDark
                        )
                    }
                }
            }
        }

        // 1b. Local Trial Role Switcher (allows testing Admin vs Bendahara vs Member RBAC without Google login)
        if (isLocalTrialMode && members.size > 1) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmOrangeContainer.copy(alpha = 0.72f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Mode Uji Coba Lokal • Ganti Peran Aktif",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF7C2D12)
                        )
                        Text(
                            text = "Ketuk profil anggota di bawah untuk menguji hak akses Admin (buat grup), Bendahara (kas keluarga), dan Member biasa:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF7C2D12)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            members.forEach { member ->
                                val roleLabel = when {
                                    member.isAdmin || family?.adminUid == member.userId -> "Admin"
                                    member.fundManager -> "Bendahara"
                                    else -> "Member"
                                }
                                FilterChip(
                                    selected = member.userId == activeLocalUid,
                                    onClick = { onSwitchLocalUser(member.userId) },
                                    label = { Text("${member.displayName} ($roleLabel)") },
                                    modifier = Modifier.testTag("switch_role_${member.userId}")
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Active Emergency SOS Banner (if any)
        if (activeSos.isNotEmpty()) {
            item {
                val latestSos = activeSos.first()
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EmergencyRedContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(AppDestination.SAFETY) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Darurat SOS",
                            tint = EmergencyRed,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DARURAT SOS: ${latestSos.senderName}",
                                style = MaterialTheme.typography.titleMedium,
                                color = EmergencyRed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = latestSos.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            }
        }

        // 3. Notifications & Agenda Summary
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SkyBlueContainer.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(AppDestination.CALENDAR) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notifikasi agenda",
                            tint = SkyBluePrimary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notifikasi & Agenda Keluarga",
                            style = MaterialTheme.typography.titleMedium
                        )
                        val nextEvent = events.firstOrNull()
                        Text(
                            text = if (nextEvent != null) {
                                "${nextEvent.title} • ${nextEvent.eventDate}"
                            } else {
                                "Belum ada agenda mendatang. Ketuk untuk menambah ulang tahun atau kumpul keluarga."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 4. Quick Access Grid (2 columns)
        item {
            SectionTitleRow(title = "Akses Cepat Keluarga")
        }

        val rows = quickTiles.chunked(2)
        items(rows.size) { rowIndex ->
            val pair = rows[rowIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { tile ->
                    QuickFeatureCard(
                        title = tile.title,
                        subtitle = tile.subtitle,
                        icon = tile.icon,
                        tint = tile.tint,
                        containerColor = tile.container,
                        testTag = tile.tag,
                        onClick = { onNavigate(tile.destination) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // 5. Recent Family Activity
        item {
            SectionTitleRow(
                title = "Aktivitas Terbaru",
                actionLabel = "Lihat Momen",
                onAction = { onNavigate(AppDestination.MOMENTS) }
            )
        }

        if (moments.isEmpty() && groups.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Belum ada aktivitas terbaru. Mulai sapa keluarga di Grup atau bagikan Family Moment pertama Anda!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(moments.take(3).size) { idx ->
                val moment = moments[idx]
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(AppDestination.MOMENTS) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FamilyAvatar(name = moment.authorName, colorIndex = idx.toLong(), size = 40.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${moment.authorName} membagikan momen",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = moment.caption,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            currentTheme = currentTheme,
            onSelectTheme = onSelectTheme,
            onDismiss = { showThemeDialog = false }
        )
    }
}
