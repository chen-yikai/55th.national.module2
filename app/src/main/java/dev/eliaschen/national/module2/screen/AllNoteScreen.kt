package dev.eliaschen.national.module2.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import dev.eliaschen.national.module2.LocalConfig
import dev.eliaschen.national.module2.LocalDataModel
import dev.eliaschen.national.module2.LocalNavController
import dev.eliaschen.national.module2.R
import dev.eliaschen.national.module2.model.BlockType
import dev.eliaschen.national.module2.model.Config
import dev.eliaschen.national.module2.model.ContentBlock
import dev.eliaschen.national.module2.model.NavController
import dev.eliaschen.national.module2.model.Note
import dev.eliaschen.national.module2.model.Screen
import kotlinx.coroutines.selects.select
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.NoSuchElementException
import java.util.UUID
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllNoteScreen() {
    val nav = LocalNavController.current
    val data = LocalDataModel.current
    val config = LocalConfig.current
    var searchText by remember { mutableStateOf("") }

    var initialized by remember { mutableStateOf(false) }
    var isGrid by remember { mutableStateOf(config.get(Config.IsGrid)) }
    var showNewest by remember { mutableStateOf(config.get(Config.IsNewest).also { initialized = true }) }

    LaunchedEffect(isGrid) {
        if (initialized) {
            config.set(Config.IsGrid, isGrid)
        }
    }

    LaunchedEffect(showNewest) {
        if (initialized) {
            config.set(Config.IsNewest, showNewest)
        }
    }

    LaunchedEffect(showNewest) {
        if (showNewest) {
            data.notes.sortByDescending { it.updatedAt }
        } else {
            data.notes.sortBy { it.updatedAt }
        }
    }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {
            val newNoteId = UUID.randomUUID().toString()
            data.newNote(newNoteId)
            nav.noteId = newNoteId
            nav.navTo(Screen.EditNote)
        }) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("所有筆記", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Row {
                OutlinedTextField(
                    searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("搜尋筆記") },
                    shape = CircleShape,
                    trailingIcon = {
                        Icon(
                            Icons.Default.Search, contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                var showDropdownMenu by remember { mutableStateOf(false) }
                val dropdownItems = listOf("最新", "最舊")
                Box {
                    DropdownMenu(
                        onDismissRequest = { showDropdownMenu = false },
                        expanded = showDropdownMenu
                    ) {
                        dropdownItems.forEachIndexed { index, item ->
                            DropdownMenuItem(text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(item)
                                    if ((index == 0 && showNewest) || (index != 0 && !showNewest)) Icon(
                                        Icons.Default.Check,
                                        contentDescription = null
                                    )
                                }
                            }, onClick = {
                                showNewest = index == 0
                            })
                        }
                    }
                    IconButton(onClick = { showDropdownMenu = true }) {
                        Icon(painter = painterResource(R.drawable.sort), contentDescription = null)
                    }
                }
                val chooseIcons = listOf(R.drawable.list, R.drawable.grid)
                var chooseItem by remember { mutableIntStateOf(0) }
                
                LaunchedEffect(isGrid) {
                    chooseItem = if (isGrid) 1 else 0
                }
                
                LaunchedEffect(chooseItem) {
                    if (initialized) {
                        isGrid = chooseItem == 1
                    }
                }
                SingleChoiceSegmentedButtonRow {
                    chooseIcons.forEachIndexed { index, item ->
                        SegmentedButton(selected = chooseItem == index,
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index, count = chooseIcons.size
                            ),
                            onClick = {
                                chooseItem = index
                            }) {
                            Icon(painter = painterResource(item), contentDescription = null)
                        }
                    }
                }
            }
            if (isGrid) {
                LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                    items(data.notes) { note ->
                        if (note.title.lowercase().contains(searchText.lowercase())) NoteCard(
                            note,
                            nav
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(data.notes) { note ->
                        if (note.title.lowercase().contains(searchText.lowercase())) NoteCard(
                            note,
                            nav
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, nav: NavController = NavController()) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp),
        border = CardDefaults.outlinedCardBorder(), onClick = {
            nav.noteId = note.id
            nav.navTo(Screen.EditNote)
        }
    ) {
        Row(modifier = Modifier.padding(15.dp)) {
            Column {
                Text(
                    note.title.ifEmpty { "未命名筆記" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                if (note.content.isNotEmpty()) {
                    if (note.content[0].value1.isNotEmpty() && note.content[0].type != BlockType.Image) {
                        Text(note.content[0].value1, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    SimpleDateFormat(
                        "yyyy年MM月dd日 HH:mm a",
                        Locale.TAIWAN
                    ).format(note.updatedAt)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmptyNoteCard() {
    NoteCard(
        Note(
            id = "xxx",
            title = "未命名筆記",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = emptyList()
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewNoteCard() {
    NoteCard(
        Note(
            id = "xxx",
            title = "未命名筆記",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            content = listOf(
                ContentBlock(
                    id = "xxx",
                    type = BlockType.Text,
                    value1 = "這是第五十五屆全國技能競賽測試筆記，這是第五十五屆全國技能競賽測試筆記。"
                )
            )
        )
    )
}
