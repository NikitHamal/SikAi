package com.sikai.learn.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sikai.learn.presentation.boot.BootState
import com.sikai.learn.presentation.screens.aitutor.AiTutorScreen
import com.sikai.learn.presentation.screens.downloads.DownloadsScreen
import com.sikai.learn.presentation.screens.home.HomeScreen
import com.sikai.learn.presentation.screens.notes.NotesScreen
import com.sikai.learn.presentation.screens.onboarding.OnboardingScreen
import com.sikai.learn.presentation.screens.papers.PastPapersScreen
import com.sikai.learn.presentation.screens.progress.ProgressScreen
import com.sikai.learn.presentation.screens.quizzes.QuizzesScreen
import com.sikai.learn.presentation.screens.settings.SettingsScreen
import com.sikai.learn.presentation.screens.snap.SnapAndSolveScreen
import com.sikai.learn.presentation.screens.studyplan.StudyPlanScreen
import com.sikai.learn.ui.components.SikAiBottomNav
import com.sikai.learn.ui.components.SikAiNavItem
import com.sikai.learn.ui.theme.SikAi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings

object Routes {
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val AiTutor = "ai_tutor"
    const val Snap = "snap"
    const val Settings = "settings"
    const val PastPapers = "past_papers"
    const val Downloads = "downloads"
    const val Quizzes = "quizzes"
    const val StudyPlan = "study_plan"
    const val Notes = "notes"
    const val Progress = "progress"
}

private val tabs = listOf(
    SikAiNavItem(Routes.Home, "Home", Icons.Outlined.Home),
    SikAiNavItem(Routes.Snap, "Snap", Icons.Outlined.CameraAlt),
    SikAiNavItem(Routes.PastPapers, "Papers", Icons.Outlined.AutoStories),
    SikAiNavItem(Routes.Settings, "Settings", Icons.Outlined.Settings),
)

@Composable
fun SikAiRoot(bootState: BootState) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Routes.Home

    val startDestination = if (bootState.isOnboarded) Routes.Home else Routes.Onboarding
    val showTabs = currentRoute in tabs.map { it.key }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SikAi.colors.background)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            NavHost(navController = navController, startDestination = startDestination) {
                composable(Routes.Onboarding) {
                    OnboardingScreen(onCompleted = {
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Onboarding) { inclusive = true }
                        }
                    })
                }
                composable(Routes.Home) {
                    HomeScreen(
                        onOpenAiTutor = { navController.navigate(Routes.AiTutor) },
                        onOpenSnap = { navController.navigate(Routes.Snap) },
                        onOpenPapers = { navController.navigate(Routes.PastPapers) },
                        onOpenQuizzes = { navController.navigate(Routes.Quizzes) },
                        onOpenStudyPlan = { navController.navigate(Routes.StudyPlan) },
                        onOpenNotes = { navController.navigate(Routes.Notes) },
                        onOpenProgress = { navController.navigate(Routes.Progress) },
                        onOpenDownloads = { navController.navigate(Routes.Downloads) },
                    )
                }
                composable(Routes.AiTutor) { AiTutorScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.Snap) { SnapAndSolveScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.PastPapers) {
                    PastPapersScreen(
                        onBack = { navController.popBackStack() },
                        onOpenDownloads = { navController.navigate(Routes.Downloads) },
                    )
                }
                composable(Routes.Downloads) { DownloadsScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.Quizzes) { QuizzesScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.StudyPlan) { StudyPlanScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.Notes) { NotesScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.Progress) { ProgressScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.Settings) {
                    SettingsScreen(
                        onOpenDownloads = { navController.navigate(Routes.Downloads) },
                    )
                }
            }
        }
        if (showTabs && bootState.isOnboarded) {
            SikAiBottomNav(
                items = tabs,
                selectedKey = currentRoute,
                onSelect = { key ->
                    if (key != currentRoute) {
                        navController.navigate(key) {
                            popUpTo(Routes.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}