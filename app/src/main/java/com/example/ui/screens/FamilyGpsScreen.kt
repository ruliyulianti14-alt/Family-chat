package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FamilyGroup
import com.example.data.model.FamilyMember
import com.example.ui.components.EmptyFamilyState
import com.example.ui.components.FamilyAvatar
import com.example.ui.components.RoleBadge
import com.example.ui.components.SectionTitleRow
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.FamilyMint
import com.example.ui.theme.FamilyMintContainer
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.WarmOrangeContainer
import com.example.util.FamilyGpsHelper
import com.example.util.GpsReading
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FamilyGpsScreen(
    currentMember: FamilyMember?,
    members: List<FamilyMember>,
    groups: List<FamilyGroup>,
    onUpdateOwnLocation: (lat: Double?, lng: Double?, sharingEnabled: Boolean, silent: Boolean) -> Unit,
    onShareGpsToGroup: (group: FamilyGroup, lat: Double, lng: Double, address: String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(FamilyGpsHelper.hasLocationPermission(context)) }
    var deviceGpsEnabled by remember { mutableStateOf(FamilyGpsHelper.isDeviceLocationEnabled(context)) }
    var hardwareGpsEnabled by remember { mutableStateOf(FamilyGpsHelper.isHardwareGpsProviderEnabled(context)) }
    var sharingConsent by remember(currentMember?.locationSharingEnabled) {
        mutableStateOf(currentMember?.locationSharingEnabled ?: false)
    }
    var liveTracking by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    var latestReading by remember { mutableStateOf<GpsReading?>(null) }
    var resolvedAddress by remember { mutableStateOf<String?>(null) }
    var gpsError by remember { mutableStateOf<String?>(null) }
    var showShareGroupDialog by remember { mutableStateOf(false) }

    val activeLat = latestReading?.latitude ?: currentMember?.lastLat
    val activeLng = latestReading?.longitude ?: currentMember?.lastLng

    fun refreshStatusFlags() {
        hasPermission = FamilyGpsHelper.hasLocationPermission(context)
        deviceGpsEnabled = FamilyGpsHelper.isDeviceLocationEnabled(context)
        hardwareGpsEnabled = FamilyGpsHelper.isHardwareGpsProviderEnabled(context)
    }

    fun captureSingleFix(saveToFamily: Boolean) {
        refreshStatusFlags()
        if (!hasPermission || !deviceGpsEnabled) return
        scope.launch {
            isFetching = true
            gpsError = null
            val res = FamilyGpsHelper.fetchCurrentHighAccuracyFix(context)
            res.onSuccess { reading ->
                latestReading = reading
                resolvedAddress = FamilyGpsHelper.reverseGeocode(context, reading.latitude, reading.longitude)
                if (saveToFamily) {
                    sharingConsent = true
                    onUpdateOwnLocation(reading.latitude, reading.longitude, true, false)
                }
            }.onFailure { err ->
                gpsError = err.localizedMessage ?: "Gagal membaca koordinat GPS."
            }
            isFetching = false
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasPermission = granted
        refreshStatusFlags()
        if (granted) {
            sharingConsent = true
            captureSingleFix(saveToFamily = true)
        } else {
            gpsError = "Izin lokasi ditolak. Aktifkan izin lokasi di pengaturan aplikasi untuk menggunakan GPS."
        }
    }

    LaunchedEffect(liveTracking, hasPermission, deviceGpsEnabled) {
        if (liveTracking && hasPermission && deviceGpsEnabled) {
            FamilyGpsHelper.observeLiveGpsUpdates(context, intervalMs = 4000L)
                .catch { err ->
                    gpsError = err.localizedMessage ?: "Pelacakan GPS langsung terhenti."
                    liveTracking = false
                }
                .collect { reading ->
                    latestReading = reading
                    gpsError = null
                    resolvedAddress = FamilyGpsHelper.reverseGeocode(context, reading.latitude, reading.longitude)
                    if (sharingConsent) {
                        onUpdateOwnLocation(reading.latitude, reading.longitude, true, true)
                    }
                }
        }
    }

    val membersWithLocation = remember(members, activeLat, activeLng) {
        members.filter { it.locationSharingEnabled && it.lastLat != null && it.lastLng != null }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. GPS Header & Status Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SkyBluePrimary, SkyBlueDark)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "GPS & Lokasi Keluarga",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Pantau posisi aman & bagikan koordinat GPS atas persetujuan Anda",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasPermission && deviceGpsEnabled) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                                    contentDescription = "Status GPS",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Status badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RoleBadge(
                                label = if (hasPermission) "IZIN GPS AKTIF" else "BUTUH IZIN GPS",
                                containerColor = if (hasPermission) FamilyMintContainer else EmergencyRedContainer,
                                contentColor = if (hasPermission) FamilyMint else EmergencyRed
                            )
                            RoleBadge(
                                label = if (deviceGpsEnabled) {
                                    if (hardwareGpsEnabled) "SATELIT GPS ON" else "LOKASI JARINGAN ON"
                                } else {
                                    "GPS PERANGKAT OFF"
                                },
                                containerColor = if (deviceGpsEnabled) SkyBlueContainer else WarmOrangeContainer,
                                contentColor = if (deviceGpsEnabled) SkyBlueDark else WarmOrange
                            )
                        }

                        if (!hasPermission) {
                            Button(
                                onClick = {
                                    permLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gps_request_permission_button")
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Izinkan Akses GPS Presisi")
                            }
                        }

                        if (!deviceGpsEnabled) {
                            OutlinedButton(
                                onClick = { FamilyGpsHelper.openLocationSettings(context) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gps_open_settings_button")
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = WarmOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Aktifkan GPS di Pengaturan Perangkat", color = WarmOrange)
                            }
                        }

                        // Opt-in Sharing Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bagikan Lokasi ke Keluarga", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Anggota keluarga hanya melihat koordinat jika sakelar ini aktif.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = sharingConsent,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        if (!hasPermission) {
                                            permLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        } else {
                                            sharingConsent = true
                                            captureSingleFix(saveToFamily = true)
                                        }
                                    } else {
                                        sharingConsent = false
                                        liveTracking = false
                                        onUpdateOwnLocation(null, null, false, false)
                                    }
                                },
                                modifier = Modifier.testTag("gps_sharing_switch")
                            )
                        }

                        // Real-time tracking toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Lacak GPS Real-Time", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Perbarui koordinat otomatis setiap 4 detik saat layar ini terbuka.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = liveTracking,
                                onCheckedChange = { wantLive ->
                                    refreshStatusFlags()
                                    if (wantLive) {
                                        if (!hasPermission) {
                                            permLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        } else if (!deviceGpsEnabled) {
                                            gpsError = "Aktifkan layanan lokasi perangkat terlebih dahulu."
                                        } else {
                                            sharingConsent = true
                                            liveTracking = true
                                        }
                                    } else {
                                        liveTracking = false
                                    }
                                },
                                modifier = Modifier.testTag("gps_live_tracking_switch")
                            )
                        }

                        if (gpsError != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = gpsError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                refreshStatusFlags()
                                if (!hasPermission) {
                                    permLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                } else if (!deviceGpsEnabled) {
                                    FamilyGpsHelper.openLocationSettings(context)
                                } else {
                                    captureSingleFix(saveToFamily = sharingConsent || true)
                                }
                            },
                            enabled = !isFetching,
                            colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("gps_refresh_button")
                        ) {
                            if (isFetching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mengambil Sinyal GPS...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ambil & Perbarui Lokasi GPS Sekarang")
                            }
                        }
                    }
                }
            }
        }

        // 2. Current Coordinates & Telemetry Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SkyBluePrimary)
                        Text("Koordinat GPS Anda", style = MaterialTheme.typography.titleLarge)
                    }

                    if (activeLat != null && activeLng != null) {
                        Surface(
                            color = SkyBlueContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.US, "Lat: %.6f  •  Lng: %.6f", activeLat, activeLng),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SkyBlueDark,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!resolvedAddress.isNullOrBlank()) {
                                    Text(
                                        text = resolvedAddress!!,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                latestReading?.let { reading ->
                                    val speedKmh = (reading.speedMetersPerSec ?: 0f) * 3.6f
                                    val altText = reading.altitudeMeters?.let { String.format(Locale.US, " • Elevasi %.0f m", it) } ?: ""
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "Akurasi ±%.0f m • Kecepatan %.1f km/jam%s",
                                            reading.accuracyMeters,
                                            speedKmh,
                                            altText
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    FamilyGpsHelper.openInMaps(
                                        context,
                                        activeLat,
                                        activeLng,
                                        currentMember?.displayName ?: "Lokasi Saya"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("gps_open_maps_button")
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka Peta", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (groups.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { showShareGroupDialog = true },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("gps_share_to_group_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kirim ke Grup", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Belum ada koordinat GPS tersimpan. Tekan \"Ambil & Perbarui Lokasi GPS Sekarang\" untuk mendeteksi posisi Anda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Family GPS Radar Canvas
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = WarmOrange)
                        Text("Radar Posisi Keluarga", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = "Visualisasi arah & jarak relatif anggota keluarga yang aktif membagikan GPS.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    GpsFamilyRadarView(
                        centerLat = activeLat,
                        centerLng = activeLng,
                        currentUserId = currentMember?.userId ?: "",
                        membersWithLocation = membersWithLocation
                    )
                }
            }
        }

        // 4. Family Members Location List
        item {
            SectionTitleRow(title = "Lokasi Anggota Keluarga (${membersWithLocation.size}/${members.size})")
        }

        if (members.isEmpty()) {
            item {
                EmptyFamilyState(
                    icon = Icons.Default.LocationOn,
                    title = "Belum Ada Anggota",
                    description = "Undang keluarga Anda agar dapat saling memantau lokasi aman."
                )
            }
        } else {
            items(members, key = { it.userId }) { member ->
                val hasCoords = member.locationSharingEnabled && member.lastLat != null && member.lastLng != null
                val distanceLabel = if (hasCoords && activeLat != null && activeLng != null && member.userId != currentMember?.userId) {
                    val dist = FamilyGpsHelper.calculateDistanceMeters(
                        activeLat,
                        activeLng,
                        member.lastLat!!,
                        member.lastLng!!
                    )
                    FamilyGpsHelper.formatDistance(dist)
                } else null

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
                        FamilyAvatar(
                            name = member.displayName,
                            colorIndex = member.avatarColorIndex,
                            size = 46.dp
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "${member.displayName} (${member.relationship})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (hasCoords) {
                                Text(
                                    text = String.format(Locale.US, "%.5f, %.5f", member.lastLat, member.lastLng),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SkyBlueDark
                                )
                                if (distanceLabel != null) {
                                    Text(
                                        text = distanceLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = FamilyMint
                                    )
                                }
                            } else if (member.locationSharingEnabled) {
                                Text(
                                    text = "Izin lokasi aktif • Menunggu pembaruan GPS",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = "Berbagi lokasi dimatikan (Privasi aktif)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (hasCoords) {
                            OutlinedButton(
                                onClick = {
                                    FamilyGpsHelper.openInMaps(
                                        context,
                                        member.lastLat!!,
                                        member.lastLng!!,
                                        member.displayName
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = "Lihat ${member.displayName} di peta", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Peta")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShareGroupDialog && activeLat != null && activeLng != null) {
        AlertDialog(
            onDismissRequest = { showShareGroupDialog = false },
            title = { Text("Bagikan Lokasi GPS ke Grup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pilih grup tujuan untuk mengirim koordinat GPS Anda:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    groups.forEach { group ->
                        OutlinedButton(
                            onClick = {
                                onShareGpsToGroup(group, activeLat, activeLng, resolvedAddress)
                                showShareGroupDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(group.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showShareGroupDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun GpsFamilyRadarView(
    centerLat: Double?,
    centerLng: Double?,
    currentUserId: String,
    membersWithLocation: List<FamilyMember>
) {
    val refLat = centerLat ?: membersWithLocation.firstOrNull()?.lastLat
    val refLng = centerLng ?: membersWithLocation.firstOrNull()?.lastLng

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFE7F5FF), Color(0xFFD1EBFF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxRadius = (minOf(size.width, size.height) / 2f) * 0.85f

            val ringStroke = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )
            listOf(0.35f, 0.68f, 1.0f).forEach { factor ->
                drawCircle(
                    color = Color(0xFF0284C7).copy(alpha = 0.25f),
                    radius = maxRadius * factor,
                    center = Offset(cx, cy),
                    style = ringStroke
                )
            }

            // Crosshair lines
            drawLine(
                color = Color(0xFF0284C7).copy(alpha = 0.2f),
                start = Offset(cx - maxRadius, cy),
                end = Offset(cx + maxRadius, cy),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = Color(0xFF0284C7).copy(alpha = 0.2f),
                start = Offset(cx, cy - maxRadius),
                end = Offset(cx, cy + maxRadius),
                strokeWidth = 1.5.dp.toPx()
            )

            // Center dot (user or reference)
            drawCircle(
                color = Color(0xFF0284C7).copy(alpha = 0.22f),
                radius = 16.dp.toPx(),
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFF0284C7),
                radius = 7.dp.toPx(),
                center = Offset(cx, cy)
            )

            if (refLat != null && refLng != null) {
                val others = membersWithLocation.filter { it.userId != currentUserId }
                val distances = others.map {
                    FamilyGpsHelper.calculateDistanceMeters(refLat, refLng, it.lastLat!!, it.lastLng!!)
                }
                val maxDist = (distances.maxOrNull() ?: 1000f).coerceAtLeast(250f)

                others.forEachIndexed { idx, m ->
                    val dist = distances[idx]
                    val bearingDeg = FamilyGpsHelper.calculateBearingDegrees(refLat, refLng, m.lastLat!!, m.lastLng!!)
                    val rad = (bearingDeg - 90f) * (PI / 180.0)
                    val norm = (dist / maxDist).coerceIn(0.18f, 0.92f)
                    val px = cx + (maxRadius * norm * cos(rad)).toFloat()
                    val py = cy + (maxRadius * norm * sin(rad)).toFloat()

                    drawCircle(
                        color = Color(0xFFF97316).copy(alpha = 0.25f),
                        radius = 14.dp.toPx(),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = Color(0xFFF97316),
                        radius = 7.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = if (refLat != null && refLng != null) {
                        "Titik Biru: Pusat Anda • Titik Oranye: ${membersWithLocation.count { it.userId != currentUserId }} Anggota Aktif"
                    } else {
                        "Aktifkan GPS untuk memetakan radar keluarga secara real-time"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBlueDark,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}
