//разрешение использования экспериментальных фукнкций
@file:OptIn(ExperimentalFoundationApi::class)
package com.example.myapplication

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.myapplication.ui.theme.DelaGothicOneFont
import androidx.compose.ui.window.Dialog

val ButtonGreen = Color(0xFFE3FC87)
val MainBlue = Color(0xFF253A82)
val DoneBlue = Color(0xFF5A6FD6).copy(alpha = 0.6f)

//класс для сериализации содержимого в категории (одна идея)
@Serializable
data class SerializableIdea(val id: Int, val text: String, val isDone: Boolean)
//класс для сериализации категории с её содержимым
@Serializable
data class SerializableCategory(val id: Int, val name: String, val ideas: List<SerializableIdea>)

//класс для идеи в категории
data class Idea(
    val id: Int, //уникальный идентификатор
    var text: String, //текст идеи
    var isDone: Boolean = false //выполнено/не выполнено
) {
    var stateText by mutableStateOf(text)//отслеживаемый текст
    var stateDone by mutableStateOf(isDone)//отслеживаемый статус
}
data class Category(
    val id: Int, //уникальный идентификатор
    var name: String, //название категории
    val ideas: MutableList<Idea> = mutableStateListOf(), //список идей
    val isExpandedInitial: Boolean = false //начальное состояние раскрытия
) {
    var isExpanded by mutableStateOf(isExpandedInitial) //текущее состояние раскрытия
    var stateName by mutableStateOf(name)//отслеживаемое название
}

