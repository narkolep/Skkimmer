package com.narkolep.skkimmer.keyboard.ui.components

import android.content.Context
import android.media.AudioManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.narkolep.skkimmer.HAPTIC_ENABLED_KEY
import com.narkolep.skkimmer.SOUND_ENABLED_KEY
import com.narkolep.skkimmer.dataStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map


@Composable
fun rememberFeedback(): () -> Unit {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    val hapticEnabled by remember(context) {
        context.dataStore.data
            .map { prefs ->
                prefs[HAPTIC_ENABLED_KEY] ?: true
            }
            .distinctUntilChanged()
    }.collectAsState(initial = true)

    val soundEnabled by remember(context) {
        context.dataStore.data
            .map { prefs ->
                prefs[SOUND_ENABLED_KEY] ?: false
            }
            .distinctUntilChanged()
    }.collectAsState(initial = false)

    return remember(hapticFeedback, audioManager, hapticEnabled, soundEnabled) {
        {
            if (hapticEnabled) {
                hapticFeedback.performHapticFeedback(
                    HapticFeedbackType.KeyboardTap
                )
            }

            if (soundEnabled) {
                audioManager.playSoundEffect(
                    AudioManager.FX_KEYPRESS_STANDARD
                )
            }
        }
    }
}
