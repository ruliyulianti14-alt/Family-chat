package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.Family
import com.example.data.model.FamilyGroup
import com.example.data.model.FamilyMember
import com.example.data.model.FundTransaction
import com.example.data.model.SafetyAlert
import com.example.ui.components.EmptyFamilyState
import com.example.ui.components.FamilyAvatar
import com.example.ui.components.FamilyNotificationHelper
import com.example.ui.components.RoleBadge
import com.example.ui.components.SectionTitleRow
import com.example.ui.components.ThemeSelectorCardSection
import com.example.ui.components.formatRupiah
import com.example.ui.theme.AppThemeOption
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.FamilyMint
import com.example.ui.theme.FamilyMintContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.WarmOrangeContainer
import com.example.util.FamilyGpsHelper
import kotlinx.coroutines.launch

@Composable
fun FundScreen(
    canManageFund: Boolean,
    transactions: List<FundTransaction>,
    onAddTransaction: (title: String, txType: String, amountRupiah: Long, category: String, note: String) -> Unit,
    onDeleteTransaction: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val totalIncome = transactions.filter { it.txType == "income" }.sumOf { it.amountRupiah }
    val totalExpense = transactions.filter { it.txType == "expense" }.sumOf { it.amountRupiah }
    val balance = totalIncome - totalExpense

    Scaffold(
        floatingActionButton = {
            if (canManageFund) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = FamilyMint,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_fund_tx_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Catat Kas Keluarga")
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
                Text("Kas Keluarga (Family Fund)", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SkyBluePrimary)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(SkyBluePrimary, SkyBlueDark)))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Laporan Saldo Kas Keluarga", color = Color.White.copy(alpha = 0.88f), style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = formatRupiah(balance),
                                color = Color.White,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Pemasukan", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
                                        Text(formatRupiah(totalIncome), color = Color.White, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Pengeluaran", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
                                        Text(formatRupiah(totalExpense), color = Color.White, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    color = if (canManageFund) FamilyMintContainer else WarmOrangeContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (canManageFund) Icons.Default.AccountBalanceWallet else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (canManageFund) FamilyMint else WarmOrange
                        )
                        Text(
                            text = if (canManageFund) {
                                "Anda memiliki hak akses Pengelola Dana / Admin untuk mencatat dan mengelola transaksi kas."
                            } else {
                                "Mode Lihat Transparansi: hanya Admin & Pengelola Dana yang diizinkan menambah/mengubah kas di backend."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item { SectionTitleRow(title = "Riwayat Transaksi (${transactions.size})") }
            if (transactions.isEmpty()) {
                item {
                    EmptyFamilyState(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Belum Ada Transaksi Kas",
                        description = "Catatan pemasukan dan pengeluaran dana keluarga akan tampil transparan di sini."
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    val isIncome = tx.txType == "income"
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
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isIncome) FamilyMintContainer else EmergencyRedContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isIncome) IncomeGreen else ExpenseRose
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${tx.category} • Dicatat oleh ${tx.recordedByName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (tx.note.isNotBlank()) {
                                    Text(tx.note, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Text(
                                text = (if (isIncome) "+ " else "- ") + formatRupiah(tx.amountRupiah),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isIncome) IncomeGreen else ExpenseRose,
                                fontWeight = FontWeight.Bold
                            )
                            if (canManageFund) {
                                IconButton(onClick = { onDeleteTransaction(tx.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog && canManageFund) {
        var title by remember { mutableStateOf("") }
        var txType by remember { mutableStateOf("income") }
        var amountStr by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Iuran") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Catat Transaksi Kas Keluarga") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = txType == "income", onClick = { txType = "income" }, label = { Text("Pemasukan") })
                        FilterChip(selected = txType == "expense", onClick = { txType = "expense" }, label = { Text("Pengeluaran") })
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Keterangan") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Nominal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori (Iuran, Acara, Sosial)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Catatan Tambahan") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toLongOrNull() ?: 0L
                        if (amt > 0L) {
                            onAddTransaction(title, txType, amt, category, note)
                            showAddDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && (amountStr.toLongOrNull() ?: 0L) > 0L
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
fun SafetyScreen(
    currentMember: FamilyMember?,
    alerts: List<SafetyAlert>,
    members: List<FamilyMember>,
    onSendSafetyAlert: (alertType: String, message: String, lat: Double?, lng: Double?, consented: Boolean) -> Unit,
    onResolveAlert: (String) -> Unit,
    onOpenGpsScreen: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var locationConsent by remember(currentMember?.locationSharingEnabled) {
        mutableStateOf(currentMember?.locationSharingEnabled ?: false)
    }
    var customMessage by remember { mutableStateOf("Saya membutuhkan bantuan keluarga segera!") }
    var lastCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    fun fetchLocationAndSend(alertType: String, msg: String) {
        if (!locationConsent) {
            onSendSafetyAlert(alertType, msg, null, null, false)
            FamilyNotificationHelper.showFamilyNotification(context, "Peringatan Keamanan Keluarga", msg, 911)
            return
        }
        scope.launch {
            val res = FamilyGpsHelper.fetchCurrentHighAccuracyFix(context)
            res.onSuccess { reading ->
                lastCoords = reading.latitude to reading.longitude
                onSendSafetyAlert(alertType, msg, reading.latitude, reading.longitude, true)
            }.onFailure {
                onSendSafetyAlert(alertType, msg, null, null, true)
            }
            FamilyNotificationHelper.showFamilyNotification(context, "Peringatan Keamanan Keluarga", msg, 911)
        }
    }

    val locationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationConsent = granted
        if (granted) {
            fetchLocationAndSend("location", "Berbagi lokasi aman dengan persetujuan saya.")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Keamanan Keluarga", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmergencyRedContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Tombol Darurat Keluarga (SOS)",
                        style = MaterialTheme.typography.titleLarge,
                        color = EmergencyRed,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = customMessage,
                        onValueChange = { customMessage = it },
                        label = { Text("Pesan Darurat") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { fetchLocationAndSend("sos", customMessage.ifBlank { "Darurat Keluarga!" }) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("sos_emergency_button")
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("KIRIM SOS DARURAT SEKARANG", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }
        }

        // Opt-in Location Sharing Control
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Berbagi Lokasi (Dengan Persetujuan)", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Lokasi hanya dibagikan saat Anda mengaktifkan izin ini secara sadar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = locationConsent,
                            onCheckedChange = { wantEnable ->
                                if (wantEnable) {
                                    if (FamilyGpsHelper.hasLocationPermission(context)) {
                                        locationConsent = true
                                    } else {
                                        locationPermLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                } else {
                                    locationConsent = false
                                }
                            }
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            if (locationConsent) {
                                fetchLocationAndSend("checkin", "Saya dalam keadaan aman. Lokasi terkini terlampir.")
                            } else {
                                onSendSafetyAlert("checkin", "Check-in aman (berbagi lokasi dimatikan oleh pengguna).", null, null, false)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SkyBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (locationConsent) "Kirim Check-In + Lokasi" else "Kirim Check-In Aman (Tanpa Lokasi)")
                    }
                    Button(
                        onClick = onOpenGpsScreen,
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("safety_open_gps_button")
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka GPS & Radar Lokasi Keluarga")
                    }
                    if (lastCoords != null) {
                        Text(
                            text = "Koordinat terakhir: ${String.format("%.4f", lastCoords!!.first)}, ${String.format("%.4f", lastCoords!!.second)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkyBlueDark
                        )
                    }
                }
            }
        }

        // Active & Past Safety Alerts
        item { SectionTitleRow(title = "Pemberitahuan Darurat & Check-In") }
        if (alerts.isEmpty()) {
            item {
                EmptyFamilyState(
                    icon = Icons.Default.Shield,
                    title = "Semua Keluarga Aman",
                    description = "Belum ada sinyal darurat SOS atau laporan lokasi."
                )
            }
        } else {
            items(alerts, key = { it.id }) { alert ->
                val isActive = alert.status == "active"
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive && alert.alertType == "sos") EmergencyRedContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${alert.senderName} (${alert.alertType.uppercase()})", style = MaterialTheme.typography.titleMedium)
                            RoleBadge(
                                label = if (isActive) "AKTIF" else "SELESAI",
                                containerColor = if (isActive) EmergencyRedContainer else FamilyMintContainer,
                                contentColor = if (isActive) EmergencyRed else FamilyMint
                            )
                        }
                        Text(alert.message, style = MaterialTheme.typography.bodyMedium)
                        if (alert.locationConsented && alert.latitude != null && alert.longitude != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lokasi: ${String.format("%.5f", alert.latitude)}, ${String.format("%.5f", alert.longitude)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SkyBlueDark
                                )
                                TextButton(
                                    onClick = {
                                        FamilyGpsHelper.openInMaps(context, alert.latitude, alert.longitude, alert.senderName)
                                    }
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Buka Peta")
                                }
                            }
                        }
                        if (isActive) {
                            TextButton(onClick = { onResolveAlert(alert.id) }) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FamilyMint)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tandai Sudah Aman")
                            }
                        }
                    }
                }
            }
        }

        // Emergency Contacts
        item { SectionTitleRow(title = "Kontak Darurat") }
        val emergencyServices = listOf(
            "Layanan Darurat Nasional" to "112",
            "Kepolisian" to "110",
            "Ambulans & Medis" to "118",
            "Pemadam Kebakaran" to "113"
        ) + members.filter { it.showPhone && it.phone.isNotBlank() }.map { "${it.displayName} (${it.relationship})" to it.phone }

        items(emergencyServices) { (label, phone) ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(label, style = MaterialTheme.typography.titleMedium)
                        Text(phone, style = MaterialTheme.typography.bodyMedium, color = SkyBlueDark)
                    }
                    Button(
                        onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmOrange)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hubungi")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSettingsScreen(
    currentMember: FamilyMember?,
    userEmail: String,
    isLocalTrialMode: Boolean = false,
    currentTheme: AppThemeOption = AppThemeOption.SKY_WARM,
    onSelectTheme: (AppThemeOption) -> Unit = {},
    onUpdateProfile: (displayName: String, relationship: String, phone: String, showPhone: Boolean, birthday: String, locationSharing: Boolean) -> Unit,
    onOpenAdminPanel: () -> Unit,
    onSignOut: () -> Unit
) {
    var displayName by remember(currentMember?.displayName) { mutableStateOf(currentMember?.displayName ?: "") }
    var relationship by remember(currentMember?.relationship) { mutableStateOf(currentMember?.relationship ?: "Anggota") }
    var phone by remember(currentMember?.phone) { mutableStateOf(currentMember?.phone ?: "") }
    var birthday by remember(currentMember?.birthday) { mutableStateOf(currentMember?.birthday ?: "") }
    var showPhone by remember(currentMember?.showPhone) { mutableStateOf(currentMember?.showPhone ?: false) }
    var locationSharing by remember(currentMember?.locationSharingEnabled) { mutableStateOf(currentMember?.locationSharingEnabled ?: false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Pengaturan Profil", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FamilyAvatar(name = displayName.ifBlank { "Saya" }, colorIndex = currentMember?.avatarColorIndex ?: 0L, size = 60.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(displayName.ifBlank { "Anggota Keluarga" }, style = MaterialTheme.typography.titleLarge)
                        Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        RoleBadge(
                            label = if (currentMember?.isAdmin == true) "ADMIN KELUARGA" else if (currentMember?.fundManager == true) "PENGELOLA DANA" else "MEMBER KELUARGA"
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Informasi & Privasi Anggota", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Nama Tampilan") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = relationship, onValueChange = { relationship = it }, label = { Text("Hubungan Keluarga") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor Telepon") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = birthday, onValueChange = { birthday = it }, label = { Text("Tanggal Lahir") }, modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tampilkan Nomor Telepon ke Keluarga", modifier = Modifier.weight(1f))
                        Switch(checked = showPhone, onCheckedChange = { showPhone = it })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Izinkan Berbagi Lokasi Darurat", modifier = Modifier.weight(1f))
                        Switch(checked = locationSharing, onCheckedChange = { locationSharing = it })
                    }
                    Button(
                        onClick = { onUpdateProfile(displayName, relationship, phone, showPhone, birthday, locationSharing) },
                        enabled = displayName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Simpan Perubahan Profil")
                    }
                }
            }
        }

        item {
            ThemeSelectorCardSection(
                currentTheme = currentTheme,
                onSelectTheme = onSelectTheme
            )
        }

        if (currentMember?.isAdmin == true) {
            item {
                Button(
                    onClick = onOpenAdminPanel,
                    colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("open_admin_panel_button")
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buka Panel Admin Keluarga")
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SkyBlueContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = SkyBlueDark)
                        Text("Pemulihan Akun & Keamanan", style = MaterialTheme.typography.titleMedium, color = SkyBlueDark)
                    }
                    Text(
                        text = if (isLocalTrialMode) {
                            "Anda sedang menggunakan Mode Uji Coba Lokal (Room SQLite) tanpa akun Google. Data tersimpan aman di perangkat ini saja."
                        } else {
                            "Akun Anda terhubung dengan Google ($userEmail). Untuk memulihkan akun di perangkat baru, cukup login ulang dengan akun Google ini."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyRed),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sign_out_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLocalTrialMode) "Keluar dari Mode Uji Coba" else "Keluar dari Akun")
            }
        }
    }
}

