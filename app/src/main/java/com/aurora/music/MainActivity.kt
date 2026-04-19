package com.aurora.music

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aurora.music.playback.PlaybackService
import com.aurora.music.ui.library.LibraryScreen
import com.aurora.music.ui.library.LibraryViewModel
import com.aurora.music.ui.player.AuroraPlayer
import com.aurora.music.ui.player.LocalAuroraPlayer
import com.aurora.music.ui.player.MiniPlayerBar
import com.aurora.music.ui.player.NowPlayingScreen
import com.aurora.music.ui.theme.AuroraTheme

private fun Context.hasAudioReadPermission(): Boolean {
    val permission = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

private fun audioReadPermission(): String {
    return if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

private fun permissionsToRequest(): Array<String> {
    return if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

private fun audioPermissionGranted(grants: Map<String, Boolean>): Boolean {
    return grants[audioReadPermission()] == true
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AuroraRoot()
        }
    }
}

@Composable
private fun AuroraRoot() {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val libraryVm: LibraryViewModel = viewModel()

    var auroraPlayer by remember { mutableStateOf(AuroraPlayer(null)) }

    DisposableEffect(activity) {
        val sessionToken = SessionToken(
            activity,
            ComponentName(activity, PlaybackService::class.java),
        )
        val mainExecutor = ContextCompat.getMainExecutor(activity)
        val future = MediaController.Builder(activity, sessionToken).buildAsync()
        future.addListener(
            {
                try {
                    val controller = future.get()
                    auroraPlayer = AuroraPlayer(controller)
                } catch (_: Exception) {
                    auroraPlayer = AuroraPlayer(null)
                }
            },
            mainExecutor,
        )

        onDispose {
            try {
                if (future.isDone) {
                    future.get().release()
                } else {
                    future.cancel(true)
                }
            } catch (_: Exception) {
            }
        }
    }

    var audioGranted by remember { mutableStateOf(context.hasAudioReadPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        audioGranted = audioPermissionGranted(grants)
        if (audioGranted) {
            libraryVm.refreshLibrary()
        }
    }

    LaunchedEffect(Unit) {
        if (!audioGranted) {
            permissionLauncher.launch(permissionsToRequest())
        } else {
            libraryVm.refreshLibrary()
        }
    }

    LaunchedEffect(audioGranted) {
        if (audioGranted) {
            libraryVm.refreshLibrary()
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    CompositionLocalProvider(LocalAuroraPlayer provides auroraPlayer) {
        AuroraTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (route == "library") {
                        MiniPlayerBar(
                            onOpenNowPlaying = { navController.navigate("nowPlaying") },
                        )
                    }
                },
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = "library",
                    modifier = Modifier.padding(padding),
                ) {
                    composable("library") {
                        LibraryScreen(
                            viewModel = libraryVm,
                            audioPermissionGranted = audioGranted,
                        )
                    }
                    composable("nowPlaying") {
                        NowPlayingScreen(
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
