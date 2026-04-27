package com.sikai.learn.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.SikAiCard
import com.sikai.learn.ui.components.SikAiPageHeader
import com.sikai.learn.ui.components.SikAiSectionTitle
import com.sikai.learn.ui.components.SikAiStatusPill
import com.sikai.learn.ui.theme.SikAi

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenAiTutor: () -> Unit,
    onOpenSnap: () -> Unit,
    onOpenPapers: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenStudyPlan: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenDownloads: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val colors = SikAi.colors
    val tokens = SikAi.tokens
    val classLabel = state.profile?.classLevel?.let { "Class $it" } ?: "Welcome"

    Column(modifier = Modifier.fillMaxSize()) {
        SikAiPageHeader(
            title = classLabel,
            subtitle = "SIKAI · LEARN WITH AI",
            trailing = {
                if (state.daysToExam != null) {
                    SikAiStatusPill(text = "${state.daysToExam} days to exam")
                }
            }
        )
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = tokens.pageHorizontal, vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SikAiCard( contentPadding = 18.dp) {
                    Column {
                        Text(
                            text = "Today's focus",
                            style = SikAi.type.sectionTitle,
                            color = colors.accent
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = state.profile?.let { "Keep going on ${it.studyMinutesPerDay} mins / day" }
                                ?: "Sign in to start a streak",
                            style = SikAi.type.titleLarge,
                            color = colors.onSurface
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (state.subjectNames.isNotEmpty())
                                state.subjectNames.joinToString(" · ")
                            else "No subjects selected yet.",
                            style = SikAi.type.bodyMedium,
                            color = colors.onSurfaceMuted
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(6.dp)) }
            item { SikAiSectionTitle("Quick actions") }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickAction(
                        title = "AI Tutor",
                        subtitle = "Ask anything",
                        icon = Icons.Outlined.Chat,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenAiTutor,
                    )
                    QuickAction(
                        title = "Snap & Solve",
                        subtitle = "Photo a question",
                        icon = Icons.Outlined.CameraAlt,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSnap,
                    )
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickAction(
                        title = "Quizzes",
                        subtitle = "MCQ practice",
                        icon = Icons.Outlined.Quiz,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenQuizzes,
                    )
                    QuickAction(
                        title = "Past papers",
                        subtitle = "SEE & NEB",
                        icon = Icons.Outlined.AutoStories,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenPapers,
                    )
                }
            }
            item { Spacer(Modifier.height(6.dp)) }
            item { SikAiSectionTitle("Tools") }
            item {
                NavRow(icon = Icons.Outlined.Schedule, title = "Study plan", subtitle = "Day-by-day map", onClick = onOpenStudyPlan)
            }
            item {
                NavRow(icon = Icons.Outlined.EditNote, title = "Notes", subtitle = "Save what matters", onClick = onOpenNotes)
            }
            item {
                NavRow(icon = Icons.Outlined.Insights, title = "Progress", subtitle = "Streak & weak topics", onClick = onOpenProgress)
            }
            item {
                NavRow(icon = Icons.Outlined.CloudDownload, title = "Downloads", subtitle = "Offline content", onClick = onOpenDownloads)
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun QuickAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = SikAi.colors
    SikAiCard(onClick = onClick, modifier = modifier) {
        Column {
            Icon(imageVector = icon, contentDescription = null, tint = colors.accent)
            Spacer(Modifier.height(12.dp))
            Text(text = title, style = SikAi.type.titleMedium, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, style = SikAi.type.bodySmall, color = colors.onSurfaceMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun NavRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val colors = SikAi.colors
    SikAiCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.accent)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = SikAi.type.titleMedium, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = SikAi.type.bodySmall, color = colors.onSurfaceMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(imageVector = Icons.Outlined.Article, contentDescription = null, tint = colors.borderStrong)
        }
    }
}
