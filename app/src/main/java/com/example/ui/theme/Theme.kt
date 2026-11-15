package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AppThemeOption(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val previewPrimary: Color,
    val previewSecondary: Color,
    val previewTertiary: Color,
    val previewBackground: Color,
    val isDark: Boolean
) {
    SKY_WARM(
        id = "sky_warm",
        displayName = "Biru Hangat",
        subtitle = "Cerah, ramah & warna utama My Family Chat",
        previewPrimary = SkyBluePrimary,
        previewSecondary = WarmOrange,
        previewTertiary = FamilyMint,
        previewBackground = WarmWhiteBg,
        isDark = false
    ),
    EMERALD_NATURE(
        id = "emerald_nature",
        displayName = "Hutan Teduh",
        subtitle = "Hijau alami yang sejuk dan nyaman di mata",
        previewPrimary = EmeraldPrimary,
        previewSecondary = AmberSecondary,
        previewTertiary = Color(0xFF0D9488),
        previewBackground = ForestBg,
        isDark = false
    ),
    SUNSET_CORAL(
        id = "sunset_coral",
        displayName = "Senja Harmoni",
        subtitle = "Koral hangat dan nuansa kekeluargaan ceria",
        previewPrimary = CoralPrimary,
        previewSecondary = HoneySecondary,
        previewTertiary = SkyBlueDark,
        previewBackground = SunsetBg,
        isDark = false
    ),
    LAVENDER_ROYAL(
        id = "lavender_royal",
        displayName = "Lavender Elegan",
        subtitle = "Violet modern dengan sentuhan mawar lembut",
        previewPrimary = LavenderPrimary,
        previewSecondary = RoseSecondary,
        previewTertiary = FamilyMint,
        previewBackground = LavenderBg,
        isDark = false
    ),
    MIDNIGHT_DARK(
        id = "midnight_dark",
        displayName = "Malam Nyaman",
        subtitle = "Mode gelap lembut untuk bacaan malam hari",
        previewPrimary = DarkSkyPrimary,
        previewSecondary = DarkWarmOrange,
        previewTertiary = Color(0xFF4CD6AC),
        previewBackground = DarkBg,
        isDark = true
    );

    companion object {
        fun fromId(id: String?): AppThemeOption =
            entries.firstOrNull { it.id == id } ?: SKY_WARM
    }
}

private val SkyWarmColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SkyBlueContainer,
    onPrimaryContainer = OnSkyBlueContainer,
    secondary = WarmOrange,
    onSecondary = Color.White,
    secondaryContainer = WarmOrangeContainer,
    onSecondaryContainer = OnWarmOrangeContainer,
    tertiary = FamilyMint,
    onTertiary = Color.White,
    tertiaryContainer = FamilyMintContainer,
    onTertiaryContainer = OnFamilyMintContainer,
    background = WarmWhiteBg,
    onBackground = DeepNavyInk,
    surface = PureWhiteSurface,
    onSurface = DeepNavyInk,
    surfaceVariant = SoftBlueSurfaceVariant,
    onSurfaceVariant = SlateSecondaryText,
    outline = SoftOutline,
    error = EmergencyRed,
    errorContainer = EmergencyRedContainer
)

private val EmeraldNatureColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = AmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    tertiary = Color(0xFF0D9488),
    onTertiary = Color.White,
    tertiaryContainer = FamilyMintContainer,
    onTertiaryContainer = OnFamilyMintContainer,
    background = ForestBg,
    onBackground = ForestInk,
    surface = PureWhiteSurface,
    onSurface = ForestInk,
    surfaceVariant = ForestSurfaceVariant,
    onSurfaceVariant = Color(0xFF42634F),
    outline = Color(0xFFC5DFCE),
    error = EmergencyRed,
    errorContainer = EmergencyRedContainer
)

private val SunsetCoralColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralContainer,
    onPrimaryContainer = OnCoralContainer,
    secondary = HoneySecondary,
    onSecondary = Color.White,
    secondaryContainer = HoneyContainer,
    onSecondaryContainer = OnHoneyContainer,
    tertiary = SkyBlueDark,
    onTertiary = Color.White,
    tertiaryContainer = SkyBlueContainer,
    onTertiaryContainer = OnSkyBlueContainer,
    background = SunsetBg,
    onBackground = SunsetInk,
    surface = PureWhiteSurface,
    onSurface = SunsetInk,
    surfaceVariant = SunsetSurfaceVariant,
    onSurfaceVariant = Color(0xFF704A42),
    outline = Color(0xFFEBD2CA),
    error = EmergencyRed,
    errorContainer = EmergencyRedContainer
)

private val LavenderRoyalColorScheme = lightColorScheme(
    primary = LavenderPrimary,
    onPrimary = Color.White,
    primaryContainer = LavenderContainer,
    onPrimaryContainer = OnLavenderContainer,
    secondary = RoseSecondary,
    onSecondary = Color.White,
    secondaryContainer = RoseContainer,
    onSecondaryContainer = OnRoseContainer,
    tertiary = FamilyMint,
    onTertiary = Color.White,
    tertiaryContainer = FamilyMintContainer,
    onTertiaryContainer = OnFamilyMintContainer,
    background = LavenderBg,
    onBackground = LavenderInk,
    surface = PureWhiteSurface,
    onSurface = LavenderInk,
    surfaceVariant = LavenderSurfaceVariant,
    onSurfaceVariant = Color(0xFF564D75),
    outline = Color(0xFFD8D0F2),
    error = EmergencyRed,
    errorContainer = EmergencyRedContainer
)

private val MidnightDarkColorScheme = darkColorScheme(
    primary = DarkSkyPrimary,
    onPrimary = Color(0xFF002F52),
    primaryContainer = Color(0xFF0B4E80),
    onPrimaryContainer = Color(0xFFD6EEFF),
    secondary = DarkWarmOrange,
    onSecondary = Color(0xFF4D1B00),
    secondaryContainer = Color(0xFF7A320A),
    onSecondaryContainer = Color(0xFFFFE5D4),
    tertiary = Color(0xFF4CD6AC),
    onTertiary = Color(0xFF003828),
    tertiaryContainer = Color(0xFF0A523D),
    onTertiaryContainer = Color(0xFFD3F6EA),
    background = DarkBg,
    onBackground = Color(0xFFEAF2FA),
    surface = DarkSurface,
    onSurface = Color(0xFFEAF2FA),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA9BDD1),
    outline = Color(0xFF334A63),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF7F1D1D)
)

fun colorSchemeFor(option: AppThemeOption): ColorScheme = when (option) {
    AppThemeOption.SKY_WARM -> SkyWarmColorScheme
    AppThemeOption.EMERALD_NATURE -> EmeraldNatureColorScheme
    AppThemeOption.SUNSET_CORAL -> SunsetCoralColorScheme
    AppThemeOption.LAVENDER_ROYAL -> LavenderRoyalColorScheme
    AppThemeOption.MIDNIGHT_DARK -> MidnightDarkColorScheme
}

val FamilyShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun MyFamilyChatTheme(
    themeOption: AppThemeOption = AppThemeOption.SKY_WARM,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(themeOption),
        typography = FamilyTypography,
        shapes = FamilyShapes,
        content = content
    )
}
