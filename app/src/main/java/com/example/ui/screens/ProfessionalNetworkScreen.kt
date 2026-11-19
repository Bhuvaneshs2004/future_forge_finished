package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ProfessionalNetworkScreen(
    user: UserEntity,
    videos: List<VideoItemEntity>,
    watchHistory: List<VideoWatchEntity>,
    savedItems: List<SavedItemEntity>,
    mentors: List<UserEntity>,
    mentorships: List<MentorshipEntity>,
    qnaList: List<ProfessionalQnaEntity>,
    liveSessions: List<LiveSessionEntity>,
    registrations: List<SessionRegistrationEntity>,
    communityPosts: List<CommunityPostEntity>,
    onToggleSaveVideo: (VideoItemEntity) -> Unit,
    onSaveVideoProgress: (videoId: String, posMs: Long, durationMs: Long) -> Unit,
    onRequestMentorship: (mentor: UserEntity, topic: String, slot: String, isMock: Boolean) -> Unit,
    onAskQuestion: (category: String, question: String) -> Unit,
    onToggleSessionRegister: (LiveSessionEntity) -> Unit,
    onCreateCommunityPost: (category: String, title: String, content: String) -> Unit,
    onUpvotePost: (CommunityPostEntity) -> Unit,
    onReportContent: (targetType: String, targetId: String, title: String, reason: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Videos, 1=Mentors & Q&A, 2=Live Webinars, 3=Peer Community
    var videoSearch by remember { mutableStateOf("") }
    var videoCat by remember { mutableStateOf("ALL") }
    var activePlayerVideo by remember { mutableStateOf<VideoItemEntity?>(null) }

    var mentorBookingTarget by remember { mutableStateOf<UserEntity?>(null) }
    var reportTarget by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    var qaCategory by remember { mutableStateOf("Engineering Careers") }
    var qaInput by remember { mutableStateOf("") }

    var postCategory by remember { mutableStateOf("CODING") }
    var postTitle by remember { mutableStateOf("") }
    var postBody by remember { mutableStateOf("") }
    var communityFilter by remember { mutableStateOf("ALL") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(
                title = "Professional Network",
                subtitle = "Playable industry videos, mentors, live webinars & peer community"
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ForgePillChip("Video Library", selectedTab == 0, { selectedTab = 0 })
                ForgePillChip("Mentors & Q&A", selectedTab == 1, { selectedTab = 1 })
                ForgePillChip("Live Sessions", selectedTab == 2, { selectedTab = 2 })
                ForgePillChip("Peer Community", selectedTab == 3, { selectedTab = 3 })
            }
        }

        when (selectedTab) {
            0 -> {
                item {
                    OutlinedTextField(
                        value = videoSearch,
                        onValueChange = { videoSearch = it },
                        label = { Text("Search industry videos, Day in My Career, or speakers") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL", "DAY_IN_CAREER", "EDUCATIONAL", "INTERVIEW", "RECORDED_WEBINAR", "INDUSTRY_EXPERT", "CAREER_GUIDANCE").forEach { cat ->
                            ForgePillChip(cat.replace("_", " "), videoCat == cat, { videoCat = cat })
                        }
                    }
                }

                val visibleVideos = videos.filter { v ->
                    v.isApproved &&
                        (videoCat == "ALL" || v.category == videoCat) &&
                        (videoSearch.isBlank() || v.title.contains(videoSearch, true) || v.industry.contains(videoSearch, true) || v.creatorName.contains(videoSearch, true))
                }

                items(visibleVideos, key = { it.id }) { vid ->
                    val watch = watchHistory.firstOrNull { it.videoId == vid.id }
                    val isSaved = savedItems.any { it.itemId == vid.id }
                    ForgeCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text("${vid.category.replace("_", " ")} • ${vid.industry} • ${vid.durationLabel}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                                Text(vid.title, style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                                Text("${vid.creatorName} — ${vid.creatorRole}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                            }
                            IconButton(onClick = { onToggleSaveVideo(vid) }) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "Save Video",
                                    tint = if (isSaved) EtherealMist else MoonlitSilver
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(vid.description, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        if (vid.dayInCareerSummary.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Surface(shape = RoundedCornerShape(10.dp), color = DeepObsidianPlum) {
                                Text("Day in My Career: ${vid.dayInCareerSummary}", style = MaterialTheme.typography.bodySmall, color = DustyLavender, modifier = Modifier.padding(10.dp))
                            }
                        }
                        if (watch != null && watch.positionMs > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (watch.completed) "Watched" else "Resume from ${watch.positionMs / 1000}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = VerifiedEmerald
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { activePlayerVideo = vid },
                            modifier = Modifier.fillMaxWidth().testTag("play_video_${vid.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if ((watch?.positionMs ?: 0L) > 1000L && watch?.completed != true) "Resume Video" else "Watch Video")
                        }
                    }
                }
            }

            1 -> {
                item {
                    Text("Verified Industry Mentors", style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                }
                items(mentors, key = { it.id }) { mentor ->
                    ForgeCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(mentor.name, style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                                Text(mentor.headline, style = MaterialTheme.typography.bodySmall, color = GlacierBlueGlow)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = VerifiedEmerald.copy(alpha = 0.16f)) {
                                Text("Verified", style = MaterialTheme.typography.labelSmall, color = VerifiedEmerald, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                        Text("Expertise: ${mentor.expertise}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Text("History: ${mentor.employmentHistory}", style = MaterialTheme.typography.bodySmall, color = DustyLavender)
                        Text("Availability: ${mentor.availability}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { mentorBookingTarget = mentor },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SmokyViolet, contentColor = LuminousPearl)
                        ) { Text("Request Mentorship / Mock Interview") }
                    }
                }

                if (mentorships.isNotEmpty()) {
                    item {
                        Text("My Mentorship & Mock Requests", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                    }
                    items(mentorships, key = { it.id }) { m ->
                        ForgeCard {
                            Text("${m.mentorName} • ${m.status}", style = MaterialTheme.typography.titleSmall, color = GlacierBlueGlow)
                            Text("${m.topic} (${m.preferredSlot})", style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                            if (m.feedback.isNotBlank()) {
                                Text("Mentor Note: ${m.feedback}", style = MaterialTheme.typography.bodySmall, color = VerifiedEmerald)
                            }
                        }
                    }
                }

                item {
                    ForgeCard {
                        Text("Ask a Professional (Moderated Q&A)", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = qaCategory, onValueChange = { qaCategory = it }, label = { Text("Topic Category") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = qaInput, onValueChange = { qaInput = it }, label = { Text("Ask experienced professionals a career question") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onAskQuestion(qaCategory, qaInput)
                                qaInput = ""
                            },
                            enabled = qaInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                        ) { Text("Submit Question") }
                    }
                }

                items(qnaList, key = { it.id }) { q ->
                    ForgeCard {
                        Text("${q.category} • Asked by ${q.authorName}", style = MaterialTheme.typography.labelSmall, color = EtherealMist)
                        Text(q.question, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                        Spacer(Modifier.height(6.dp))
                        if (q.answerText.isNotBlank()) {
                            Text("Answer (${q.answeredByName} — ${q.answeredByRole}):", style = MaterialTheme.typography.labelSmall, color = VerifiedEmerald)
                            Text(q.answerText, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        } else {
                            Text("Awaiting verified professional response...", style = MaterialTheme.typography.bodySmall, color = CautionAmber)
                        }
                    }
                }
            }

            2 -> {
                items(liveSessions, key = { it.id }) { session ->
                    val registered = registrations.any { it.sessionId == session.id }
                    ForgeCard {
                        Text("${session.topic} • ${session.scheduleLabel}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                        Text(session.title, style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                        Text("Host: ${session.hostName} (${session.hostTitle})", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                        Spacer(Modifier.height(4.dp))
                        Text(session.description, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        if (session.liveNotes.isNotBlank()) {
                            Text("Live Desk: ${session.liveNotes}", style = MaterialTheme.typography.bodySmall, color = DustyLavender)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onToggleSessionRegister(session) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (registered) VerifiedEmerald else EtherealMist,
                                    contentColor = MidnightIndigo
                                )
                            ) { Text(if (registered) "Registered & Reminded" else "Register + Remind") }
                            if (session.recordingUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        activePlayerVideo = VideoItemEntity(
                                            id = session.id,
                                            title = session.title,
                                            description = session.description,
                                            creatorId = session.hostId,
                                            creatorName = session.hostName,
                                            creatorRole = session.hostTitle,
                                            category = "RECORDED_WEBINAR",
                                            industry = session.topic,
                                            videoUrl = session.recordingUrl,
                                            durationLabel = "Live Replay"
                                        )
                                    }
                                ) { Text("Watch Replay") }
                            }
                        }
                    }
                }
            }

            3 -> {
                item {
                    ForgeCard(borderColor = GlacierBlueGlow.copy(alpha = 0.5f)) {
                        Text("Moderated Peer Learning Community", style = MaterialTheme.typography.titleMedium, color = GlacierBlueGlow)
                        Text("Guidelines: Respectful scholarly dialogue only. Zero spam, harassment, or unverified exam leaks. Youth-safe moderation enabled.", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                    }
                }
                item {
                    ForgeCard {
                        Text("Share Study Notes, Doubt Tips, or Group Challenge", style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("CODING", "EXAM_PREP", "CAREER_ADVICE", "CHALLENGE", "STUDY_NOTES").forEach { cat ->
                                ForgePillChip(cat.replace("_", " "), postCategory == cat, { postCategory = cat })
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = postTitle, onValueChange = { postTitle = it }, label = { Text("Post Title") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = postBody, onValueChange = { postBody = it }, label = { Text("Share insight or resource") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onCreateCommunityPost(postCategory, postTitle, postBody)
                                postTitle = ""
                                postBody = ""
                            },
                            enabled = postTitle.isNotBlank() && postBody.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                        ) { Text("Publish to Peer Community") }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL", "CODING", "EXAM_PREP", "CAREER_ADVICE", "CHALLENGE", "STUDY_NOTES").forEach { cat ->
                            ForgePillChip(cat.replace("_", " "), communityFilter == cat, { communityFilter = cat })
                        }
                    }
                }
                items(communityPosts.filter { communityFilter == "ALL" || it.category == communityFilter }, key = { it.id }) { post ->
                    ForgeCard {
                        Text("${post.category.replace("_", " ")} • ${post.authorName}", style = MaterialTheme.typography.labelSmall, color = EtherealMist)
                        Text(post.title, style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Text(post.content, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { onUpvotePost(post) }) {
                                Icon(Icons.Filled.ThumbUp, contentDescription = "Helpful", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Helpful (${post.likesCount})")
                            }
                            TextButton(onClick = { reportTarget = Triple("COMMUNITY_POST", post.id, post.title) }) {
                                Text("Report", color = AlertRose)
                            }
                        }
                    }
                }
            }
        }
    }

    activePlayerVideo?.let { vid ->
        val resumeMs = watchHistory.firstOrNull { it.videoId == vid.id }?.positionMs ?: 0L
        VideoPlayerDialog(
            title = vid.title,
            creatorLabel = "${vid.creatorName} • ${vid.creatorRole}",
            videoUrl = vid.videoUrl,
            resumePositionMs = resumeMs,
            onSaveProgress = { pos, dur -> onSaveVideoProgress(vid.id, pos, dur) },
            onDismiss = { activePlayerVideo = null }
        )
    }

    mentorBookingTarget?.let { mentor ->
        var topic by remember { mutableStateOf("Career & Roadmap Guidance") }
        var slot by remember { mutableStateOf(mentor.availability.ifBlank { "Thu 6:30 PM" }) }
        var isMock by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { mentorBookingTarget = null },
            containerColor = MidnightIndigo,
            title = { Text("Request Session: ${mentor.name}", color = LuminousPearl) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isMock, onCheckedChange = { isMock = it })
                        Text("Professional Mock Interview", color = MoonlitSilver)
                    }
                    OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Discussion Topic") })
                    OutlinedTextField(value = slot, onValueChange = { slot = it }, label = { Text("Preferred Time Slot") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    onRequestMentorship(mentor, topic, slot, isMock)
                    mentorBookingTarget = null
                }) { Text("Send Request") }
            },
            dismissButton = { TextButton(onClick = { mentorBookingTarget = null }) { Text("Cancel") } }
        )
    }

    reportTarget?.let { (type, id, title) ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reportTarget = null },
            containerColor = MidnightIndigo,
            title = { Text("Report Content", color = LuminousPearl) },
            text = {
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason for moderation review") })
            },
            confirmButton = {
                Button(onClick = {
                    onReportContent(type, id, title, reason)
                    reportTarget = null
                }) { Text("Submit Report") }
            },
            dismissButton = { TextButton(onClick = { reportTarget = null }) { Text("Cancel") } }
        )
    }
}
