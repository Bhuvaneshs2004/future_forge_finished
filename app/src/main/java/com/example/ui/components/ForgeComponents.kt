package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ForgeBrandMark(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Image(
        painter = painterResource(id = R.drawable.img_ff_logo_icon),
        contentDescription = "Future Forge Monogram Emblem",
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.dp, DustyLavender.copy(alpha = 0.55f), CircleShape),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ForgeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = DustyLavender.copy(alpha = 0.28f),
    containerColor: Color = ElevatedScholarSurface.copy(alpha = 0.92f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SectionTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = LuminousPearl
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.headlineSmall,
                color = EtherealMist
            )
        }
    }
}

@Composable
fun AiPlaceholderNoticeBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SmokyViolet.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, GlacierBlueGlow.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = "AI Notice",
                tint = GlacierBlueGlow,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(id = R.string.ai_placeholder_notice),
                style = MaterialTheme.typography.bodySmall,
                color = LuminousPearl
            )
        }
    }
}

@Composable
fun SourceVerificationBadge(
    sourceType: String,
    sourceName: String,
    lastUpdated: String,
    modifier: Modifier = Modifier
) {
    val (label, tint) = when (sourceType) {
        "OFFICIAL_EMPLOYER" -> "Official Employer" to VerifiedEmerald
        "TRUSTED_FEED" -> "Trusted Job Feed" to GlacierBlueGlow
        else -> "Third-Party Report" to CautionAmber
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = tint.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, tint.copy(alpha = 0.55f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (sourceType == "THIRD_PARTY") Icons.Filled.Info else Icons.Filled.Verified,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint
                )
            }
        }
        Text(
            text = "$sourceName • Updated $lastUpdated",
            style = MaterialTheme.typography.bodySmall,
            color = MoonlitSilver.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ForgePillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) EtherealMist else SmokyViolet.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, if (selected) LuminousPearl else DustyLavender.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MidnightIndigo else MoonlitSilver
            )
        }
    }
}

@Composable
fun ForgeProgressRing(
    progressFraction: Float,
    centerLabel: String,
    modifier: Modifier = Modifier,
    ringSize: Dp = 74.dp
) {
    val clamped = progressFraction.coerceIn(0f, 1f)
    Box(modifier = modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            drawArc(
                color = SmokyViolet.copy(alpha = 0.55f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(EtherealMist, GlacierBlueGlow, DustyLavender, EtherealMist)),
                startAngle = -90f,
                sweepAngle = 360f * clamped,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = centerLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = LuminousPearl
        )
    }
}

@Composable
fun ForgeBarChart(
    bars: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            if (bars.isEmpty()) return@Canvas
            val barWidth = (size.width / (bars.size * 2f)).coerceAtMost(44.dp.toPx())
            val slot = size.width / bars.size
            bars.forEachIndexed { idx, (_, fraction) ->
                val clamped = fraction.coerceIn(0.05f, 1f)
                val barHeight = size.height * clamped
                val left = idx * slot + (slot - barWidth) / 2f
                drawRoundRect(
                    color = SmokyViolet.copy(alpha = 0.4f),
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(GlacierBlueGlow, EtherealMist)),
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            bars.forEach { (label, frac) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${(frac * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = EtherealMist
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MoonlitSilver,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayerDialog(
    title: String,
    creatorLabel: String,
    videoUrl: String,
    resumePositionMs: Long,
    onSaveProgress: (positionMs: Long, durationMs: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var isBuffering by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentMs by remember { mutableLongStateOf(resumePositionMs) }
    var totalMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(videoViewRef) {
        while (videoViewRef != null) {
            delay(1000)
            val vv = videoViewRef
            if (vv != null && vv.duration > 0) {
                currentMs = vv.currentPosition.toLong()
                totalMs = vv.duration.toLong()
                isPlaying = vv.isPlaying
                onSaveProgress(currentMs, totalMs)
            }
        }
    }

    Dialog(
        onDismissRequest = {
            videoViewRef?.let { vv ->
                if (vv.duration > 0) onSaveProgress(vv.currentPosition.toLong(), vv.duration.toLong())
            }
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = MidnightIndigo,
            border = BorderStroke(1.dp, DustyLavender.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                        Text(creatorLabel, style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                    }
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                if (vv.duration > 0) onSaveProgress(vv.currentPosition.toLong(), vv.duration.toLong())
                            }
                            onDismiss()
                        },
                        modifier = Modifier.testTag("close_video_player")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close Video Player", tint = MoonlitSilver)
                    }
                }
                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DeepObsidianPlum),
                    contentAlignment = Alignment.Center
                ) {
                    if (playbackError == null) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    val controller = MediaController(ctx)
                                    controller.setAnchorView(this)
                                    setMediaController(controller)
                                    setVideoURI(Uri.parse(videoUrl))
                                    setOnPreparedListener { mp ->
                                        isBuffering = false
                                        totalMs = mp.duration.toLong()
                                        if (resumePositionMs in 1000L until mp.duration.toLong()) {
                                            seekTo(resumePositionMs.toInt())
                                        }
                                        start()
                                        isPlaying = true
                                    }
                                    setOnErrorListener { _, what, extra ->
                                        isBuffering = false
                                        playbackError = "Unable to stream video (code $what/$extra). Verify internet connectivity or use a local picked video."
                                        true
                                    }
                                    setOnCompletionListener {
                                        isPlaying = false
                                        if (duration > 0) {
                                            onSaveProgress(duration.toLong(), duration.toLong())
                                        }
                                    }
                                    videoViewRef = this
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isBuffering) {
                            CircularProgressIndicator(color = EtherealMist)
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.WifiOff, contentDescription = "Video Error", tint = AlertRose)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = playbackError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MoonlitSilver
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (totalMs > 0) "Progress: ${currentMs / 1000}s / ${totalMs / 1000}s" else "Streaming Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = MoonlitSilver
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                videoViewRef?.let { vv ->
                                    if (vv.isPlaying) {
                                        vv.pause()
                                        isPlaying = false
                                    } else {
                                        vv.start()
                                        isPlaying = true
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (isPlaying) "Pause" else "Play")
                        }
                    }
                }
            }
        }
    }
}
