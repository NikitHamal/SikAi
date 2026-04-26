package com.sikai.learn.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sikai.learn.ui.components.NeoVedicBottomNav
import com.sikai.learn.ui.components.NeoVedicNavItem
import com.sikai.learn.ui.screens.boot.BootViewModel
import com.sikai.learn.ui.screens.downloads.DownloadsScreen
import com.sikai.learn.ui.screens.home.HomeScreen
import com.sikai.learn.ui.screens.notes.NotesScreen
import com.sikai.learn.ui.screens.onboarding.OnboardingScreen
import com.sikai.learn.ui.screens.papers.PastPapersScreen
import com.sikai.learn.ui.screens.plan.StudyPlanScreen
import com.sikai.learn.ui.screens.progress.ProgressScreen
import com.sikai.learn.ui.screens.quiz.QuizScreen
import com.sikai.learn.ui.screens.settings.ProviderEditScreen
import com.sikai.learn.ui.screens.settings.ProviderListScreen
import com.sikai.learn.ui.screens.settings.SettingsScreen
import com.sikai.learn.ui.screens.solve.SolveScreen
import com.sikai.learn.ui.screens.tutor.TutorScreen

private val bottomNavRoutes = setOf(
    SikAiDestinations.HOME,
    SikAiDestinations.PAPERS,
    SikAiDestinations.QUIZ,
    SikAiDestinations.NOTES,
    SikAiDestinations.PROGRESS,
    SikAiDestinations.SETTINGS,
)

@Composable
fun SikAiNavGraph() {
    val nav = rememberNavController()
    val backStack: NavBackStackEntry? by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val bootVm: BootViewModel = hiltViewModel()
    val onboarded by bootVm.onboarded.collectAsState(initial = null)
    val startDestination = when (onboarded) {
        null -> SikAiDestinations.ONBOARDING
        false -> SikAiDestinations.ONBOARDING
        true -> SikAiDestinations.HOME
    }

    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NeoVedicBottomNav(
                    items = listOf(
                        NeoVedicNavItem(SikAiDestinations.HOME, "Home", Icons.Filled.Home),
                        NeoVedicNavItem(SikAiDestinations.PAPERS, "Papers", Icons.Filled.Description),
                        NeoVedicNavItem(SikAiDestinations.QUIZ, "Quiz", Icons.Filled.Quiz),
                        NeoVedicNavItem(SikAiDestinations.NOTES, "Notes", Icons.Filled.AutoStories),
                        NeoVedicNavItem(SikAiDestinations.PROGRESS, "Progress", Icons.Filled.Insights),
                        NeoVedicNavItem(SikAiDestinations.SETTINGS, "Settings", Icons.Filled.Settings),
                    ),
                    currentRoute = currentRoute,
                    onSelect = { route ->
                        nav.navigate(route) {
                            popUpTo(SikAiDestinations.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = nav, startDestination = startDestination) {
                composable(SikAiDestinations.ONBOARDING) {
                    OnboardingScreen(onComplete = {
                        nav.navigate(SikAiDestinations.HOME) {
                            popUpTo(SikAiDestinations.ONBOARDING) { inclusive = true }
                        }
                    })
                }
                composable(SikAiDestinations.HOME) {
                    HomeScreen(
                        onOpenTutor = { nav.navigate(SikAiDestinations.TUTOR) },
                        onOpenSolve = { nav.navigate(SikAiDestinations.SOLVE) },
                        onOpenPapers = { nav.navigate(SikAiDestinations.PAPERS) },
                        onOpenDownloads = { nav.navigate(SikAiDestinations.DOWNLOADS) },
                        onOpenQuiz = { nav.navigate(SikAiDestinations.QUIZ) },
                        onOpenPlan = { nav.navigate(SikAiDestinations.PLAN) },
                        onOpenNotes = { nav.navigate(SikAiDestinations.NOTES) },
                        onOpenProgress = { nav.navigate(SikAiDestinations.PROGRESS) },
                        onOpenSettings = { nav.navigate(SikAiDestinations.SETTINGS) },
                    )
                }
                composable(SikAiDestinations.TUTOR) {
                    TutorScreen(onBack = { nav.popBackStack() })
                }
                composable(SikAiDestinations.SOLVE) {
                    SolveScreen(onBack = { nav.popBackStack() })
                }
                composable(SikAiDestinations.PAPERS) {
                    PastPapersScreen(
                        onBack = { nav.popBackStack() },
                        onOpenDownloads = { nav.navigate(SikAiDestinations.DOWNLOADS) },
                    )
                }
                composable(SikAiDestinations.DOWNLOADS) {
                    DownloadsScreen(onBack = { nav.popBackStack() })
                }
                composable(SikAiDestinations.QUIZ) {
                    QuizScreen(onBack = { nav.popBackStack() })
                }
                composable(SikAiDestinations.PLAN) {
                    StudyPlanScreen(onBack = { nav.popBackStack() })
                }
                composable(SikAiDestinations.NOTES) {
                    NotesScreen(onBack = { nav.popBackStack() })
                }
                composable(SikAiDestinations.PROGRESS) {
                    ProgressScreen(onBack = { nav.popBackStack() })
                }
                composable(SikAiDestinations.SETTINGS) {
                    SettingsScreen(
                        onBack = { nav.popBackStack() },
                        onOpenProviders = { nav.navigate(SikAiDestinations.PROVIDER_LIST) },
                    )
                }
                composable(SikAiDestinations.PROVIDER_LIST) {
                    ProviderListScreen(
                        onBack = { nav.popBackStack() },
                        onEdit = { id ->
                            nav.navigate("${SikAiDestinations.PROVIDER_EDIT}?id=$id")
                        },
                    )
                }
                composable("${SikAiDestinations.PROVIDER_EDIT}?id={id}") { entry ->
                    val id = entry.arguments?.getString("id")
                    ProviderEditScreen(
                        providerId = id,
                        onBack = { nav.popBackStack() },
                    )
                }
            }
        }
    }
}

@Suppress("unused")
private fun handle(controller: NavHostController) = controller // placeholder
