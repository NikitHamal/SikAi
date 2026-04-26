package com.sikai.learn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sikai.learn.core.design.NeoVedicBottomNav
import com.sikai.learn.core.design.NeoVedicTheme
import com.sikai.learn.presentation.MainViewModel
import com.sikai.learn.presentation.downloads.DownloadsScreen
import com.sikai.learn.presentation.home.HomeScreen
import com.sikai.learn.presentation.notes.NotesScreen
import com.sikai.learn.presentation.onboarding.OnboardingScreen
import com.sikai.learn.presentation.papers.PapersScreen
import com.sikai.learn.presentation.progress.ProgressScreen
import com.sikai.learn.presentation.quiz.QuizScreen
import com.sikai.learn.presentation.settings.SettingsScreen
import com.sikai.learn.presentation.solve.SolveScreen
import com.sikai.learn.presentation.studyplan.StudyPlanScreen
import com.sikai.learn.presentation.tutor.TutorScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = hiltViewModel()
            val profile by vm.profile.collectAsState()
            val preferences by vm.preferences.collectAsState()
            NeoVedicTheme(preferences?.theme ?: "system") {
                val nav = rememberNavController()
                val bottom = listOf(
                    "home" to ("Home" to Icons.Outlined.Home),
                    "tutor" to ("Tutor" to Icons.Outlined.Psychology),
                    "solve" to ("Solve" to Icons.Outlined.PhotoCamera),
                    "papers" to ("Papers" to Icons.Outlined.Article),
                    "progress" to ("Progress" to Icons.Outlined.Insights)
                )
                val onboardingDone = profile?.onboardingComplete == true
                val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route
                LaunchedEffect(onboardingDone) {
                    if (profile != null) nav.navigate(if (onboardingDone) "home" else "onboarding") { popUpTo(0) }
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (onboardingDone && currentRoute in bottom.map { it.first }) {
                            NeoVedicBottomNav(bottom.map { it.second }, bottom.indexOfFirst { it.first == currentRoute }.coerceAtLeast(0)) { index ->
                                nav.navigate(bottom[index].first) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                ) { _ ->
                    NavHost(navController = nav, startDestination = "onboarding") {
                        composable("onboarding") { OnboardingScreen(onDone = { nav.navigate("home") { popUpTo("onboarding") { inclusive = true } } }) }
                        composable("home") { HomeScreen(nav) }
                        composable("tutor") { TutorScreen(nav) }
                        composable("solve") { SolveScreen(nav) }
                        composable("papers") { PapersScreen(nav) }
                        composable("downloads") { DownloadsScreen(nav) }
                        composable("quiz") { QuizScreen(nav) }
                        composable("study") { StudyPlanScreen(nav) }
                        composable("notes") { NotesScreen(nav) }
                        composable("progress") { ProgressScreen(nav) }
                        composable("settings") { SettingsScreen(nav) }
                    }
                }
            }
        }
    }
}