@Composable
//основной экран с категориями и идеями
fun IdeasScreen() {
    val context = LocalContext.current //контекст приложения
    val scope = rememberCoroutineScope() //для фонового сохранения категорий и идей
    var categories by remember { mutableStateOf(mutableStateListOf<Category>()) } //список категорий с идеями
    var showFirstLaunchDialog by remember { mutableStateOf(false) } //показывать ли инф окно первого запуска

    var newCategoryDialog by remember { mutableStateOf(false) }//показывать ли окно добавления категории
    var newCategoryName by remember { mutableStateOf("") }//имя новой категории

    var newIdeaCategory by remember { mutableStateOf<Category?>(null) }//категория, в которую добавляется идея
    var newIdeaText by remember { mutableStateOf("") }//текст новой идеи

    var renameCategoryTarget by remember { mutableStateOf<Category?>(null) }//категория для переименования
    var renameIdeaTarget by remember { mutableStateOf<Idea?>(null) }//идея для переименования
    var renameText by remember { mutableStateOf("") }//текст переименования категории/идеи

    //загрузка категорий
    LaunchedEffect(Unit) {
        //сохраненные данные из DataStore
        val prefs = context.dataStore.data.first()
        val json = prefs[PreferencesKeys.CATEGORIES_JSON]

        //есть сохраненные категории - загружаем их
        categories = if (json != null) {
            try {
                Json.decodeFromString<List<SerializableCategory>>(json).map { sc ->
                    Category(
                        id = sc.id,
                        name = sc.name,
                        ideas = sc.ideas.map { i -> Idea(i.id, i.text, i.isDone) }.toMutableStateList()
                    )
                }.toMutableStateList()
            } catch (_: Exception) {
                mutableStateListOf()
            }
            //иначе показываем примеры и инф окно первого запуска
        } else {
            showFirstLaunchDialog = true
            mutableStateListOf(
                Category(
                    id = 1,
                    name = "Места",
                    mutableStateListOf(
                        Idea(1, "Мон-Сен-Мишель, Франция"),
                        Idea(2, "Собор святого Вита, Чехия")
                    )
                ),
                Category(
                    id = 2,
                    name = "Развлечения",
                    mutableStateListOf(
                        Idea(3, "Рафтинг"),
                        Idea(4, "Катание на сноуборде")
                    )
                ),
                Category(
                    id = 3,
                    name = "Блюда",
                    mutableStateListOf(
                        Idea(5, "Чуррос"),
                        Idea(6, "Такояки")
                    )
                ),
                Category(
                    id = 4,
                    name = "Цели",
                    mutableStateListOf(
                        Idea(7, "Пройти курс Android-разработки"),
                        Idea(8, "Научиться говорить по-китайски")
                    )
                ),
                Category(
                    id = 5,
                    name = "Блюда",
                    mutableStateListOf(
                        Idea(9, "Опера-балет"),
                        Idea(10, "Чемпионат по фигурному катанию")
                    )
                )
            )
        }
    }

    //сохранение категорий в DataStore
    fun saveData() {
        scope.launch {
            val serializable = categories.map { c ->
                SerializableCategory(c.id, c.name, c.ideas.map { i -> SerializableIdea(i.id, i.text, i.isDone) })
            }
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.CATEGORIES_JSON] = Json.encodeToString(serializable)
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        //заголовок
        Text(
            "Мои желания",
            fontFamily = DelaGothicOneFont,
            color = MainBlue,
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        //окно первого запуска
        if (showFirstLaunchDialog) {
            AlertDialog(
                onDismissRequest = { showFirstLaunchDialog = false },
                text = {
                    Text(
                        "Текущие категории с желаниями — примеры. Чтобы удалить или " +
                                "переименовать их используйте долгое нажатие.",
                        fontFamily = DelaGothicOneFont
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showFirstLaunchDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen)
                    ) {
                        Text(
                            "Понятно",
                            color = MainBlue,
                            fontFamily = DelaGothicOneFont
                        )
                    }
                }
            )
        }

        categories.forEach { category ->
            //отображение категории с действиями
            CategoryItem(
                category = category,
                onToggleExpand = { category.isExpanded = !category.isExpanded },//раскрытие/свертывание списка идей
                //добавление новой идеи в категорию
                onAddIdea = {
                    newIdeaCategory = category
                    newIdeaText = ""
                },
                //удаление категории
                onDeleteCategory = {
                    categories.remove(category)
                    saveData()
                },
                //выбор категории для переименования через долгое нажатие
                onRenameCategoryLongClick = {
                    renameCategoryTarget = category
                    renameText = category.name
                }
            )

            //категория раскрыта - показ списка с идеями
            if (category.isExpanded) {
                Column(modifier = Modifier.padding(start = 32.dp, top = 8.dp)) {
                    category.ideas.forEach { idea ->
                        //отображение отдельной идеи с действиями
                        IdeaItem(
                            idea = idea,
                            //статус выполнено/не выполнено
                            onToggleDone = {
                                idea.isDone = !idea.isDone
                                saveData()
                            },
                            //удаление идеи
                            onDeleteIdea = {
                                category.ideas.remove(idea)
                                saveData()
                            },
                            //выбор идеи для переименования через долгое нажатие
                            onRenameIdeaLongClick = {
                                renameIdeaTarget = idea
                                renameText = idea.text
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //добавление новой категории
        Button(
            onClick = {
                newCategoryDialog = true
                newCategoryName = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Добавить категорию",
                color = MainBlue,
                fontSize = 17.sp,
                fontFamily = DelaGothicOneFont
            )
        }
    }

    //выбрано создание категории - показываем окно ввода имени
    if (newCategoryDialog) {
        SimpleInputDialog(
            value = newCategoryName,//текущее значение ввода
            onValueChange = { newCategoryName = it }, //обновление текста
            onConfirm = {
                //введено не пустое имя - создание новой категории
                if (newCategoryName.isNotBlank()) {
                    val newCategory = Category(
                        id = (categories.maxOfOrNull { it.id } ?: 0) + 1,
                        name = newCategoryName,
                        isExpandedInitial = true
                    )
                    categories.add(newCategory)
                    saveData()
                }
                newCategoryDialog = false //закрытие диалога
            },
            onDismiss = { newCategoryDialog = false } //закрытие диалога без сохранения
        )
    }

    //выбрано создание идеи - показываем окно ввода текста
    if (newIdeaCategory != null) {
        val category = newIdeaCategory!!
        SimpleInputDialog(
            value = newIdeaText,
            onValueChange = { newIdeaText = it },
            onConfirm = {
                if (newIdeaText.isNotBlank()) {
                    category.ideas.add(Idea(category.ideas.size + 1, newIdeaText))
                    category.isExpanded = true
                    saveData()
                }
                newIdeaCategory = null
            },
            onDismiss = { newIdeaCategory = null }
        )
    }

    //выбрано переименование категории - показываем окно ввод текста
    if (renameCategoryTarget != null) {
        SimpleInputDialog(
            value = renameText,
            onValueChange = { renameText = it },
            onConfirm = {
                renameCategoryTarget!!.apply {
                    name = renameText
                    stateName = renameText
                }
                saveData()
                renameCategoryTarget = null
            },
            onDismiss = { renameCategoryTarget = null },
            confirmButtonText = "Сохранить"
        )
    }

    //выбрано переименование идеи - показываем окно ввод текста
    if (renameIdeaTarget != null) {
        SimpleInputDialog(
            value = renameText,
            onValueChange = { renameText = it },
            onConfirm = {
                renameIdeaTarget!!.apply {
                    text = renameText
                    stateText = renameText
                }
                saveData()
                renameIdeaTarget = null
            },
            onDismiss = { renameIdeaTarget = null },
            confirmButtonText = "Сохранить"
        )
    }
}

//окно с полем ввода и кнопкой подтверждения
@Composable
fun SimpleInputDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: String = "Добавить"
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    placeholder = null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen)
                ) {
                    Text(
                        confirmButtonText,
                        color = MainBlue,
                        fontFamily = DelaGothicOneFont)
                }
            }
        }
    }
}

