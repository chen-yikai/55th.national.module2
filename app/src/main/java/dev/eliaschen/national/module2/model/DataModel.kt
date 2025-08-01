package dev.eliaschen.national.module2.model

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID

data class Note(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val content: List<ContentBlock>
)

data class ContentBlock(
    val id: String,
    val type: BlockType,
    val value1: String = "",
    val value2: String = "",
    val value3: String = ""
)

enum class BlockType {
    Text, Todo, List, Image
}

class DataModel(private val context: Application) : AndroidViewModel(context) {
    private val file = File(context.getExternalFilesDir(null), "data.json")
    val notes = mutableStateListOf<Note>()
    var newBlockId by mutableStateOf("")

    fun updateDate(noteId: String) {
        val noteIndex = notes.indexOfFirst { it.id == noteId }
        notes[noteIndex] = notes[noteIndex].copy(updatedAt = System.currentTimeMillis())
    }

    fun newNote(id: String) {
        notes.add(Note(id, "", System.currentTimeMillis(), System.currentTimeMillis(), emptyList()))
        val newId = UUID.randomUUID().toString()
        val noteIndex = notes.indexOfFirst { it.id == id }
        val currentNote = notes[noteIndex]
        val newContent =
            currentNote.content + ContentBlock(id = newId, type = BlockType.Text, value3 = "0")
        notes[noteIndex] = currentNote.copy(content = newContent)
        write()
    }

    fun newBlock(id: String, type: BlockType, value1: String = "") {
        val newId = UUID.randomUUID().toString()
        val noteIndex = notes.indexOfFirst { it.id == id }
        val currentNote = notes[noteIndex]
        val newContent =
            currentNote.content + ContentBlock(
                id = newId,
                type = type,
                value1 = value1,
                value3 = "0"
            )
        notes[noteIndex] = currentNote.copy(content = newContent)
        write()
        newBlockId = newId
    }

    fun getTypeOf(noteId: String, blockId: String): BlockType {
        val noteIndex = notes.indexOfFirst { it.id == noteId }
        val blockIndex = notes[noteIndex].content.indexOfFirst { it.id == blockId }
        return if (blockIndex != -1) {
            notes[noteIndex].content[blockIndex].type
        } else {
            BlockType.Text
        }
    }

    fun increaseBlockIndent(noteId: String, blockId: String) {
        val noteIndex = notes.indexOfFirst { it.id == noteId }
        val blockIndex = notes[noteIndex].content.indexOfFirst { it.id == blockId }
        if (blockIndex == -1) return
        val currentBlock = notes[noteIndex].content[blockIndex]
        val newContent = notes[noteIndex].content.toMutableList().apply {
            val currentIndent = currentBlock.value3.toIntOrNull() ?: 0
            set(
                blockIndex,
                currentBlock.copy(value3 = (if (currentIndent < 4) currentIndent + 1 else currentIndent).toString())
            )
        }
        notes[noteIndex] = notes[noteIndex].copy(content = newContent)
        write()
    }

    fun decreaseBlockIndent(noteId: String, blockId: String) {
        val noteIndex = notes.indexOfFirst { it.id == noteId }
        val blockIndex = notes[noteIndex].content.indexOfFirst { it.id == blockId }
        val currentBlock = notes[noteIndex].content[blockIndex]
        val newContent = notes[noteIndex].content.toMutableList().apply {
            val currentIndent = currentBlock.value3.toInt()
            set(
                blockIndex,
                currentBlock.copy(value3 = if (currentIndent == 0) "0" else (currentBlock.value3.toInt() - 1).toString())
            )
        }
        notes[noteIndex] = notes[noteIndex].copy(content = newContent)
        write()
    }

    fun updateBlock(
        noteId: String,
        blockId: String,
        value1: String = "",
        value2: String = "",
    ) {
        val noteIndex = notes.indexOfFirst { it.id == noteId }
        val blockIndex = notes[noteIndex].content.indexOfFirst { it.id == blockId }
        val currentBlock = notes[noteIndex].content[blockIndex]
        val newContent = notes[noteIndex].content.toMutableList().apply {
            set(blockIndex, currentBlock.copy(value1 = value1, value2 = value2))
        }
        notes[noteIndex] = notes[noteIndex].copy(content = newContent)
        write()
    }

    fun deleteNote(noteId: String) {
        val noteIndex = notes.indexOfFirst { it.id == noteId }
        notes.removeAt(noteIndex)
        write()
    }

    fun deleteBlock(noteId: String, blockId: String) {
        val noteIndex = notes.indexOfFirst { it.id == noteId }
        val blockIndex = notes[noteIndex].content.indexOfFirst { it.id == blockId }
        val newContent = notes[noteIndex].content.toMutableList()
        newContent.removeAt(blockIndex)
        notes[noteIndex] = notes[noteIndex].copy(content = newContent)
        write()
    }

    fun updateTitle(noteId: String, title: String) {
        val noteIndex = notes.indexOfFirst { it.id == noteId }
        notes[noteIndex] = notes[noteIndex].copy(title = title)
        write()
    }

    init {
        if (!file.exists()) {
            file.writeText("[]")
        }
        get()
    }

    fun get() {
        notes.clear()
        notes.addAll(Gson().fromJson(file.readText(), object : TypeToken<List<Note>>() {}.type))
    }

    fun write() {
        file.writeText(Gson().toJson(notes))
    }
}