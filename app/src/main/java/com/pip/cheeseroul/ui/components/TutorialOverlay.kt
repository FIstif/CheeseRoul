// ui/components/TutorialOverlay.kt
package com.pip.cheeseroul.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pip.cheeseroul.ui.theme.CheeseBrown
import com.pip.cheeseroul.ui.theme.CheeseCardBg
import com.pip.cheeseroul.ui.theme.CheeseOrange

@Composable
fun TutorialOverlay(
    text: String,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    // Блокируем клики по элементам под оверлеем
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CheeseCardBg),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💡 Подсказка",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CheeseOrange
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = text,
                    fontSize = 16.sp,
                    color = CheeseBrown,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = CheeseOrange),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Понятно", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onSkip) {
                    Text("Пропустить обучение", color = Color.Gray)
                }
            }
        }
    }
}