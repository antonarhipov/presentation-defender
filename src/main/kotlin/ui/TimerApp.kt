package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.TimerEntry
import viewmodel.TimerViewModel

@Composable
fun TimerApp(viewModel: TimerViewModel) {
    var newDescription by remember { mutableStateOf("") }
    var newMinutes by remember { mutableStateOf("0") }
    var newSeconds by remember { mutableStateOf("0") }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveFileName by remember { mutableStateOf("") }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode toggle, file operations, and total time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { viewModel.toggleMode() }) {
                        Text(if (viewModel.isEditMode) "Switch to Play Mode" else "Switch to Edit Mode")
                    }
                    if (viewModel.isEditMode) {
                        Button(onClick = { showSaveDialog = true }) {
                            Text("Save List")
                        }
                        Button(onClick = { viewModel.clearCurrentList() }) {
                            Text("Clear")
                        }
                    }
                }
                Text(
                    when {
                        !viewModel.isEditMode -> {
                            val minutes = viewModel.totalTimeRemaining.inWholeMinutes
                            val seconds = viewModel.totalTimeRemaining.inWholeSeconds % 60
                            "Total Time: %d:%02d".format(minutes, seconds)
                        }

                        else -> "Total Time: ${viewModel.totalTimeRemaining}"
                    }
                )
            }

            // Saved lists
            if (viewModel.isEditMode && viewModel.savedLists.isNotEmpty()) {
                Text(text = "Saved Lists:", style = MaterialTheme.typography.h6)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.savedLists) { name ->
                        Card(
                            elevation = 4.dp,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = name)
                                Button(
                                    onClick = { viewModel.loadList(name) },
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text("Load")
                                }
                                Button(
                                    onClick = { viewModel.deleteList(name) }
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }

            // Save dialog
            if (showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = { Text("Save List") },
                    text = {
                        TextField(
                            value = saveFileName,
                            onValueChange = { saveFileName = it },
                            label = { Text("List Name") }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (saveFileName.isNotBlank()) {
                                    viewModel.saveCurrentList(saveFileName)
                                    saveFileName = ""
                                    showSaveDialog = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showSaveDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // New entry input (only in edit mode)
            if (viewModel.isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Description") }
                    )
                    TextField(
                        value = newMinutes,
                        onValueChange = { newMinutes = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.width(80.dp),
                        label = { Text("Min") }
                    )
                    TextField(
                        value = newSeconds,
                        onValueChange = { newSeconds = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.width(80.dp),
                        label = { Text("Sec") }
                    )
                    Button(
                        onClick = {
                            if (newDescription.isNotBlank()) {
                                viewModel.addEntry(
                                    newDescription,
                                    newMinutes.toIntOrNull() ?: 0,
                                    newSeconds.toIntOrNull() ?: 0
                                )
                                newDescription = ""
                                newMinutes = "0"
                                newSeconds = "0"
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }

            // Entries list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.entries.withIndex().toList()) { (index, entry) ->
                    EntryItem(entry, viewModel.isEditMode, index, viewModel)
                }
            }
        }
    }
}

@Composable
fun EntryItem(entry: TimerEntry, isEditMode: Boolean, index: Int, viewModel: TimerViewModel) {
    if (isEditMode) {
        var description by remember(entry) { mutableStateOf(entry.description) }
        var minutes by remember(entry) { mutableStateOf((entry.duration.inWholeMinutes).toString()) }
        var seconds by remember(entry) { mutableStateOf((entry.duration.inWholeSeconds % 60).toString()) }
        var isEditing by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Description") }
                        )
                        TextField(
                            value = minutes,
                            onValueChange = { minutes = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.width(80.dp),
                            label = { Text("Min") }
                        )
                        TextField(
                            value = seconds,
                            onValueChange = { seconds = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.width(80.dp),
                            label = { Text("Sec") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateEntry(
                                    index,
                                    description,
                                    minutes.toIntOrNull() ?: 0,
                                    seconds.toIntOrNull() ?: 0
                                )
                                isEditing = false
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Save")
                        }
                        Button(
                            onClick = { isEditing = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = entry.description)
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            Text(text = "${entry.duration}", modifier = Modifier.padding(end = 16.dp))
                            Button(
                                onClick = { isEditing = true },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Edit")
                            }
                            Button(
                                onClick = { viewModel.removeEntry(index) }
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = entry.description)
                    Text(
                        text = entry.getRemainingTimeFormatted(),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                LinearProgressIndicator(
                    progress = entry.progress.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
