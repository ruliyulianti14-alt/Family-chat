package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CalendarEvent
import com.example.data.model.Family
import com.example.data.model.FamilyMember
import com.example.data.model.GalleryAlbum
import com.example.data.model.TreeNode
import com.example.ui.components.EmptyFamilyState
import com.example.ui.components.FamilyAvatar
import com.example.ui.components.FamilyNotificationHelper
import com.example.ui.components.RoleBadge
import com.example.ui.theme.FamilyMint
import com.example.ui.theme.FamilyMintContainer
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.WarmOrangeContainer

@Composable
fun MembersScreen(
    family: Family?,
    currentUserId: String,
    members: List<FamilyMember>,
    onStartChat: (FamilyMember) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Anggota Keluarga (${members.size})", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SkyBlueContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Undang Anggota Baru", style = MaterialTheme.typography.titleMedium, color = SkyBlueDark)
                        Text(
                            text = "Kode Undangan: ${family?.inviteCode ?: "-"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SkyBlueDark
                        )
                    }
                    Button(
                        onClick = {
                            val code = family?.inviteCode ?: ""
                            val famName = family?.name ?: "Keluarga Kita"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Ayo bergabung di MY FAMILY CHAT ($famName)! Gunakan kode undangan: $code\n\"One Family, One Connection, One Love.\""
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Undangan Keluarga"))
                        },
                        modifier = Modifier.testTag("share_invite_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Undang")
                    }
                }
            }
        }

        if (members.isEmpty()) {
            item {
                EmptyFamilyState(
                    icon = Icons.Default.People,
                    title = "Belum Ada Anggota",
                    description = "Bagikan kode undangan di atas kepada ayah, ibu, kakek, nenek, atau saudara."
                )
            }
        } else {
            items(members, key = { it.userId }) { member ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FamilyAvatar(name = member.displayName, colorIndex = member.avatarColorIndex, size = 50.dp)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(member.displayName, style = MaterialTheme.typography.titleMedium)
                                if (member.isAdmin) {
                                    RoleBadge("Admin", WarmOrangeContainer, WarmOrange)
                                } else if (member.fundManager) {
                                    RoleBadge("Bendahara", FamilyMintContainer, FamilyMint)
                                }
                            }
                            Text(
                                text = member.relationship + if (member.birthday.isNotBlank()) " • Lahir: ${member.birthday}" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (member.showPhone && member.phone.isNotBlank()) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = FamilyMint, modifier = Modifier.size(14.dp))
                                    Text(member.phone, style = MaterialTheme.typography.labelMedium, color = FamilyMint)
                                } else if (member.phone.isNotBlank()) {
                                    Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                    Text("Nomor disembunyikan (Privasi)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (member.userId != currentUserId) {
                            IconButton(onClick = { onStartChat(member) }) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat Pribadi", tint = SkyBluePrimary)
                            }
                        }
                        if (member.showPhone && member.phone.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}")))
                                }
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Telepon", tint = WarmOrange)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FamilyTreeScreen(
    nodes: List<TreeNode>,
    onAddTreeNode: (fullName: String, relationTitle: String, generation: Long, parentName: String, spouseName: String, notes: String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedNode by remember { mutableStateOf<TreeNode?>(null) }

    val generationLabels = mapOf(
        1L to "Generasi 1 • Kakek & Nenek",
        2L to "Generasi 2 • Ayah, Ibu, Paman & Bibi",
        3L to "Generasi 3 • Anak & Saudara",
        4L to "Generasi 4 • Cucu"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = FamilyMint,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_tree_node_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Anggota Silsilah")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Silsilah Keluarga", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Pohon silsilah interaktif dari Kakek-Nenek hingga Anak dan Cucu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (nodes.isEmpty()) {
                item {
                    EmptyFamilyState(
                        icon = Icons.Default.AccountTree,
                        title = "Pohon Silsilah Masih Kosong",
                        description = "Ketuk tombol + untuk menambahkan Kakek, Nenek, Ayah, Ibu, Paman, Bibi, Saudara, Anak, dan Cucu."
                    )
                }
            } else {
                listOf(1L, 2L, 3L, 4L).forEach { gen ->
                    val genNodes = nodes.filter { it.generation == gen }
                    if (genNodes.isNotEmpty()) {
                        item(key = "gen_$gen") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = if (gen % 2L == 1L) SkyBlueContainer else WarmOrangeContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = generationLabels[gen] ?: "Generasi $gen",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (gen % 2L == 1L) SkyBlueDark else WarmOrange,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    genNodes.forEachIndexed { idx, node ->
                                        Card(
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                            modifier = Modifier
                                                .width(175.dp)
                                                .clickable { selectedNode = node }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                FamilyAvatar(name = node.fullName, colorIndex = idx.toLong() + gen, size = 46.dp)
                                                Text(node.fullName, style = MaterialTheme.typography.titleMedium)
                                                RoleBadge(
                                                    label = node.relationTitle,
                                                    containerColor = SkyBlueContainer,
                                                    contentColor = SkyBlueDark
                                                )
                                                if (node.parentName.isNotBlank()) {
                                                    Text(
                                                        text = "Anak dari: ${node.parentName}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (node.spouseName.isNotBlank()) {
                                                    Text(
                                                        text = "Pasangan: ${node.spouseName}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = WarmOrange
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (gen < 4L && nodes.any { it.generation > gen }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(22.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawLine(
                                                color = Color(0xFF90CAF9),
                                                start = Offset(size.width / 2f, 0f),
                                                end = Offset(size.width / 2f, size.height),
                                                strokeWidth = 5f
                                            )
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

    if (showAddDialog) {
        var fullName by remember { mutableStateOf("") }
        var relationTitle by remember { mutableStateOf("Ayah") }
        var generation by remember { mutableLongStateOf(2L) }
        var parentName by remember { mutableStateOf("") }
        var spouseName by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        val presets = listOf(
            Triple("Kakek", 1L, "Gen 1"),
            Triple("Nenek", 1L, "Gen 1"),
            Triple("Ayah", 2L, "Gen 2"),
            Triple("Ibu", 2L, "Gen 2"),
            Triple("Paman", 2L, "Gen 2"),
            Triple("Bibi", 2L, "Gen 2"),
            Triple("Anak", 3L, "Gen 3"),
            Triple("Saudara", 3L, "Gen 3"),
            Triple("Cucu", 4L, "Gen 4")
        )

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Anggota Silsilah") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nama Lengkap") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("tree_name_input")
                    )
                    Text("Peran & Generasi", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { (title, gen, _) ->
                            FilterChip(
                                selected = relationTitle == title,
                                onClick = {
                                    relationTitle = title
                                    generation = gen
                                },
                                label = { Text(title) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = parentName,
                        onValueChange = { parentName = it },
                        label = { Text("Nama Orang Tua (Opsional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = spouseName,
                        onValueChange = { spouseName = it },
                        label = { Text("Nama Pasangan (Opsional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan / Kota Asal") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddTreeNode(fullName, relationTitle, generation, parentName, spouseName, notes)
                        showAddDialog = false
                    },
                    enabled = fullName.isNotBlank()
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }

    if (selectedNode != null) {
        val n = selectedNode!!
        AlertDialog(
            onDismissRequest = { selectedNode = null },
            title = { Text(n.fullName) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Hubungan: ${n.relationTitle} (Generasi ${n.generation})")
                    if (n.parentName.isNotBlank()) Text("Orang Tua: ${n.parentName}")
                    if (n.spouseName.isNotBlank()) Text("Pasangan: ${n.spouseName}")
                    if (n.notes.isNotBlank()) Text("Catatan: ${n.notes}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedNode = null }) { Text("Tutup") }
            }
        )
    }
}

@Composable
fun CalendarScreen(
    events: List<CalendarEvent>,
    onAddEvent: (title: String, category: String, eventDate: String, description: String, reminderEnabled: Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("all") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = if (selectedFilter == "all") events else events.filter { it.category == selectedFilter }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SkyBluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_calendar_event_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Agenda Keluarga")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Kalender Keluarga", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "all" to "Semua",
                        "birthday" to "Ulang Tahun",
                        "wedding" to "Pernikahan",
                        "gathering" to "Kumpul Keluarga",
                        "reminder" to "Pengingat"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyFamilyState(
                        icon = Icons.Default.CalendarMonth,
                        title = "Belum Ada Agenda Keluarga",
                        description = "Catat ulang tahun anggota keluarga, acara pernikahan, atau jadwal kumpul keluarga."
                    )
                }
            } else {
                items(filtered, key = { it.id }) { event ->
                    val icon = when (event.category) {
                        "birthday" -> Icons.Default.Cake
                        "wedding" -> Icons.Default.Favorite
                        "gathering" -> Icons.Default.Groups
                        else -> Icons.Default.NotificationsActive
                    }
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(WarmOrangeContainer, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = WarmOrange)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = event.eventDate,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SkyBlueDark
                                )
                                if (event.description.isNotBlank()) {
                                    Text(
                                        text = event.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    FamilyNotificationHelper.showFamilyNotification(
                                        context,
                                        "Pengingat Keluarga: ${event.title}",
                                        "${event.eventDate} — ${event.description.ifBlank { "Jangan lewatkan momen keluarga!" }}",
                                        event.id.hashCode()
                                    )
                                    val calIntent = Intent(Intent.ACTION_INSERT).apply {
                                        data = CalendarContract.Events.CONTENT_URI
                                        putExtra(CalendarContract.Events.TITLE, event.title)
                                        putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                                    }
                                    try { context.startActivity(calIntent) } catch (_: Exception) {}
                                }
                            ) {
                                Icon(Icons.Default.EventAvailable, contentDescription = "Simpan ke Kalender HP", tint = SkyBluePrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("birthday") }
        var eventDate by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var reminderEnabled by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Acara Keluarga") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama Acara (mis. Ultah Nenek)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "birthday" to "Ulang Tahun",
                            "wedding" to "Pernikahan",
                            "gathering" to "Kumpul",
                            "reminder" to "Pengingat"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = category == key,
                                onClick = { category = key },
                                label = { Text(label) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Tanggal (mis. 20 Des 2026)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Lokasi / Catatan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Aktifkan Pengingat")
                        Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddEvent(title, category, eventDate, description, reminderEnabled)
                        if (reminderEnabled) {
                            FamilyNotificationHelper.showFamilyNotification(
                                context,
                                "Agenda Keluarga Tersimpan",
                                "$title pada $eventDate"
                            )
                        }
                        showAddDialog = false
                    },
                    enabled = title.isNotBlank() && eventDate.length >= 4
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun GalleryScreen(
    isAdmin: Boolean,
    albums: List<GalleryAlbum>,
    onCreateAlbum: (title: String, eventName: String, year: Long, mediaType: String, accessLevel: String, initialItems: List<String>) -> Unit,
    onAddMediaToAlbum: (albumId: String, mediaLabel: String) -> Unit
) {
    var selectedYearFilter by remember { mutableStateOf<Long?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var openedAlbum by remember { mutableStateOf<GalleryAlbum?>(null) }

    val visibleAlbums = albums
        .filter { it.accessLevel != "admin_only" || isAdmin }
        .filter { selectedYearFilter == null || it.year == selectedYearFilter }
    val availableYears = albums.map { it.year }.distinct().sortedDescending()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = WarmOrange,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_album_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Album Galeri")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Galeri Keluarga", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Album foto dan video dikelompokkan berdasarkan acara, tahun, dan hak akses.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (availableYears.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedYearFilter == null,
                            onClick = { selectedYearFilter = null },
                            label = { Text("Semua Tahun") }
                        )
                        availableYears.forEach { yr ->
                            FilterChip(
                                selected = selectedYearFilter == yr,
                                onClick = { selectedYearFilter = yr },
                                label = { Text("$yr") }
                            )
                        }
                    }
                }
            }

            if (visibleAlbums.isEmpty()) {
                item {
                    EmptyFamilyState(
                        icon = Icons.Default.PhotoLibrary,
                        title = "Belum Ada Album Keluarga",
                        description = "Buat album kenangan berdasarkan acara (mis. Lebaran 2026, Liburan Jogja) dan atur hak aksesnya."
                    )
                }
            } else {
                items(visibleAlbums, key = { it.id }) { album ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openedAlbum = album }
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(album.title, style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        "${album.eventName} • ${album.year}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SkyBlueDark
                                    )
                                }
                                RoleBadge(
                                    label = when (album.accessLevel) {
                                        "admin_only" -> "Khusus Admin"
                                        "adults_only" -> "Dewasa"
                                        else -> "Semua Keluarga"
                                    },
                                    containerColor = WarmOrangeContainer,
                                    contentColor = WarmOrange
                                )
                            }
                            Text(
                                text = "${album.itemLabels.size} item media (${album.mediaType})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var eventName by remember { mutableStateOf("") }
        var yearStr by remember { mutableStateOf("2026") }
        var mediaType by remember { mutableStateOf("photo") }
        var accessLevel by remember { mutableStateOf("all_family") }
        var initialItems by remember { mutableStateOf<List<String>>(emptyList()) }

        val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                initialItems = initialItems + "Media: ${uri.lastPathSegment ?: "foto"}"
            }
        }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Buat Album Keluarga") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Album") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eventName, onValueChange = { eventName = it }, label = { Text("Nama Acara") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = yearStr, onValueChange = { yearStr = it }, label = { Text("Tahun") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Tipe Media", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("photo" to "Foto", "video" to "Video", "mixed" to "Campuran").forEach { (k, l) ->
                            FilterChip(selected = mediaType == k, onClick = { mediaType = k }, label = { Text(l) })
                        }
                    }
                    Text("Hak Akses Album", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("all_family" to "Semua", "adults_only" to "Dewasa", "admin_only" to "Admin").forEach { (k, l) ->
                            FilterChip(selected = accessLevel == k, onClick = { accessLevel = k }, label = { Text(l) })
                        }
                    }
                    OutlinedButton(
                        onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Foto/Video (${initialItems.size})")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val yr = yearStr.toLongOrNull()?.coerceIn(1950L, 2100L) ?: 2026L
                        onCreateAlbum(title, eventName, yr, mediaType, accessLevel, initialItems)
                        showCreateDialog = false
                    },
                    enabled = title.isNotBlank() && eventName.isNotBlank()
                ) {
                    Text("Buat Album")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Batal") }
            }
        )
    }

    if (openedAlbum != null) {
        val album = albums.firstOrNull { it.id == openedAlbum!!.id } ?: openedAlbum!!
        val addMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                onAddMediaToAlbum(album.id, "Foto/Video: ${uri.lastPathSegment ?: "kenangan"}")
            }
        }
        AlertDialog(
            onDismissRequest = { openedAlbum = null },
            title = { Text("${album.title} (${album.year})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Acara: ${album.eventName}")
                    if (album.itemLabels.isEmpty()) {
                        Text("Belum ada foto/video di album ini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        album.itemLabels.forEach { label ->
                            Surface(color = SkyBlueContainer, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Text(label, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Button(
                        onClick = { addMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Foto/Video via Photo Picker")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { openedAlbum = null }) { Text("Tutup") }
            }
        )
    }
}
