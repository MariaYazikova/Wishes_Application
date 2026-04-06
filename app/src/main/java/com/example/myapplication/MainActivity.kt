package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold { innerPadding ->

                    //для фонового сохранения завершения первого запуска
                    val scope = rememberCoroutineScope()
                    //для хранения состояния - первый ли запуск приложения
                    var isFirstLaunch by remember { mutableStateOf<Boolean?>(null) }

                    //чтение и обновление флага первого запуска
                    LaunchedEffect(Unit) {
                        dataStore.data
                            .map { prefs -> prefs[PreferencesKeys.FIRST_LAUNCH] ?: true }
                            .collect { value ->
                                isFirstLaunch = value
                            }
                    }

                    //ожидание прочтения значения первого запуска
                    if (isFirstLaunch != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            //первый запуск - показ начального экран
                            if (isFirstLaunch == true) {
                                IntroScreen(
                                    onStartClick = {
                                        //сохранение завершения первого запуска
                                        scope.launch {
                                            dataStore.edit { prefs ->
                                                prefs[PreferencesKeys.FIRST_LAUNCH] = false
                                            }
                                        }
                                        isFirstLaunch = false
                                    }
                                )
                            } else {
                                //не первый запуск - основной экран с желаниями
                                IdeasScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}