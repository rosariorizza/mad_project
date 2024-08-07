package it.polito.mad.g18.mad_lab5

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.g18.mad_lab5.gui.ThemePreferences
import it.polito.mad.g18.mad_lab5.ui.theme.Navigation
import it.polito.mad.g18.mad_lab5.ui.theme.ThemeSwitcherTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences
    private var deepLinkUri: Uri? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        if (!hasRequiredCameraPermissions(applicationContext)) {
            ActivityCompat.requestPermissions(
                this,
                CAMERAX_PERMISSIONS, //array di permessi
                0
            )
        }
        setContent {
            var darkTheme by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                themePreferences.darkTheme.collect { darkTheme = it }
            }

            ThemeSwitcherTheme(darkTheme = darkTheme) {

                Navigation(deepLinkUri)
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }



    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            deepLinkUri = uri
        }
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA // to resolve this value you should import "android.Manifest" not "my.app.package.Manifest"
        )
    }

    private fun hasRequiredCameraPermissions(ctx: Context): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                ctx,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


}

