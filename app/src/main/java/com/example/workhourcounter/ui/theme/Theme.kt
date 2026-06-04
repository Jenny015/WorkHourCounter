package com.example.workhourcounter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import com.example.workhourcounter.Config

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun WorkHourCounterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object AppDesignSystem {
    // Consistent Typography Mapping Rule
    @Composable
    fun getTitleStyle(): TextStyle {
        val base = MaterialTheme.typography.headlineMedium
        return when (Config.fontSize) {
            0 -> base
            1 -> base.copy(fontSize = base.fontSize * 1.15f, lineHeight = base.lineHeight * 1.15f)
            2 -> base.copy(fontSize = base.fontSize * 1.15f, lineHeight = base.lineHeight * 1.15f)
            else -> {
                base.copy(fontSize = base.fontSize * 1.15f, lineHeight = base.lineHeight * 1.15f)
            }
        }
    }

    @Composable
    fun getSectionHeaderStyle(): TextStyle {
        val base = MaterialTheme.typography.titleLarge
        return when (Config.fontSize) {
            0 -> base
            1 -> base.copy(fontSize = base.fontSize * 1.15f, lineHeight = base.lineHeight * 1.15f)
            2 -> base.copy(fontSize = base.fontSize * 1.35f, lineHeight = base.lineHeight * 1.25f)
            else -> {
                base.copy(fontSize = base.fontSize * 1.15f, lineHeight = base.lineHeight * 1.15f)
            }
        }
    }

    @Composable
    fun getBodyStyle(n: Int = -1): TextStyle {
        val base = MaterialTheme.typography.bodyMedium
        val size = if (n == -1) Config.fontSize else n;
        return when (size) {
            0 -> base
            1 -> base.copy(fontSize = base.fontSize * 1.15f, lineHeight = base.lineHeight * 1.15f)
            2 -> base.copy(fontSize = base.fontSize * 1.55f, lineHeight = base.lineHeight * 1.25f)
            else -> {
                base.copy(fontSize = base.fontSize * 1.15f, lineHeight = base.lineHeight * 1.15f)
            }
        }
    }

    @Composable
    fun getCalendarStyle(): TextStyle {
        return when (Config.fontSize) {
            0 -> MaterialTheme.typography.bodyMedium
            1 -> MaterialTheme.typography.titleSmall
            2 -> MaterialTheme.typography.titleLarge
            else -> {
                MaterialTheme.typography.titleSmall
            }
        }
    }
}