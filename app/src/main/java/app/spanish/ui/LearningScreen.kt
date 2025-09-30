package app.spanish.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.spanish.data.TextPair

@OptIn(ExperimentalMaterial3Api::class)
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
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categoryTotalCount by viewModel.categoryTotalCount.collectAsStateWithLifecycle()
    val categorySuccessCount by viewModel.categorySuccessCount.collectAsStateWithLifecycle()
    val progress =
        if (categoryTotalCount > 0) categorySuccessCount / categoryTotalCount.toFloat() else 0f

    LaunchedEffect(Unit) {
        viewModel.loadNextPair()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(bottom = 16.dp)) {
            SegmentedButton(
                selected = learningMode == LearningMode.VISUAL,
                onClick = { viewModel.setLearningMode(LearningMode.VISUAL) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text("Visual")
            }

            SegmentedButton(
                selected = learningMode == LearningMode.LISTENING,
                onClick = { viewModel.setLearningMode(LearningMode.LISTENING) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text("Listening")
            }
        }

        var expanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            var expanded by remember { mutableStateOf(false) }
            val focusRequester = remember { FocusRequester() }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                TextField(
                    value = selectedCategory ?: "All Categories",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .focusRequester(focusRequester),
                    label = { Text("Category") }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Categories") },
                        onClick = {
                            viewModel.setSelectedCategory(null)
                            expanded = false
                        }
                    )

                    availableCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                viewModel.setSelectedCategory(category)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else if (currentPair == null) {
                Text(
                    text = "No learning pairs available.\nAdd some translations first.",
                    textAlign = TextAlign.Center
                )
            } else {
                when (learningMode) {
                    LearningMode.VISUAL -> VisualLearningContent(
                        pair = currentPair!!,
                        showTranslation = showTranslation,
                        onShowTranslation = { viewModel.showTranslation() },
                        onSpeak = onSpeak
                    )

                    LearningMode.LISTENING -> ListeningLearningContent(
                        pair = currentPair!!,
                        showTranslation = showTranslation,
                        hasPlayedAudio = hasPlayedAudio,
                        onShowTranslation = { viewModel.showTranslation() },
                        onSpeak = {
                            onSpeak(currentPair!!.translated)
                            viewModel.markAudioPlayed()
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "$categorySuccessCount/$categoryTotalCount",
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(
                onClick = { viewModel.resetCategoryProgress() },
                enabled = selectedCategory != null && categoryTotalCount > 0
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Progress")
            }
        }

        if (currentPair != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        viewModel.markResult(true)
                        viewModel.loadNextPair()
                    },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50), // Green
                        contentColor = androidx.compose.ui.graphics.Color.White // White text
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Correct")
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Correct")
                }

                Spacer(modifier = Modifier.size(16.dp))

                Button(
                    onClick = {
                        viewModel.markResult(false)
                        viewModel.loadNextPair()
                    },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    )
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Incorrect")
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Incorrect")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { viewModel.loadNextPair() },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = androidx.compose.ui.graphics.Color.White // White text
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Skip")
                Spacer(modifier = Modifier.size(8.dp))
                Text("Skip")
            }
        } else {
            Button(
                onClick = { viewModel.loadNextPair() },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = androidx.compose.ui.graphics.Color.White // White text
                )
            ) {
                Text("Try Again")
            }
        }
    }
}

@Composable
fun VisualLearningContent(
    pair: TextPair,
    showTranslation: Boolean,
    onShowTranslation: () -> Unit,
    onSpeak: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Czech",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp, max = 180.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pair.original,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Spanish",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showTranslation) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp, max = 180.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    ) {
                        Text(
                            text = pair.translated,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        IconButton(
                            onClick = { onSpeak(pair.translated) },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Speak",
                                tint = androidx.compose.ui.graphics.Color.Red,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onShowTranslation,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        Text("Reveal Translation")
                    }
                }
            }
        }
    }
}

@Composable
fun ListeningLearningContent(
    pair: TextPair,
    showTranslation: Boolean,
    hasPlayedAudio: Boolean,
    onShowTranslation: () -> Unit,
    onSpeak: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Listen and Learn",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Make the play sound button/icon more visible
        Button(
            onClick = onSpeak,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .height(56.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = androidx.compose.ui.graphics.Color.White
            )
        ) {
            Icon(
                Icons.Default.VolumeUp,
                contentDescription = "Play audio",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Play Spanish Audio")
        }

        AnimatedVisibility(
            visible = hasPlayedAudio,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showTranslation) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Spanish",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = pair.translated,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Text(
                                text = "Czech",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 16.dp)
                            )

                            Text(
                                text = pair.original,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onShowTranslation,
                        modifier = Modifier.padding(top = 16.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        Text("Show Meaning")
                    }
                }
            }
        }
    }
}