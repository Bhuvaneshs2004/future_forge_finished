package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun MentorDashboardScreen(
    user: UserEntity,
    mentorships: List<MentorshipEntity>,
    qnaList: List<ProfessionalQnaEntity>,
    videos: List<VideoItemEntity>,
    liveSessions: List<LiveSessionEntity>,
    onUpdateProfile: (UserEntity) -> Unit,
    onUpdateMentorship: (MentorshipEntity, status: String, feedback: String) -> Unit,
    onAnswerQuestion: (ProfessionalQnaEntity, String) -> Unit,
    onPublishVideo: (title: String, desc: String, category: String, industry: String, url: String, daySummary: String) -> Unit,
    onHostLiveSession: (title: String, topic: String, schedule: String, desc: String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Mentorship, 1=Q&A, 2=Upload Video & Live, 3=Profile
    var activePlayerVideo by remember { mutableStateOf<VideoItemEntity?>(null) }

    var answerDrafts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var feedbackDrafts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Video upload form
    var vidTitle by remember { mutableStateOf("") }
    var vidDesc by remember { mutableStateOf("") }
    var vidCat by remember { mutableStateOf("DAY_IN_CAREER") }
    var vidIndustry by remember { mutableStateOf("Technology & Engineering") }
    var vidUrl by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4") }
    var vidDaySummary by remember { mutableStateOf("") }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) vidUrl = uri.toString()
    }

    // Live webinar form
    var liveTitle by remember { mutableStateOf("") }
    var liveTopic by remember { mutableStateOf("") }
    var liveSchedule by remember { mutableStateOf("Fri, Nov 27 • 6:30 PM") }
    var liveDesc by remember { mutableStateOf("") }

    // Profile fields
    var headline by remember(user) { mutableStateOf(user.headline) }
    var history by remember(user) { mutableStateOf(user.employmentHistory) }
    var expertise by remember(user) { mutableStateOf(user.expertise) }
    var availability by remember(user) { mutableStateOf(user.availability) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ForgeBrandMark(size = 40.dp)
                    Column {
                        Text("Mentor & Expert Portal", style = MaterialTheme.typography.headlineMedium, color = LuminousPearl)
                        Text("${user.name} • Status: ${user.verificationStatus}", style = MaterialTheme.typography.bodySmall, color = if (user.verificationStatus == "VERIFIED") VerifiedEmerald else CautionAmber)
                    }
                }
                IconButton(onClick = onLogout, modifier = Modifier.testTag("mentor_logout")) {
                    Icon(Icons.Filled.Logout, contentDescription = "Sign Out", tint = AlertRose)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ForgePillChip("Mentorship", selectedTab == 0, { selectedTab = 0 })
                ForgePillChip("Ask a Pro Q&A", selectedTab == 1, { selectedTab = 1 })
                ForgePillChip("Videos & Webinars", selectedTab == 2, { selectedTab = 2 })
                ForgePillChip("Expert Profile", selectedTab == 3, { selectedTab = 3 })
            }
        }

        when (selectedTab) {
            0 -> {
                val myRequests = mentorships.filter { it.mentorId == user.id }
                item {
                    ForgeCard {
                        Text("Incoming Mentorship & Mock Interview Requests (${myRequests.size})", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                    }
                }
                if (myRequests.isEmpty()) {
                    item {
                        ForgeCard { Text("No student mentorship requests assigned yet.", style = MaterialTheme.typography.bodySmall) }
                    }
                } else {
                    items(myRequests, key = { it.id }) { m ->
                        val fb = feedbackDrafts[m.id] ?: m.feedback
                        ForgeCard {
                            Text("${if (m.isMockInterview) "MOCK INTERVIEW" else "MENTORSHIP"} • ${m.status}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                            Text("${m.studentName} — ${m.topic}", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                            Text("Requested Slot: ${m.preferredSlot}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = fb,
                                onValueChange = { feedbackDrafts = feedbackDrafts + (m.id to it) },
                                label = { Text("Session Notes / Candidate Feedback") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onUpdateMentorship(m, "SCHEDULED", fb) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                                ) { Text("Schedule") }
                                Button(
                                    onClick = { onUpdateMentorship(m, "COMPLETED", fb) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald, contentColor = MidnightIndigo)
                                ) { Text("Complete") }
                                OutlinedButton(
                                    onClick = { onUpdateMentorship(m, "DECLINED", fb) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Decline") }
                            }
                        }
                    }
                }
            }

            1 -> {
                items(qnaList, key = { it.id }) { q ->
                    val draft = answerDrafts[q.id] ?: q.answerText
                    ForgeCard {
                        Text("${q.category} • Asked by ${q.authorName}", style = MaterialTheme.typography.labelSmall, color = EtherealMist)
                        Text(q.question, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { answerDrafts = answerDrafts + (q.id to it) },
                            label = { Text("Your Expert Answer") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { onAnswerQuestion(q, draft) },
                            enabled = draft.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                        ) { Text(if (q.answerText.isBlank()) "Publish Answer" else "Update Answer") }
                    }
                }
            }

            2 -> {
                item {
                    ForgeCard {
                        Text("Publish Authorized Educational / Day in My Career Video", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("DAY_IN_CAREER", "EDUCATIONAL", "INDUSTRY_EXPERT", "CAREER_GUIDANCE", "INTERVIEW", "RECORDED_WEBINAR").forEach { c ->
                                ForgePillChip(c.replace("_", " "), vidCat == c, { vidCat = c })
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = vidTitle, onValueChange = { vidTitle = it }, label = { Text("Video Title") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = vidIndustry, onValueChange = { vidIndustry = it }, label = { Text("Industry") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = vidDesc, onValueChange = { vidDesc = it }, label = { Text("Description") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = vidDaySummary, onValueChange = { vidDaySummary = it }, label = { Text("Day in My Career / Key Takeaways (optional)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = vidUrl, onValueChange = { vidUrl = it }, label = { Text("Video Stream URL or Picked Media URI") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Pick Local Video") }
                            Button(
                                onClick = {
                                    onPublishVideo(vidTitle, vidDesc, vidCat, vidIndustry, vidUrl, vidDaySummary)
                                    vidTitle = ""
                                    vidDesc = ""
                                    vidDaySummary = ""
                                },
                                enabled = vidTitle.isNotBlank() && vidUrl.isNotBlank(),
                                modifier = Modifier.weight(1f).testTag("publish_video_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                            ) { Text("Publish Video") }
                        }
                    }
                }

                item {
                    ForgeCard {
                        Text("Host Live Expert Session / Webinar", style = MaterialTheme.typography.titleMedium, color = GlacierBlueGlow)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = liveTitle, onValueChange = { liveTitle = it }, label = { Text("Webinar Title") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = liveTopic, onValueChange = { liveTopic = it }, label = { Text("Topic") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = liveSchedule, onValueChange = { liveSchedule = it }, label = { Text("Date & Time") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = liveDesc, onValueChange = { liveDesc = it }, label = { Text("Session Agenda") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onHostLiveSession(liveTitle, liveTopic, liveSchedule, liveDesc)
                                liveTitle = ""
                                liveTopic = ""
                                liveDesc = ""
                            },
                            enabled = liveTitle.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SmokyViolet, contentColor = LuminousPearl)
                        ) { Text("Schedule Live Session") }
                    }
                }

                items(videos.filter { it.creatorId == user.id }, key = { it.id }) { vid ->
                    ForgeCard(onClick = { activePlayerVideo = vid }) {
                        Text("${vid.category} • ${vid.industry}", style = MaterialTheme.typography.labelSmall, color = EtherealMist)
                        Text(vid.title, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                        Text("Tap to verify playback", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                    }
                }
            }

            3 -> {
                item {
                    ForgeCard {
                        Text("Professional Credentials & Availability", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = headline, onValueChange = { headline = it }, label = { Text("Role & Organization") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = history, onValueChange = { history = it }, label = { Text("Employment History") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = expertise, onValueChange = { expertise = it }, label = { Text("Industries & Expertise") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = availability, onValueChange = { availability = it }, label = { Text("Mentorship Availability") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onUpdateProfile(
                                    user.copy(
                                        headline = headline,
                                        employmentHistory = history,
                                        expertise = expertise,
                                        availability = availability
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                        ) { Text("Save Professional Profile") }
                    }
                }
            }
        }
    }

    activePlayerVideo?.let { vid ->
        VideoPlayerDialog(
            title = vid.title,
            creatorLabel = vid.creatorName,
            videoUrl = vid.videoUrl,
            resumePositionMs = 0L,
            onSaveProgress = { _, _ -> },
            onDismiss = { activePlayerVideo = null }
        )
    }
}
