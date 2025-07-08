package app.espanol.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
                            Text("🔊 Speak")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "How did you do?",
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
                                    containerColor = Color(0xFF4CAF50), // Green
                                    contentColor = Color.White
                                )
                            ) {
                                Text("✓ Correct")
                            }
                            Button(
                                onClick = {
                                    viewModel.markResult(false)
                                    viewModel.loadNextPair()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                                        Color(0xFFD32F2F) // Light mode - darker red
                                    } else {
                                        Color(0xFFEF5350) // Dark mode - lighter red
                                    },
                                    contentColor = Color.White
                                )
                            ) {
                                Text("✗ Wrong")
                            }                        }
                    } else {
                        Text(
                            text = "Think of the Spanish translation, then:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

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
                            Text("Show Translation")
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