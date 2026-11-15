package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.ChatMessage
import com.example.data.model.FamilyGroup
import com.example.data.model.FamilyMember
import com.example.data.model.PrivateChat
import com.example.ui.components.EmptyFamilyState
import com.example.ui.components.FamilyAvatar
import com.example.ui.components.FamilyDetailTopBar
import com.example.ui.components.RoleBadge
import com.example.ui.components.SectionTitleRow
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.ReadBlueTick
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.WarmOrangeContainer
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActiveRoomTarget(
    val roomId: String,
    val roomType: String, // "private" | "group"
    val title: String,
    val subtitle: String,
    val memberUids: List<String>
)

@Composable
fun ChatListScreen(
    currentUserId: String,
    members: List<FamilyMember>,
    privateChats: List<PrivateChat>,
    groups: List<FamilyGroup>,
    onOpenPrivateChatWithMember: (FamilyMember) -> Unit,
    onOpenRoom: (ActiveRoomTarget) -> Unit,
    onOpenGroupsScreen: () -> Unit
) {
    val otherMembers = members.filter { it.userId != currentUserId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Obrolan Keluarga", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Chat pribadi & grup keluarga dengan pesan suara, media, stiker, balas, dan teruskan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Quick start private chat with any family member
        item {
            SectionTitleRow(title = "Mulai Chat Pribadi")
        }
        if (otherMembers.isEmpty()) {
            item {
                EmptyFamilyState(
                    icon = Icons.Default.AddComment,
                    title = "Belum Ada Anggota Lain",
                    description = "Undang anggota keluarga melalui kode undangan di menu Anggota atau Panel Admin."
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    otherMembers.forEach { member ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenPrivateChatWithMember(member) }
                                .padding(8.dp)
                        ) {
                            FamilyAvatar(name = member.displayName, colorIndex = member.avatarColorIndex, size = 54.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = member.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = member.relationship,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Active Private Chats
        item {
            SectionTitleRow(title = "Chat Pribadi (${privateChats.size})")
        }
        if (privateChats.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Ketuk foto anggota keluarga di atas untuk memulai percakapan pribadi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(privateChats, key = { it.id }) { chat ->
                val otherUid = chat.memberUids.firstOrNull { it != currentUserId }
                val otherMember = members.firstOrNull { it.userId == otherUid }
                val chatTitle = otherMember?.displayName
                    ?: chat.participantNames.joinToString(" & ")
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onOpenRoom(
                                ActiveRoomTarget(
                                    roomId = chat.id,
                                    roomType = "private",
                                    title = chatTitle,
                                    subtitle = otherMember?.relationship ?: "Chat Pribadi",
                                    memberUids = chat.memberUids
                                )
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FamilyAvatar(
                            name = chatTitle,
                            colorIndex = otherMember?.avatarColorIndex ?: 1L,
                            size = 48.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(chatTitle, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = chat.lastMessageText.ifBlank { "Ketuk untuk mengirim pesan" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        RoleBadge(label = "Pribadi", containerColor = SkyBlueContainer, contentColor = SkyBlueDark)
                    }
                }
            }
        }

        // Family Groups Shortcut
        item {
            SectionTitleRow(
                title = "Grup Keluarga (${groups.size})",
                actionLabel = "Kelola Grup",
                onAction = onOpenGroupsScreen
            )
        }
        items(groups, key = { it.id }) { group ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onOpenRoom(
                            ActiveRoomTarget(
                                roomId = group.id,
                                roomType = "group",
                                title = group.name,
                                subtitle = "${group.memberUids.size} anggota • ${group.category}",
                                memberUids = group.memberUids
                            )
                        )
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(WarmOrangeContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = WarmOrange)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(group.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (group.lastMessageText.isNotBlank()) {
                                "${group.lastMessageSender}: ${group.lastMessageText}"
                            } else {
                                group.description
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    RoleBadge(label = "Grup", containerColor = WarmOrangeContainer, contentColor = WarmOrange)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    room: ActiveRoomTarget,
    currentUserId: String,
    messages: List<ChatMessage>,
    availableRoomsToForward: List<ActiveRoomTarget>,
    onBack: () -> Unit,
    onMarkRead: (List<ChatMessage>) -> Unit,
    onSendMessage: (
        roomId: String,
        roomType: String,
        text: String,
        messageType: String,
        mediaLabel: String,
        replyToText: String,
        replyToSender: String,
        forwarded: Boolean,
        memberUids: List<String>
    ) -> Unit,
    onStartVoiceCall: (String) -> Unit,
    onStartVideoCall: (String) -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<ChatMessage?>(null) }
    var forwardingMsg by remember { mutableStateOf<ChatMessage?>(null) }
    var showStickerSheet by remember { mutableStateOf(false) }

    // Voice note recorder & playback state
    var isRecording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentAudioPath by remember { mutableStateOf<String?>(null) }
    var recordStartMs by remember { mutableStateOf(0L) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingMsgId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try { recorder?.release() } catch (_: Exception) {}
            try { mediaPlayer?.release() } catch (_: Exception) {}
        }
    }

    LaunchedEffect(messages.size) {
        onMarkRead(messages)
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri) ?: "image/*"
            val type = if (mime.startsWith("video")) "video" else "image"
            val name = queryFileName(context, uri) ?: if (type == "video") "Video Keluarga" else "Foto Keluarga"
            onSendMessage(
                room.roomId,
                room.roomType,
                if (type == "video") "Mengirim video: $name" else "Mengirim foto: $name",
                type,
                "$name (${uri.lastPathSegment ?: "media"})",
                replyingTo?.text ?: "",
                replyingTo?.senderName ?: "",
                false,
                room.memberUids
            )
            replyingTo = null
        }
    }

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = queryFileName(context, uri) ?: "Dokumen_Keluarga.pdf"
            onSendMessage(
                room.roomId,
                room.roomType,
                "Mengirim dokumen: $name",
                "document",
                name,
                replyingTo?.text ?: "",
                replyingTo?.senderName ?: "",
                false,
                room.memberUids
            )
            replyingTo = null
        }
    }

    fun startRecordingAudio() {
        try {
            val outFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setOutputFile(outFile.absolutePath)
            mr.prepare()
            mr.start()
            recorder = mr
            currentAudioPath = outFile.absolutePath
            recordStartMs = System.currentTimeMillis()
            isRecording = true
        } catch (_: Exception) {
            isRecording = false
        }
    }

    fun stopAndSendVoiceNote() {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (_: Exception) {
        } finally {
            recorder = null
            isRecording = false
        }
        val durationSec = ((System.currentTimeMillis() - recordStartMs) / 1000L).coerceAtLeast(1L)
        val path = currentAudioPath ?: ""
        onSendMessage(
            room.roomId,
            room.roomType,
            "Pesan suara (${durationSec} dtk)",
            "voice",
            path,
            replyingTo?.text ?: "",
            replyingTo?.senderName ?: "",
            false,
            room.memberUids
        )
        replyingTo = null
    }

    val audioPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startRecordingAudio()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        FamilyDetailTopBar(
            title = room.title,
            subtitle = room.subtitle,
            onBack = onBack,
            actions = {
                IconButton(onClick = { onStartVoiceCall(room.title) }) {
                    Icon(Icons.Default.Call, contentDescription = "Panggilan Suara", tint = SkyBluePrimary)
                }
                IconButton(onClick = { onStartVideoCall(room.title) }) {
                    Icon(Icons.Default.Videocam, contentDescription = "Panggilan Video", tint = WarmOrange)
                }
            }
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    EmptyFamilyState(
                        icon = Icons.Default.EmojiEmotions,
                        title = "Belum Ada Pesan",
                        description = "Mulai obrolan hangat dengan teks, stiker keluarga, foto, dokumen, atau pesan suara."
                    )
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    val isMine = msg.senderId == currentUserId
                    val isPlaying = playingMsgId == msg.id
                    MessageBubble(
                        message = msg,
                        isMine = isMine,
                        isPlayingAudio = isPlaying,
                        onReply = { replyingTo = msg },
                        onForward = { forwardingMsg = msg },
                        onPlayVoice = { path ->
                            if (isPlaying) {
                                try { mediaPlayer?.stop(); mediaPlayer?.release() } catch (_: Exception) {}
                                mediaPlayer = null
                                playingMsgId = null
                            } else if (path.isNotBlank() && File(path).exists()) {
                                try {
                                    mediaPlayer?.release()
                                    mediaPlayer = MediaPlayer().apply {
                                        setDataSource(path)
                                        prepare()
                                        setOnCompletionListener {
                                            playingMsgId = null
                                        }
                                        start()
                                    }
                                    playingMsgId = msg.id
                                } catch (_: Exception) {
                                    playingMsgId = null
                                }
                            }
                        }
                    )
                }
            }
        }

        // Reply banner
        if (replyingTo != null) {
            Surface(
                color = SkyBlueContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Membalas ${replyingTo!!.senderName}",
                            style = MaterialTheme.typography.labelLarge,
                            color = SkyBlueDark
                        )
                        Text(
                            text = replyingTo!!.text,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { replyingTo = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Batal balas")
                    }
                }
            }
        }

        // Attachment & Composer bar
        Surface(
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(onClick = { showStickerSheet = true }) {
                        Icon(Icons.Default.EmojiEmotions, contentDescription = "Emoji & Stiker", tint = WarmOrange)
                    }
                    IconButton(
                        onClick = {
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        }
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = "Foto & Video", tint = SkyBluePrimary)
                    }
                    IconButton(onClick = { docPickerLauncher.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Dokumen", tint = SkyBlueDark)
                    }
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Tulis pesan hangat...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input")
                    )
                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val textToSend = inputText.trim()
                                inputText = ""
                                onSendMessage(
                                    room.roomId,
                                    room.roomType,
                                    textToSend,
                                    "text",
                                    "",
                                    replyingTo?.text ?: "",
                                    replyingTo?.senderName ?: "",
                                    false,
                                    room.memberUids
                                )
                                replyingTo = null
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SkyBluePrimary)
                                .testTag("chat_send_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim", tint = Color.White)
                        }
                    } else {
                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    stopAndSendVoiceNote()
                                } else {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) startRecordingAudio()
                                    else audioPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isRecording) EmergencyRed else WarmOrange)
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop & Kirim Suara" else "Rekam Suara",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStickerSheet) {
        val stickers = listOf(
            "❤️ Sayang Keluarga",
            "🙏 Mohon Doanya",
            "🍽️ Ayo Makan Bersama!",
            "🎂 Selamat Ulang Tahun!",
            "🏠 Peluk Hangat dari Rumah",
            "🥰 Rindu Kalian Semua",
            "🎉 Alhamdulillah!",
            "👋 Selamat Pagi Keluarga!"
        )
        val emojis = listOf("❤️", "😂", "🥰", "🙏", "🎉", "👏", "🤗", "🌟", "💐", "☕")
        ModalBottomSheet(onDismissRequest = { showStickerSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Emoji & Stiker Keluarga", style = MaterialTheme.typography.titleLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojis.forEach { emoji ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                inputText += emoji
                                showStickerSheet = false
                            },
                            label = { Text(emoji, style = MaterialTheme.typography.titleLarge) }
                        )
                    }
                }
                Text("Stiker Cepat", style = MaterialTheme.typography.titleMedium)
                stickers.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { stiker ->
                            Surface(
                                color = WarmOrangeContainer,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onSendMessage(
                                            room.roomId,
                                            room.roomType,
                                            stiker,
                                            "sticker",
                                            "",
                                            "",
                                            "",
                                            false,
                                            room.memberUids
                                        )
                                        showStickerSheet = false
                                    }
                            ) {
                                Text(
                                    text = stiker,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (forwardingMsg != null) {
        val msgToFwd = forwardingMsg!!
        AlertDialog(
            onDismissRequest = { forwardingMsg = null },
            title = { Text("Teruskan Pesan ke...") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (availableRoomsToForward.isEmpty()) {
                        Text("Belum ada obrolan lain untuk diteruskan.")
                    } else {
                        availableRoomsToForward.forEach { target ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSendMessage(
                                            target.roomId,
                                            target.roomType,
                                            msgToFwd.text,
                                            msgToFwd.messageType,
                                            msgToFwd.mediaLabel,
                                            "",
                                            "",
                                            true,
                                            target.memberUids
                                        )
                                        forwardingMsg = null
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(target.title, style = MaterialTheme.typography.titleMedium)
                                    Text(target.subtitle, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { forwardingMsg = null }) { Text("Tutup") }
            }
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    isPlayingAudio: Boolean,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onPlayVoice: (String) -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeText = message.createdAt?.toDate()?.let { timeFormat.format(it) } ?: timeFormat.format(Date())
    val bubbleColor = if (isMine) SkyBluePrimary else MaterialTheme.colorScheme.surface
    val textColor = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMine) 18.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 18.dp
            ),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!isMine) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (message.forwarded) {
                    Text(
                        text = "↪ Diteruskan",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMine) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (message.replyToText.isNotBlank()) {
                    Surface(
                        color = if (isMine) Color.White.copy(alpha = 0.18f) else SkyBlueContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = message.replyToSender.ifBlank { "Balasan" },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = message.replyToText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                when (message.messageType) {
                    "voice" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPlayVoice(message.mediaLabel) }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Putar pesan suara",
                                tint = textColor
                            )
                            Text(message.text, color = textColor, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    "image", "video", "document" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (message.messageType == "document") Icons.Default.Description else Icons.Default.Photo,
                                contentDescription = null,
                                tint = textColor
                            )
                            Text(message.text, color = textColor, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    else -> {
                        Text(message.text, color = textColor, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = "Balas",
                            tint = textColor.copy(alpha = 0.75f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(onClick = onReply)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Forward,
                            contentDescription = "Teruskan",
                            tint = textColor.copy(alpha = 0.75f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(onClick = onForward)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.8f)
                        )
                        if (isMine) {
                            val isRead = message.status == "read" || message.readByUids.size > 1
                            Icon(
                                imageVector = if (message.status == "sent") Icons.Default.Done else Icons.Default.DoneAll,
                                contentDescription = if (isRead) "Dibaca" else "Terkirim",
                                tint = if (isRead) Color(0xFFFFE082) else Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun queryFileName(context: android.content.Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIdx >= 0) cursor.getString(nameIdx) else null
        }
    } catch (_: Exception) {
        null
    }
}
