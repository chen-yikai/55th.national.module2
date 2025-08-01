package dev.eliaschen.national.module2

import android.app.LocaleConfig
import android.os.Bundle
import android.provider.ContactsContract.Data
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import dev.eliaschen.national.module2.model.ConfigModel
import dev.eliaschen.national.module2.model.DataModel
import dev.eliaschen.national.module2.model.NavController
import dev.eliaschen.national.module2.model.Screen
import dev.eliaschen.national.module2.screen.AllNoteScreen
import dev.eliaschen.national.module2.screen.EditNoteScreen
import dev.eliaschen.national.module2.ui.theme.Nationalmodule2Theme

val LocalNavController = compositionLocalOf<NavController> { error("LocalNavController") }
val LocalDataModel = compositionLocalOf<DataModel> { error("LocalDataModel") }
val LocalConfig = compositionLocalOf<ConfigModel> { error("LocalConfig") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Nationalmodule2Theme {
                val nav = ViewModelProvider(this)[NavController::class.java]
                val data = ViewModelProvider(this)[DataModel::class.java]
                val config = ViewModelProvider(this)[ConfigModel::class.java]

                CompositionLocalProvider(
                    LocalNavController provides nav,
                    LocalDataModel provides data,
                    LocalConfig provides config
                ) {
                    BackHandler {
                        if(nav.navStack.size > 0){
                            nav.pop()
                        }else{
                            finish()
                        }
                    }
                    Surface {
                        when (nav.currentNav) {
                            Screen.AllNote -> AllNoteScreen()
                            Screen.EditNote -> EditNoteScreen()
                        }
                    }
                }
            }
        }
    }
}