@Composable
fun AdminPanelScreen(
    family: Family?,
    currentUserId: String,
    members: List<FamilyMember>,
    groups: List<FamilyGroup>,
    onUpdateFamilyName: (String) -> Unit,
    onRegenerateInvite: () -> Unit,
    onUpdateMemberRole: (targetUid: String, role: String, fundManager: Boolean) -> Unit,
    onRemoveMember: (String) -> Unit,
    onCreateGroup: (name: String, description: String, category: String, memberUids: List<String>) -> Unit,
    onDeleteGroup: (String) -> Unit
) {
    var familyNameInput by remember(family?.name) { mutableStateOf(family?.name ?: "") }
    var showCreateGroup by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Panel Admin Keluarga", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Kontrol penuh pengelolaan keluarga, hak akses peran, kode undangan, dan grup (tervalidasi di Firestore Security Rules).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pengaturan Keluarga & Kode Undangan", style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = familyNameInput,
                            onValueChange = { familyNameInput = it },
                            label = { Text("Nama Keluarga") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { onUpdateFamilyName(familyNameInput) }, enabled = familyNameInput.length >= 2) {
                            Text("Simpan")
                        }
                    }
                    Surface(color = SkyBlueContainer, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Kode Undangan Aktif", style = MaterialTheme.typography.labelMedium, color = SkyBlueDark)
                                Text(family?.inviteCode ?: "-", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SkyBlueDark)
                            }
                            OutlinedButton(onClick = onRegenerateInvite) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buat Baru")
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Kelola Grup Keluarga (${groups.size})", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { showCreateGroup = true }, colors = ButtonDefaults.buttonColors(containerColor = WarmOrange)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buat Grup")
                }
            }
        }
        items(groups, key = { "adm_grp_${it.id}" }) { group ->
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = SkyBluePrimary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(group.name, style = MaterialTheme.typography.titleMedium)
                        Text("${group.category} • ${group.memberUids.size} anggota", style = MaterialTheme.typography.labelMedium)
                    }
                    IconButton(onClick = { onDeleteGroup(group.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Grup", tint = EmergencyRed)
                    }
                }
            }
        }

        item { SectionTitleRow(title = "Hak Akses Anggota (${members.size})") }
        items(members, key = { "adm_mem_${it.userId}" }) { member ->
            val isSelf = member.userId == currentUserId
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FamilyAvatar(name = member.displayName, colorIndex = member.avatarColorIndex, size = 44.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${member.displayName} (${member.relationship})", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Peran: ${if (member.isAdmin) "Admin" else "Member"} • Bendahara: ${if (member.fundManager) "Ya" else "Tidak"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!isSelf && member.userId != family?.adminUid) {
                            IconButton(onClick = { onRemoveMember(member.userId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Keluarkan", tint = EmergencyRed)
                            }
                        }
                    }
                    if (!isSelf) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = member.isAdmin,
                                onClick = {
                                    val newRole = if (member.isAdmin) "member" else "admin"
                                    onUpdateMemberRole(member.userId, newRole, member.fundManager)
                                },
                                label = { Text(if (member.isAdmin) "Admin Aktif" else "Jadikan Admin") }
                            )
                            FilterChip(
                                selected = member.fundManager,
                                onClick = {
                                    onUpdateMemberRole(member.userId, member.role, !member.fundManager)
                                },
                                label = { Text(if (member.fundManager) "Pengelola Dana" else "Beri Akses Kas") }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = FamilyMintContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Keamanan Backend Aktif", style = MaterialTheme.typography.titleMedium, color = FamilyMint)
                    Text(
                        "• Hanya Admin yang dapat membuat, mengedit, atau menghapus Grup Keluarga.\n" +
                            "• Member tidak dapat mempromosikan diri sendiri menjadi Admin atau Pengelola Dana.\n" +
                            "• Hanya Admin & Pengelola Dana yang dapat menulis/menghapus Kas Keluarga.\n" +
                            "• Profil privat (/users/{uid}) hanya dapat diakses oleh pemilik akun.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    if (showCreateGroup) {
        GroupEditorDialog(
            title = "Buat Grup Keluarga (Admin)",
            initialName = "",
            initialDescription = "",
            initialCategory = "Keluarga Besar",
            initialMemberUids = members.map { it.userId },
            allMembers = members,
            onDismiss = { showCreateGroup = false },
            onConfirm = { name, desc, cat, uids ->
                onCreateGroup(name, desc, cat, uids)
                showCreateGroup = false
            }
        )
    }
}
