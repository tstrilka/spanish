package app.espanol.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.espanol.ui.theme.SpanishBrown
import app.espanol.ui.theme.SpanishGoldDark

@Composable
fun LearningScreen(
    modifier: Modifier = Modifier,
    viewModel: LearningViewModel,
    onSpeak: (String) -> Unit
) {
    val currentPair by viewModel.currentPair.collectAsStateWithLifecycle()
    val showTranslation by viewModel.showTranslation.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val learningMode by viewModel.learningMode.collectAsStateWithLifecycle()
    val hasPlayedAudio by viewModel.hasPlayedAudio.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadNextPair()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text(
                text = "Visual",
                color = if (learningMode == LearningMode.VISUAL) {
                    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (learningMode == LearningMode.VISUAL) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = learningMode == LearningMode.LISTENING,
                onCheckedChange = { isListening ->
                    viewModel.setLearningMode(
                        if (isListening) LearningMode.LISTENING else LearningMode.VISUAL
                    )
                },
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    checkedTrackColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    },
                    uncheckedThumbColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    uncheckedTrackColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Listening",
                color = if (learningMode == LearningMode.LISTENING) {
                    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (learningMode == LearningMode.LISTENING) FontWeight.Bold else FontWeight.Normal
            )
        }
        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading next word...")
        } else if (currentPair != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (learningMode) {
                        LearningMode.VISUAL -> {
                            // Traditional visual learning mode
                            Text(
                                text = "Translate to Spanish:",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = currentPair?.original ?: "",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            if (showTranslation) {
                                Text(
                                    text = "Spanish:",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentPair?.translated ?: "",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                                        SpanishBrown
                                    } else {
                                        SpanishGoldDark
                                    }
                                )
                            } else {
                                Text(
                                    text = "Think of the Spanish translation, then:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        LearningMode.LISTENING -> {
                            // New listening learning mode
                            Text(
                                text = "Listen and understand:",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "🎧",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (!hasPlayedAudio) {
                                Text(
                                    text = "Play the Spanish audio first:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        currentPair?.let { pair ->
                                            onSpeak(pair.translated)
                                            viewModel.markAudioPlayed()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                ) {
                                    Text("🔊 Play Spanish")
                                }
                            } else {
                                if (showTranslation) {
                                    Text(
                                        text = "Spanish:",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = currentPair?.translated ?: "",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                                            SpanishBrown
                                        } else {
                                            SpanishGoldDark
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Czech meaning:",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = currentPair?.original ?: "",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            currentPair?.let { pair -> onSpeak(pair.translated) }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary,
                                            contentColor = MaterialTheme.colorScheme.onSecondary
                                        )
                                    ) {
                                        Text("🔊 Play Again")
                                    }
                                } else {
                                    Text(
                                        text = "Did you understand the meaning?",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            currentPair?.let { pair -> onSpeak(pair.translated) }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary,
                                            contentColor = MaterialTheme.colorScheme.onSecondary
                                        )
                                    ) {
                                        Text("🔊 Play Again")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Show translation button (for visual mode or after audio played)
                    if ((learningMode == LearningMode.VISUAL && !showTranslation) ||
                        (learningMode == LearningMode.LISTENING && hasPlayedAudio && !showTranslation)
                    ) {
                        Button(
                            onClick = { viewModel.showTranslation() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                                contentColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSecondary
                                }
                            )
                        ) {
                            Text(if (learningMode == LearningMode.VISUAL) "Show Translation" else "Show Answer")
                        }
                    }

                    // Result buttons (show after translation is revealed)
                    if (showTranslation) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (learningMode == LearningMode.VISUAL) {
                                "How did you do?"
                            } else {
                                "Did you understand correctly?"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.markResult(true)
                                    viewModel.loadNextPair()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("✓ " + if (learningMode == LearningMode.VISUAL) "Correct" else "Understood")
                            }
                            Button(
                                onClick = {
                                    viewModel.markResult(false)
                                    viewModel.loadNextPair()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                                        Color(0xFFD32F2F)
                                    } else {
                                        Color(0xFFEF5350)
                                    },
                                    contentColor = Color.White
                                )
                            ) {
                                Text("✗ " + if (learningMode == LearningMode.VISUAL) "Wrong" else "Didn't understand")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.loadNextPair() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    contentColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            ) {
                Text("Skip to Next Word")
            }
        } else {
            Text(
                text = "No translations available for learning.\nAdd some translations first!",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}