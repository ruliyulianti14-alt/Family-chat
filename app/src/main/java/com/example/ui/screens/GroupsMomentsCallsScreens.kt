package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.FamilyCall
import com.example.data.model.FamilyGroup
import com.example.data.model.FamilyMember
import com.example.data.model.FamilyMoment
import com.example.data.model.MomentComment
import com.example.ui.components.EmptyFamilyState
import com.example.ui.components.FamilyAvatar
import com.example.ui.components.RoleBadge
import com.example.ui.components.SectionTitleRow
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.FamilyMint
import com.example.ui.theme.FamilyMintContainer
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.WarmOrangeContainer

@Composable
fun GroupsScreen(
    isAdmin: Boolean,
    groups: List<FamilyGroup>,
    members: List<FamilyMember>,
    onOpenGroupChat: (FamilyGroup) -> Unit,
    onCreateGroup: (name: String, description: String, category: String, memberUids: List<String>) -> Unit,
    onUpdateGroup: (groupId: String, name: String, description: String, category: String, memberUids: List<String>) -> Unit,
    onDeleteGroup: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<FamilyGroup?>(null) }
    var infoGroup by remember { mutableStateOf<FamilyGroup?>(null) }

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = WarmOrange,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("create_group_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Buat Grup Keluarga")
                }
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
                Text("Grup Keluarga", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = if (isAdmin) SkyBlueContainer else WarmOrangeContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isAdmin) Icons.Default.Groups else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isAdmin) SkyBlueDark else WarmOrange
                        )
                        Text(
                            text = if (isAdmin) {
                                "Sebagai Admin, Anda dapat membuat, mengedit, menambah/mengeluarkan anggota, dan menghapus grup (terlindungi di Firestore Rules)."
                            } else {
                                "Pembuatan & perubahan grup hanya boleh dilakukan oleh Admin keluarga sesuai aturan keamanan."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (groups.isEmpty()) {
                item {
                    EmptyFamilyState(
                        icon = Icons.Default.Groups,
                        title = "Belum Ada Grup Keluarga",
                        description = if (isAdmin) "Ketuk tombol + di kanan bawah untuk membuat grup keluarga baru." else "Tunggu Admin keluarga menambahkan Anda ke grup."
                    )
                }
            } else {
                items(groups, key = { it.id }) { group ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenGroupChat(group) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(SkyBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = SkyBlueDark)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(group.name, style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        "${group.category} • ${group.memberUids.size} Anggota",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { infoGroup = group }) {
                                    Icon(Icons.Default.Info, contentDescription = "Info Grup")
                                }
                                if (isAdmin) {
                                    IconButton(onClick = { editingGroup = group }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Grup", tint = WarmOrange)
                                    }
                                    IconButton(onClick = { onDeleteGroup(group.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus Grup", tint = EmergencyRed)
                                    }
                                }
                            }
                            if (group.description.isNotBlank()) {
                                Text(
                                    text = group.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (group.lastMessageText.isNotBlank()) "${group.lastMessageSender}: ${group.lastMessageText}" else "Belum ada pesan",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SkyBlueDark,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { onOpenGroupChat(group) },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Buka Chat")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog && isAdmin) {
        GroupEditorDialog(
            title = "Buat Grup Keluarga",
            initialName = "",
            initialDescription = "",
            initialCategory = "Keluarga Besar",
            initialMemberUids = members.map { it.userId },
            allMembers = members,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc, cat, uids ->
                onCreateGroup(name, desc, cat, uids)
                showCreateDialog = false
            }
        )
    }

    if (editingGroup != null && isAdmin) {
        val g = editingGroup!!
        GroupEditorDialog(
            title = "Edit Grup Keluarga",
            initialName = g.name,
            initialDescription = g.description,
            initialCategory = g.category,
            initialMemberUids = g.memberUids,
            allMembers = members,
            onDismiss = { editingGroup = null },
            onConfirm = { name, desc, cat, uids ->
                onUpdateGroup(g.id, name, desc, cat, uids)
                editingGroup = null
            }
        )
    }

    if (infoGroup != null) {
        val g = infoGroup!!
        val groupMembers = members.filter { g.memberUids.contains(it.userId) }
        AlertDialog(
            onDismissRequest = { infoGroup = null },
            title = { Text(g.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kategori: ${g.category}", style = MaterialTheme.typography.labelLarge)
                    Text(g.description, style = MaterialTheme.typography.bodyMedium)
                    Text("Anggota (${groupMembers.size}):", style = MaterialTheme.typography.titleMedium)
                    groupMembers.forEach { m ->
                        Text("• ${m.displayName} (${m.relationship})", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { infoGroup = null }) { Text("Tutup") }
            }
        )
    }
}

@Composable
fun GroupEditorDialog(
    title: String,
    initialName: String,
    initialDescription: String,
    initialCategory: String,
    initialMemberUids: List<String>,
    allMembers: List<FamilyMember>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, category: String, memberUids: List<String>) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var category by remember { mutableStateOf(initialCategory) }
    var selectedUids by remember { mutableStateOf(initialMemberUids.toSet()) }
    val categories = listOf("Keluarga Besar", "Liburan", "Arisan", "Orang Tua", "Anak & Cucu")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Grup") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("group_name_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Grup") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Kategori", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Text("Pilih Anggota Grup (${selectedUids.size})", style = MaterialTheme.typography.labelLarge)
                allMembers.forEach { m ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedUids = if (selectedUids.contains(m.userId)) {
                                    selectedUids - m.userId
                                } else {
                                    selectedUids + m.userId
                                }
                            }
                    ) {
                        Checkbox(
                            checked = selectedUids.contains(m.userId),
                            onCheckedChange = { checked ->
                                selectedUids = if (checked) selectedUids + m.userId else selectedUids - m.userId
                            }
                        )
                        Text("${m.displayName} (${m.relationship})")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, description, category, selectedUids.toList()) },
                enabled = name.trim().length >= 2,
                modifier = Modifier.testTag("save_group_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomentsScreen(
    currentUserId: String,
    moments: List<FamilyMoment>,
    comments: List<MomentComment>,
    onSelectMomentForComments: (String?) -> Unit,
    onCreateMoment: (caption: String, momentType: String, mediaLabel: String) -> Unit,
    onToggleLike: (FamilyMoment) -> Unit,
    onAddComment: (momentId: String, text: String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var activeCommentMoment by remember { mutableStateOf<FamilyMoment?>(null) }
    var newCommentText by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = WarmOrange,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_moment_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Bagikan Momen")
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
                Text("Family Moments", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Bagikan status cerita, foto, dan video momen hangat bersama keluarga.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (moments.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_empty_moments),
                                contentDescription = "Ilustrasi album momen",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                            Text("Belum Ada Family Moment", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Bagikan kabar hari ini, foto cucu, atau video liburan keluarga pertama Anda!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(moments, key = { it.id }) { moment ->
                    val liked = moment.likedByUids.contains(currentUserId)
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FamilyAvatar(name = moment.authorName, size = 44.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(moment.authorName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        moment.authorRoleLabel.ifBlank { "Keluarga" },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RoleBadge(
                                    label = when (moment.momentType) {
                                        "photo" -> "Foto"
                                        "video" -> "Video"
                                        else -> "Status"
                                    },
                                    containerColor = if (moment.momentType == "text") SkyBlueContainer else WarmOrangeContainer,
                                    contentColor = if (moment.momentType == "text") SkyBlueDark else WarmOrange
                                )
                            }

                            Text(moment.caption, style = MaterialTheme.typography.bodyLarge)

                            if (moment.mediaLabel.isNotBlank()) {
                                Surface(
                                    color = SkyBlueContainer.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (moment.momentType == "video") Icons.Default.Videocam else Icons.Default.Photo,
                                            contentDescription = null,
                                            tint = SkyBlueDark
                                        )
                                        Text(moment.mediaLabel, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { onToggleLike(moment) }) {
                                    Icon(
                                        imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Suka",
                                        tint = if (liked) EmergencyRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${moment.likedByUids.size} Suka")
                                }
                                TextButton(
                                    onClick = {
                                        activeCommentMoment = moment
                                        onSelectMomentForComments(moment.id)
                                    }
                                ) {
                                    Icon(Icons.Default.ModeComment, contentDescription = "Komentar")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${moment.commentCount} Komentar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var caption by remember { mutableStateOf("") }
        var momentType by remember { mutableStateOf("text") }
        var mediaLabel by remember { mutableStateOf("") }
        val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                mediaLabel = "Lampiran: ${uri.lastPathSegment ?: "media_keluarga"}"
            }
        }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Bagikan Family Moment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("text" to "Tulisan", "photo" to "Foto", "video" to "Video").forEach { (type, label) ->
                            FilterChip(
                                selected = momentType == type,
                                onClick = { momentType = type },
                                label = { Text(label) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Cerita atau status untuk keluarga...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("moment_caption_input")
                    )
                    if (momentType != "text") {
                        OutlinedButton(
                            onClick = {
                                val req = if (momentType == "video") {
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                } else {
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                }
                                picker.launch(req)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (mediaLabel.isBlank()) "Pilih Media dari Galeri HP" else mediaLabel)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateMoment(caption, momentType, mediaLabel)
                        showCreateDialog = false
                    },
                    enabled = caption.isNotBlank(),
                    modifier = Modifier.testTag("submit_moment_button")
                ) {
                    Text("Bagikan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Batal") }
            }
        )
    }

    if (activeCommentMoment != null) {
        val mom = activeCommentMoment!!
        ModalBottomSheet(
            onDismissRequest = {
                activeCommentMoment = null
                onSelectMomentForComments(null)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Komentar (${comments.size})", style = MaterialTheme.typography.titleLarge)
                if (comments.isEmpty()) {
                    Text(
                        "Belum ada komentar. Jadilah yang pertama membalas momen ${mom.authorName}!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    comments.forEach { c ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(c.authorName, style = MaterialTheme.typography.labelLarge, color = SkyBlueDark)
                                Text(c.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Tulis komentar hangat...") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(mom.id, newCommentText)
                                newCommentText = ""
                            }
                        }
                    ) {
                        Text("Kirim")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CallsScreen(
    calls: List<FamilyCall>,
    members: List<FamilyMember>,
    groups: List<FamilyGroup>,
    onStartCall: (callType: String, targetName: String) -> Unit,
    onUpdateCallStatus: (callId: String, status: String, join: Boolean) -> Unit
) {
    val context = LocalContext.current
    val activeCalls = calls.filter { it.status == "ringing" || it.status == "active" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Panggilan Keluarga", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = SkyBlueContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ruang sinyal panggilan tersinkronisasi real-time melalui Cloud Firestore. Untuk panggilan suara langsung ke nomor telepon anggota, ketuk ikon Telepon HP (ACTION_DIAL). Keterbatasan: streaming media WebRTC lintas jaringan memerlukan konfigurasi TURN server tambahan.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (activeCalls.isNotEmpty()) {
            item { SectionTitleRow(title = "Panggilan Berlangsung") }
            items(activeCalls, key = { it.id }) { call ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FamilyMintContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = null,
                            tint = FamilyMint,
                            modifier = Modifier.size(30.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${call.callerName} → ${call.targetName}", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${call.callType.uppercase()} • ${call.status.uppercase()} (${call.participantUids.size} peserta)",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        if (call.status == "ringing") {
                            Button(onClick = { onUpdateCallStatus(call.id, "active", true) }) {
                                Text("Gabung")
                            }
                        }
                        IconButton(
                            onClick = { onUpdateCallStatus(call.id, "ended", false) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(EmergencyRed)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Akhiri", tint = Color.White)
                        }
                    }
                }
            }
        }

        item { SectionTitleRow(title = "Mulai Panggilan Grup") }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val defaultGroupName = groups.firstOrNull()?.name ?: "Keluarga Besar"
                Button(
                    onClick = { onStartCall("group", defaultGroupName) },
                    modifier = Modifier.weight(1f).testTag("start_group_call_button")
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Panggilan Grup")
                }
                Button(
                    onClick = { onStartCall("video", defaultGroupName) },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Video Grup")
                }
            }
        }

        item { SectionTitleRow(title = "Hubungi Anggota Keluarga") }
        items(members, key = { it.userId }) { member ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FamilyAvatar(name = member.displayName, colorIndex = member.avatarColorIndex, size = 44.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(member.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(member.relationship, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onStartCall("voice", member.displayName) }) {
                        Icon(Icons.Default.Call, contentDescription = "Panggilan Suara", tint = SkyBluePrimary)
                    }
                    IconButton(onClick = { onStartCall("video", member.displayName) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Panggilan Video", tint = WarmOrange)
                    }
                    if (member.showPhone && member.phone.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = "Dial Telepon", tint = FamilyMint)
                        }
                    }
                }
            }
        }

        item { SectionTitleRow(title = "Riwayat Panggilan (${calls.size})") }
        if (calls.isEmpty()) {
            item {
                EmptyFamilyState(
                    icon = Icons.Default.Call,
                    title = "Belum Ada Riwayat Panggilan",
                    description = "Mulai panggilan suara, video, atau panggilan grup keluarga di atas."
                )
            }
        } else {
            items(calls.take(15), key = { "hist_${it.id}" }) { call ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${call.callerName} → ${call.targetName}", style = MaterialTheme.typography.titleMedium)
                            Text("Tipe: ${call.callType} • Status: ${call.status}", style = MaterialTheme.typography.labelMedium)
                        }
                        RoleBadge(label = call.status.uppercase())
                    }
                }
            }
        }
    }
}