//категория
@Composable
fun CategoryItem(
    category: Category,
    onToggleExpand: () -> Unit,
    onAddIdea: () -> Unit,
    onDeleteCategory: () -> Unit,
    onRenameCategoryLongClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }//показывать ли окно с действиями (удалить/переименовать)

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(color = Color(0xFF253A82), shape = RoundedCornerShape(24.dp))
                .combinedClickable(
                    onClick = onToggleExpand, //нажатие - раскрытие/сворачивание
                    onLongClick = { showDialog = true } //долгое нажатие - открытие окно с действиями
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.stateName,
                fontFamily = DelaGothicOneFont,
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            )

            Text(
                text = "+",
                fontFamily = DelaGothicOneFont,
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 16.dp).clickable { onAddIdea() }
            )
        }
        //меню с действиями (удалить/переименовать)
        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Box(
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Button(
                            onClick = {
                                onDeleteCategory()
                                showDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen)
                        ) {
                            Text(
                                "Удалить",
                                color = MainBlue,
                                fontFamily = DelaGothicOneFont
                            )
                        }

                        Button(
                            onClick = {
                                onRenameCategoryLongClick()
                                showDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen)
                        ) {
                            Text(
                                "Переименовать",
                                color = MainBlue,
                                fontFamily = DelaGothicOneFont
                            )
                        }
                    }
                }
            }
        }
    }
}

//идея
@Composable
fun IdeaItem(
    idea: Idea,
    onToggleDone: () -> Unit,
    onDeleteIdea: () -> Unit,
    onRenameIdeaLongClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }//показывать ли диалог с действиями (удалить/переименовать)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = {}, //одно нажатие - ничего не происходит
                onLongClick = { showDialog = true }//долгое нажатие - открыть окно с действиями
            )
    ) {
        //индикатор статуса выполнено/не выполнено
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, MainBlue, CircleShape)
                .background(
                    if (idea.stateDone) ButtonGreen else Color.White,
                    shape = CircleShape
                )
                .clickable {
                    idea.stateDone = !idea.stateDone
                    onToggleDone()
                }
        ) {
            if (idea.isDone) {
                Text(
                    "✔",
                    color = MainBlue,
                    fontSize = 16.sp,
                    fontFamily = DelaGothicOneFont
                )
            }
        }

        Text(
            text = idea.stateText,
            color = if (idea.stateDone) DoneBlue else MainBlue,
            fontSize = 17.sp,
            fontFamily = DelaGothicOneFont,
            modifier = Modifier.padding(start = 8.dp)
        )
    }

    //окно с действиями (удалить/переименовать)
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Box(
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Button(
                        onClick = {
                            onDeleteIdea()
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen)
                    ) {
                        Text(
                            "Удалить",
                            color = MainBlue,
                            fontFamily = DelaGothicOneFont
                        )
                    }

                    Button(
                        onClick = {
                            onRenameIdeaLongClick()
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen)
                    ) {
                        Text(
                            "Переименовать",
                            color = MainBlue,
                            fontFamily = DelaGothicOneFont
                        )
                    }
                }
            }
        }
    }
}