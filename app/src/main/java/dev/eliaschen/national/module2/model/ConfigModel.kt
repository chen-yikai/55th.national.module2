package dev.eliaschen.national.module2.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel

class ConfigModel(private val context: Application) : AndroidViewModel(context) {
    private val sharedPreferences =
        context.getSharedPreferences("app", Context.MODE_PRIVATE) as SharedPreferences

    fun set(config: Config, value: Boolean) {
        sharedPreferences.edit().putBoolean(config.toString(), value).apply()
    }

    fun get(config: Config): Boolean {
        return sharedPreferences.getBoolean(config.toString(), false)
    }
}

enum class Config {
    IsGrid, IsNewest
}