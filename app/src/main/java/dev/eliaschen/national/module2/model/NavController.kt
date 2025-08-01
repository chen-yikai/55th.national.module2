package dev.eliaschen.national.module2.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class NavController : ViewModel() {
    private var initScreen = Screen.AllNote
    var currentNav by mutableStateOf(initScreen)
    var navStack = mutableStateListOf<Screen>()
    var noteId by mutableStateOf("")

    init {
        navStack.add(initScreen)
    }

    fun navTo(screen: Screen) {
        currentNav = screen
        navStack.add(screen)
    }

    fun pop() {
        navStack.removeLast()
        currentNav = navStack.last()
    }
}

enum class Screen {
    AllNote, EditNote
}