package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
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
fun AdminDashboardScreen(
    adminUser: UserEntity,
    allUsers: List<UserEntity>,
    companies: List<CompanyProfileEntity>,
    opportunities: List<JobOpportunityEntity>,
    videos: List<VideoItemEntity>,
    reports: List<ContentReportEntity>,
    onVerifyUser: (UserEntity, String) -> Unit,
    onToggleSuspendUser: (UserEntity) -> Unit,
    onVerifyCompany: (CompanyProfileEntity, Boolean) -> Unit,
    onModerateOpportunity: (JobOpportunityEntity, Boolean, String) -> Unit,
    onToggleVideoApproval: (VideoItemEntity) -> Unit,
    onResolveReport: (ContentReportEntity, removeContent: Boolean) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Analytics, 1=Verifications, 2=Moderation & Reports, 3=Users

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
                        Text("Administrator Governance", style = MaterialTheme.typography.headlineMedium, color = LuminousPearl)
                        Text("${adminUser.name} • Trust, Verification & Analytics", style = MaterialTheme.typography.bodySmall, color = GlacierBlueGlow)
                    }
                }
                IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout")) {
                    Icon(Icons.Filled.Logout, contentDescription = "Sign Out", tint = AlertRose)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ForgePillChip("Analytics", selectedTab == 0, { selectedTab = 0 })
                ForgePillChip("Verifications", selectedTab == 1, { selectedTab = 1 })
                ForgePillChip("Moderation (${reports.count { it.status == "OPEN" }})", selectedTab == 2, { selectedTab = 2 })
                ForgePillChip("Users (${allUsers.size})", selectedTab == 3, { selectedTab = 3 })
            }
        }

        when (selectedTab) {
            0 -> {
                val studentsCount = allUsers.count { it.role == UserRole.STUDENT.name }
                val mentorsCount = allUsers.count { it.role == UserRole.PROFESSIONAL.name }
                val recruitersCount = allUsers.count { it.role == UserRole.RECRUITER.name }
                val verifiedRatio = if (opportunities.isEmpty()) 0f else opportunities.count { it.verified }.toFloat() / opportunities.size

                item {
                    ForgeCard {
                        Text("Platform Data Quality & Ecosystem Telemetry", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Spacer(Modifier.height(10.dp))
                        ForgeBarChart(
                            bars = listOf(
                                "VerifiedJobs" to verifiedRatio,
                                "ApprovedVid" to (if (videos.isEmpty()) 1f else videos.count { it.isApproved }.toFloat() / videos.size),
                                "VerifiedCo" to (if (companies.isEmpty()) 1f else companies.count { it.verified }.toFloat() / companies.size)
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Accounts: $studentsCount Students • $mentorsCount Mentors • $recruitersCount Recruiters", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                        Text("Open Moderation Reports: ${reports.count { it.status == "OPEN" }}", style = MaterialTheme.typography.bodySmall, color = CautionAmber)
                    }
                }
            }

            1 -> {
                item { Text("Pending & Verified Professionals / Recruiters", style = MaterialTheme.typography.titleMedium, color = LuminousPearl) }
                items(allUsers.filter { it.role == UserRole.PROFESSIONAL.name || it.role == UserRole.RECRUITER.name }, key = { it.id }) { u ->
                    ForgeCard {
                        Text("${u.name} (${u.role})", style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                        Text("${u.headline} • Status: ${u.verificationStatus}", style = MaterialTheme.typography.bodySmall, color = if (u.verificationStatus == "VERIFIED") VerifiedEmerald else CautionAmber)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onVerifyUser(u, "VERIFIED") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = VerifiedEmerald, contentColor = MidnightIndigo)
                            ) { Text("Verify") }
                            OutlinedButton(
                                onClick = { onVerifyUser(u, "REJECTED") },
                                modifier = Modifier.weight(1f)
                            ) { Text("Reject") }
                        }
                    }
                }
                item { Text("Company Verification", style = MaterialTheme.typography.titleMedium, color = LuminousPearl) }
                items(companies, key = { it.id }) { comp ->
                    ForgeCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(comp.name, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                                Text("${comp.industry} • Verified: ${comp.verified}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                            }
                            Switch(checked = comp.verified, onCheckedChange = { onVerifyCompany(comp, it) })
                        }
                    }
                }
            }

            2 -> {
                item { Text("Dispute & Content Reports", style = MaterialTheme.typography.titleMedium, color = LuminousPearl) }
                if (reports.isEmpty()) {
                    item { ForgeCard { Text("No active reports or review disputes.", style = MaterialTheme.typography.bodySmall) } }
                } else {
                    items(reports, key = { it.id }) { rep ->
                        ForgeCard(borderColor = if (rep.status == "OPEN") AlertRose else SmokyViolet) {
                            Text("${rep.targetType} • ${rep.status}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                            Text(rep.targetTitle, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                            Text("Reason: ${rep.reason}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                            if (rep.status == "OPEN") {
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onResolveReport(rep, true) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = AlertRose, contentColor = MidnightIndigo)
                                    ) { Text("Remove Content") }
                                    OutlinedButton(
                                        onClick = { onResolveReport(rep, false) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Dismiss") }
                                }
                            }
                        }
                    }
                }

                item { Text("Vacancy & Video Moderation", style = MaterialTheme.typography.titleMedium, color = LuminousPearl) }
                items(opportunities, key = { it.id }) { opp ->
                    ForgeCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(opp.title, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                                Text("${opp.companyName} • Status: ${opp.hiringStatus}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                            }
                            OutlinedButton(onClick = {
                                val nextStatus = if (opp.hiringStatus == "REMOVED") "OPEN" else "REMOVED"
                                onModerateOpportunity(opp, opp.verified, nextStatus)
                            }) { Text(if (opp.hiringStatus == "REMOVED") "Restore" else "Remove") }
                        }
                    }
                }
                items(videos, key = { it.id }) { vid ->
                    ForgeCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(vid.title, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                                Text("${vid.creatorName} • Approved: ${vid.isApproved}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                            }
                            Switch(checked = vid.isApproved, onCheckedChange = { onToggleVideoApproval(vid) })
                        }
                    }
                }
            }

            3 -> {
                items(allUsers, key = { it.id }) { u ->
                    ForgeCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(u.name, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                                Text("${u.email} • ${u.role}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                                Text("Suspended: ${u.suspended}", style = MaterialTheme.typography.labelSmall, color = if (u.suspended) AlertRose else VerifiedEmerald)
                            }
                            if (u.id != adminUser.id) {
                                OutlinedButton(onClick = { onToggleSuspendUser(u) }) {
                                    Text(if (u.suspended) "Reinstate" else "Suspend")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
