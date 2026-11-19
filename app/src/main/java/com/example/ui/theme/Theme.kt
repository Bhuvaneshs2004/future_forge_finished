package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val FutureForgeColorScheme = darkColorScheme(
    primary = EtherealMist,
    onPrimary = MidnightIndigo,
    primaryContainer = SmokyViolet,
    onPrimaryContainer = LuminousPearl,
    secondary = DustyLavender,
    onSecondary = MidnightIndigo,
    secondaryContainer = MutedMauveSlate,
    onSecondaryContainer = MoonlitSilver,
    tertiary = GlacierBlueGlow,
    onTertiary = MidnightIndigo,
    background = DarkDustyMauve,
    onBackground = LuminousPearl,
    surface = MidnightIndigo,
    onSurface = MoonlitSilver,
    surfaceVariant = ElevatedScholarSurface,
    onSurfaceVariant = MoonlitSilver,
    surfaceContainer = ElevatedScholarSurface,
    surfaceContainerHigh = SmokyViolet,
    surfaceContainerLow = DeepObsidianPlum,
    outline = DustyLavender.copy(alpha = 0.45f),
    outlineVariant = SmokyViolet.copy(alpha = 0.65f),
    error = AlertRose,
    onError = MidnightIndigo
)

val FutureForgeShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FutureForgeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FutureForgeColorScheme,
        typography = FutureForgeTypography,
        shapes = FutureForgeShapes,
        content = content
    )
}

@Composable
fun ForgeAtmosphericBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkDustyMauve.copy(alpha = 0.42f),
                        MidnightIndigo,
                        DeepObsidianPlum
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            DarkDustyMauve.copy(alpha = 0.34f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.08f),
                        radius = size.maxDimension * 0.48f
                    ),
                    radius = size.maxDimension * 0.48f,
                    center = Offset(size.width * 0.85f, size.height * 0.08f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SmokyViolet.copy(alpha = 0.32f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.72f),
                        radius = size.maxDimension * 0.45f
                    ),
                    radius = size.maxDimension * 0.45f,
                    center = Offset(size.width * 0.15f, size.height * 0.72f)
                )
            },
        content = content
    )
}
