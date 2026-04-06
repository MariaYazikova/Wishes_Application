package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.DelaGothicOneFont
import androidx.compose.ui.Alignment
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

//начальный экран
@Composable
fun IntroScreen(onStartClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //вступление
        Text(
            buildAnnotatedString {
                append("Иногда мы забываем о своих желаниях, потому что не фиксируем их.\n\n")
                append("Мимолётные идеи, спонтанные мечты, странные, но интересные мысли — всё это может затеряться и забыться.\n\n")
                append("Сохраняй их и напоминай себе о том, ")

                withStyle(
                    style = SpanStyle(color = Color(0xFF5A6FD6))
                ) {
                    append("чего ты действительно хочешь")
                }
            },
            fontSize = 20.sp,
            color = Color(0xFF253A82),
            fontFamily = DelaGothicOneFont,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onStartClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3FC87)),
            modifier = Modifier.padding(top = 40.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                "Начать",
                fontFamily = DelaGothicOneFont,
                fontSize = 17.sp,
                color = Color(0xFF253A82),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}