package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*

object StudentRoutes {
    const val HOME = "home"
    const val COMPASS = "career_compass"
    const val EXAM_HUB = "exam_hub"
    const val SKILL_FORGE = "skill_forge"
    const val INTERVIEW_ARENA = "interview_arena"
    const val COMPANY_JOBS = "company_jobs"
    const val NETWORK = "professional_network"
    const val MY_PROGRESS = "my_progress"
}

@Composable
fun HomeScreen(
    user: UserEntity,
    roadmap: RoadmapEntity?,
    milestones: List<RoadmapMilestoneEntity>,
    studyTasks: List<StudyPlanTaskEntity>,
    notifications: List<NotificationEntity>,
    achievements: List<AchievementEntity>,
    onNavigate: (String) -> Unit,
    onToggleTask: (StudyPlanTaskEntity) -> Unit,
    onUpdateProfile: (UserEntity) -> Unit,
    onMarkNotificationsRead: () -> Unit,
    onLogout: () -> Unit
) {
    var showNotifications by remember { mutableStateOf(false) }
    var showProfileSettings by remember { mutableStateOf(false) }

    val unreadCount = notifications.count { !it.isRead }
    val completedMilestones = milestones.count { it.completed }
    val roadmapFraction = if (milestones.isEmpty()) 0f else completedMilestones.toFloat() / milestones.size.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ForgeBrandMark(size = 42.dp)
                    Column {
                        Text("FUTURE FORGE", style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                        Text(
                            text = stringResource(id = R.string.app_tagline),
                            style = MaterialTheme.typography.labelSmall,
                            color = EtherealMist
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { showNotifications = true },
                        modifier = Modifier.testTag("home_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = GlacierBlueGlow, contentColor = MidnightIndigo) {
                                        Text(unreadCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = LuminousPearl)
                        }
                    }
                    IconButton(
                        onClick = { showProfileSettings = true },
                        modifier = Modifier.testTag("home_profile_settings_button")
                    ) {
                        Icon(Icons.Filled.ManageAccounts, contentDescription = "Profile & Privacy Settings", tint = LuminousPearl)
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_forge_hero_banner),
                    contentDescription = "Scholar Sanctuary Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MidnightIndigo.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, DustyLavender.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "Scholar • ${user.educationLevel.ifBlank { "Career Builder" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EtherealMist,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Welcome back, ${user.name.substringBefore(" ")}",
                            style = MaterialTheme.typography.displayMedium,
                            color = LuminousPearl
                        )
                        Text(
                            text = "${achievements.sumOf { it.points }} Forge Points • ${achievements.size} Earned Badges",
                            style = MaterialTheme.typography.bodySmall,
                            color = MoonlitSilver
                        )
                    }
                }
            }
        }

        // Destiny Roadmap Summary Card
        item {
            ForgeCard(
                onClick = { onNavigate(StudentRoutes.COMPASS) },
                borderColor = EtherealMist.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ForgeProgressRing(
                        progressFraction = roadmapFraction,
                        centerLabel = "${(roadmapFraction * 100).toInt()}%"
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DESTINY ROADMAP",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlacierBlueGlow
                        )
                        Text(
                            text = roadmap?.targetCareer ?: "Forge Your Career Pathway",
                            style = MaterialTheme.typography.titleLarge,
                            color = LuminousPearl
                        )
                        Text(
                            text = if (milestones.isEmpty()) {
                                "Tap to launch AI Career Compass & generate milestones."
                            } else {
                                "$completedMilestones of ${milestones.size} milestones completed"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MoonlitSilver
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Career Compass",
                        tint = EtherealMist
                    )
                }
            }
        }

        // Unified 8-Module Command Grid
        item {
            Text(
                text = "Future Forge Ecosystem",
                style = MaterialTheme.typography.titleLarge,
                color = LuminousPearl
            )
            Spacer(Modifier.height(8.dp))
            val modules = listOf(
                ModuleTile("Career Compass", "AI Paths & Destiny Roadmap", Icons.Filled.Explore, StudentRoutes.COMPASS),
                ModuleTile("Exam Hub", "Mock Tests, Doubt AI & Planner", Icons.Filled.Quiz, StudentRoutes.EXAM_HUB),
                ModuleTile("Skill Forge", "Courses & Comm Lab", Icons.Filled.Code, StudentRoutes.SKILL_FORGE),
                ModuleTile("Interview Arena", "AI & Mentor Simulations", Icons.Filled.RecordVoiceOver, StudentRoutes.INTERVIEW_ARENA),
                ModuleTile("Company & Jobs", "Intel, Salaries & Readiness", Icons.Filled.BusinessCenter, StudentRoutes.COMPANY_JOBS),
                ModuleTile("Pro Network", "Mentors, Videos & Peer Hub", Icons.Filled.Groups, StudentRoutes.NETWORK),
                ModuleTile("My Progress", "Resume Studio & Analytics", Icons.Filled.Insights, StudentRoutes.MY_PROGRESS),
                ModuleTile("Privacy & Profile", "Skills, Goals & Security", Icons.Filled.Security, "PROFILE_MODAL")
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                modules.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { tile ->
                            ForgeCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_tile_${tile.route}"),
                                onClick = {
                                    if (tile.route == "PROFILE_MODAL") showProfileSettings = true
                                    else onNavigate(tile.route)
                                }
                            ) {
                                Icon(
                                    imageVector = tile.icon,
                                    contentDescription = tile.title,
                                    tint = EtherealMist,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(tile.title, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                                Spacer(Modifier.height(2.dp))
                                Text(tile.subtitle, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver.copy(alpha = 0.85f))
                            }
                        }
                    }
                }
            }
        }

        // Recommended Next Actions + Smart Study Planner Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Study Plan & Reminders", style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                TextButton(onClick = { onNavigate(StudentRoutes.EXAM_HUB) }) {
                    Text("Open Planner", color = GlacierBlueGlow)
                }
            }
        }

        if (studyTasks.isEmpty()) {
            item {
                ForgeCard {
                    Text("No study tasks queued yet. Schedule an exam or skill session in Exam Hub.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(studyTasks.take(3), key = { it.id }) { task ->
                ForgeCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.completed,
                            onCheckedChange = { onToggleTask(task) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                            Text(
                                text = "${task.subject} • Due ${task.dueDate} • ${task.durationMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = EtherealMist
                            )
                        }
                        if (task.reminderEnabled) {
                            Icon(Icons.Filled.Alarm, contentDescription = "Reminder Active", tint = GlacierBlueGlow, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            containerColor = MidnightIndigo,
            title = { Text("Notifications & Reminders", color = LuminousPearl) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (notifications.isEmpty()) {
                        Text("You're all caught up.", color = MoonlitSilver)
                    } else {
                        notifications.take(8).forEach { n ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ElevatedScholarSurface
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                    Text(n.title, style = MaterialTheme.typography.titleSmall, color = if (n.isRead) MoonlitSilver else GlacierBlueGlow)
                                    Text(n.message, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onMarkNotificationsRead()
                    showNotifications = false
                }) { Text("Mark All Read", color = EtherealMist) }
            }
        )
    }

    if (showProfileSettings) {
        ProfileAndPrivacyDialog(
            user = user,
            onSave = {
                onUpdateProfile(it)
                showProfileSettings = false
            },
            onLogout = {
                showProfileSettings = false
                onLogout()
            },
            onDismiss = { showProfileSettings = false }
        )
    }
}

private data class ModuleTile(val title: String, val subtitle: String, val icon: ImageVector, val route: String)

@Composable
fun ProfileAndPrivacyDialog(
    user: UserEntity,
    onSave: (UserEntity) -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    var education by remember(user) { mutableStateOf(user.educationLevel) }
    var qualifications by remember(user) { mutableStateOf(user.qualifications) }
    var interests by remember(user) { mutableStateOf(user.interests) }
    var strengths by remember(user) { mutableStateOf(user.strengths) }
    var skills by remember(user) { mutableStateOf(user.skills) }
    var ambitions by remember(user) { mutableStateOf(user.careerAmbitions) }
    var publicProfile by remember(user) { mutableStateOf(user.privacyPublicProfile) }
    var shareResume by remember(user) { mutableStateOf(user.privacyShareResume) }
    var youthSafe by remember(user) { mutableStateOf(user.privacyYouthSafeMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightIndigo,
        title = { Text("Profile, Privacy & Security", color = LuminousPearl) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text("${user.name} (${user.role})", style = MaterialTheme.typography.titleSmall, color = EtherealMist)
                        Text("Verification: ${user.verificationStatus}", style = MaterialTheme.typography.bodySmall, color = VerifiedEmerald)
                    }
                    item {
                        OutlinedTextField(value = education, onValueChange = { education = it }, label = { Text("Education Level") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = qualifications, onValueChange = { qualifications = it }, label = { Text("Qualifications") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = interests, onValueChange = { interests = it }, label = { Text("Interests") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = strengths, onValueChange = { strengths = it }, label = { Text("Strengths") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = skills, onValueChange = { skills = it }, label = { Text("Existing Skills") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = ambitions, onValueChange = { ambitions = it }, label = { Text("Career Ambitions") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        HorizontalDivider(color = SmokyViolet)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discoverable by Verified Mentors", style = MaterialTheme.typography.bodySmall)
                            Switch(checked = publicProfile, onCheckedChange = { publicProfile = it })
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Share Resume on Direct Applications", style = MaterialTheme.typography.bodySmall)
                            Switch(checked = shareResume, onCheckedChange = { shareResume = it })
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Youth & Community Safety Filter", style = MaterialTheme.typography.bodySmall)
                            Switch(checked = youthSafe, onCheckedChange = { youthSafe = it })
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth().testTag("sign_out_button"),
                            border = BorderStroke(1.dp, AlertRose.copy(alpha = 0.7f))
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = "Sign Out", tint = AlertRose, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sign Out", color = AlertRose)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        user.copy(
                            educationLevel = education,
                            qualifications = qualifications,
                            interests = interests,
                            strengths = strengths,
                            skills = skills,
                            careerAmbitions = ambitions,
                            privacyPublicProfile = publicProfile,
                            privacyShareResume = shareResume,
                            privacyYouthSafeMode = youthSafe
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
            ) { Text("Save Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = MoonlitSilver) }
        }
    )
}
