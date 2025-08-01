package dev.eliaschen.national.module2.screen

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eliaschen.national.module2.LocalDataModel
import dev.eliaschen.national.module2.LocalNavController
import dev.eliaschen.national.module2.R
import dev.eliaschen.national.module2.model.BlockType
import kotlinx.coroutines.delay

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalLayoutApi::class
)
@Composable
fun EditNoteScreen() {
    val nav = LocalNavController.current
    val dataModel = LocalDataModel.current
    val id = nav.noteId
    val note = dataModel.notes.find { it.id == id }

    var showDeleteDialog by remember { mutableStateOf(false) }

    var titleText by remember { mutableStateOf("") }
    val titleTextStyle =
        TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    val contentTextEmptyStyle = TextStyle(fontSize = 18.sp, color = Color.Gray)
    val contentTextStyleStyle = TextStyle(fontSize = 18.sp)

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val titleFocusRequester = remember { FocusRequester() }
    val blocksFocusRequesters = remember { mutableStateMapOf<String, FocusRequester>() }
    var currentFocus by remember { mutableStateOf("") }

    val context = LocalContext.current
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                dataModel.newBlock(id, BlockType.Image, uri.toString())
            } catch (e: Exception) {
                Log.e("EditNoteScreen", e.toString())
            }
        }
    }

    if (showDeleteDialog) DoubleCheckDialog(dismiss = { showDeleteDialog = false }) {
        dataModel.deleteNote(id)
        nav.pop()
    }

    if (note != null) {
        LaunchedEffect(Unit) {
            titleFocusRequester.requestFocus()
            titleText = note.title
            dataModel.updateDate(id)
        }

        LaunchedEffect(note.content) {
            blocksFocusRequesters.clear()
            note.content.forEach {
                blocksFocusRequesters[it.id] = FocusRequester()
            }
        }

        Scaffold(bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(WindowInsets.isImeVisible) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding(), shape = RectangleShape
                    ) {
                        LazyRow(modifier = Modifier.padding(horizontal = 10.dp)) {
                            item {
                                ToolKitButton(R.drawable.text_note) {
                                    dataModel.newBlock(id, BlockType.Text)
                                }
                                ToolKitButton(R.drawable.list_note) {
                                    dataModel.newBlock(id, BlockType.List)
                                }
                                ToolKitButton(R.drawable.check_note) {
                                    dataModel.newBlock(id, BlockType.Todo)
                                }
                                ToolKitButton(R.drawable.image_note) {
                                    fileLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                                ToolKitButton(
                                    R.drawable.decrease_indent,
                                    dataModel.getTypeOf(id, currentFocus) != BlockType.Image
                                ) {
                                    dataModel.decreaseBlockIndent(id, currentFocus)
                                }
                                ToolKitButton(
                                    R.drawable.increase_indent,
                                    dataModel.getTypeOf(id, currentFocus) != BlockType.Image
                                ) {
                                    dataModel.increaseBlockIndent(id, currentFocus)
                                }
                                ToolKitButton(R.drawable.undo) { }
                                ToolKitButton(R.drawable.redo) { }
                                ToolKitButton(R.drawable.delete) {
                                    if (currentFocus.isNotEmpty()) {
                                        dataModel.deleteBlock(
                                            id, currentFocus
                                        )
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        currentFocus = ""
                                    }
                                }
                                ToolKitButton(R.drawable.hide_keyboard) {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    currentFocus = ""
                                }
                            }
                        }
                    }
                }
            }
        }) { innerPadding ->
            LazyColumn(
                contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                modifier = Modifier.statusBarsPadding()
            ) {
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                nav.pop()
                            }, modifier = Modifier.graphicsLayer {
                                translationX = -40f
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null)
                            }
                            IconButton(onClick = {
                                showDeleteDialog = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        }
//                        Text("$currentFocus")
                        BasicTextField(value = titleText,
                            onValueChange = {
                                titleText = it
                                dataModel.updateTitle(id, titleText)
                            },
                            singleLine = true,
                            textStyle = titleTextStyle.copy(color = Color.Black),
                            decorationBox = { innerTextField ->
                                if (titleText.isEmpty()) {
                                    Text(
                                        note.title.ifEmpty { "未命名筆記" }, style = titleTextStyle
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier
                                .focusRequester(titleFocusRequester)
                                .onFocusChanged {
                                    if (it.isFocused) currentFocus = ""
                                })
                        Spacer(Modifier.height(20.dp))
                    }
                }
                items(note.content, key = { it.id }) { block ->
                    val focus = remember(block.id) {
                        FocusRequester().also { blocksFocusRequesters[block.id] = it }
                    }

                    LaunchedEffect(dataModel.newBlockId, block.id) {
                        if (dataModel.newBlockId == block.id && block.type != BlockType.Image) {
                            focus.requestFocus()
                            dataModel.newBlockId = ""
                        }
                    }

                    Surface(
                        modifier = Modifier.padding(
                            start = if (block.type != BlockType.Image) (10 * (block.value3.toIntOrNull()
                                ?: 0)).dp else 0.dp
                        )
                    ) {
                        when (block.type) {
                            BlockType.Text -> {
                                var text by remember { mutableStateOf(block.value1) }

                                LaunchedEffect(text) {
                                    dataModel.updateBlock(id, block.id, text)
                                }

                                Surface(modifier = Modifier.padding(vertical = 5.dp)) {
                                    BasicTextField(text,
                                        onValueChange = { text = it },
                                        textStyle = contentTextStyleStyle,
                                        singleLine = true,
                                        decorationBox = { innerTextField ->
                                            if (text.isEmpty()) {
                                                Text(
                                                    "在這裡輸入文字", style = contentTextEmptyStyle
                                                )
                                            }
                                            innerTextField()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp)
                                            .focusRequester(focus)
                                            .onFocusChanged {
                                                if (it.isFocused) currentFocus = block.id
                                            })
                                }
                            }

                            BlockType.Todo -> {
                                var text by remember { mutableStateOf(block.value1) }
                                var isChecked by remember { mutableStateOf(block.value2.toBoolean()) }

                                LaunchedEffect(text, isChecked) {
                                    dataModel.updateBlock(id, block.id, text, isChecked.toString())
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp)
                                ) {
                                    Checkbox(checked = isChecked,
                                        onCheckedChange = { isChecked = it })
                                    BasicTextField(text,
                                        onValueChange = { text = it },
                                        textStyle = contentTextStyleStyle,
                                        singleLine = true,
                                        decorationBox = { innerTextField ->
                                            if (text.isEmpty()) {
                                                Text(
                                                    "在這裡輸入文字", style = contentTextEmptyStyle
                                                )
                                            }
                                            innerTextField()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focus)
                                            .onFocusChanged {
                                                if (it.isFocused) currentFocus = block.id
                                            })
                                }
                            }

                            BlockType.List -> {
                                var text by remember { mutableStateOf(block.value1) }

                                LaunchedEffect(text) {
                                    dataModel.updateBlock(id, block.id, text)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp)
                                        .padding(vertical = 5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    BasicTextField(text,
                                        onValueChange = { text = it },
                                        textStyle = contentTextStyleStyle,
                                        singleLine = true,
                                        decorationBox = { innerTextField ->
                                            if (text.isEmpty()) {
                                                Text(
                                                    "在這裡輸入文字", style = contentTextEmptyStyle
                                                )
                                            }
                                            innerTextField()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focus)
                                            .onFocusChanged {
                                                if (it.isFocused) currentFocus = block.id
                                            })
                                }
                            }

                            BlockType.Image -> {
                                NetworkImage(block.value1) {
                                    Box(modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth()
                                        .focusRequester(focus)
                                        .clickable {
                                            currentFocus = block.id
                                            focus.requestFocus()
                                            keyboardController?.show()
                                        }) {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(it.width / it.height.toFloat())
                                        )
                                    }
                                }
                            }

                            else -> {
                                Text("無法顯示此區塊")
                            }
                        }
                    }
                }
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable {
                                dataModel.newBlock(id, BlockType.Text)
                            })
                }
            }
        }
    }
}


@Composable
fun NetworkImage(uriString: String, content: @Composable (Bitmap) -> Unit) {
    val context = LocalContext.current
    var bitmap by remember(uriString) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uriString) {
        try {
            val uri = Uri.parse(uriString)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                bitmap = BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e("NetworkImage", "Failed to load image", e)
        }
    }

    bitmap?.let {
        content(it)
    }
}

@Composable
fun DoubleCheckDialog(dismiss: () -> Unit, confirm: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("確定要刪除此筆記嗎？") },
        confirmButton = {
            Button(onClick = {
                dismiss()
                confirm()
            }) {
                Text("刪除")
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = dismiss) {
                Text("取消")
            }
        })
}

@Composable
fun ToolKitButton(icon: Int, enable: Boolean = true, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enable) {
        Icon(
            painter = painterResource(icon), contentDescription = null
        )
    }
}