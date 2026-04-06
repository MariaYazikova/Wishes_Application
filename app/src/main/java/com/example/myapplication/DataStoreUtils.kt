package com.example.myapplication

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

//DataStore для хранения настроек приложения
val Context.dataStore by preferencesDataStore(name = "app_prefs")

//ключи для хранения данных в DataStore
object PreferencesKeys {
    val FIRST_LAUNCH = booleanPreferencesKey("first_launch") //первый запуск приложения
    val CATEGORIES_JSON = stringPreferencesKey("categories_json") //категории и идеи в виде json
}