// ui/components/ResultBanner.kt
package com.pip.cheeseroul.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pip.cheeseroul.model.DisplayMode
import com.pip.cheeseroul.model.Player
import com.pip.cheeseroul.ui.theme.CheeseBrown
import com.pip.cheeseroul.ui.theme.CheeseCardBg

@Composable
fun ResultBanner(
    player: Player,
    displayMode: DisplayMode,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CheeseCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(player.color)
            )

            Spacer(modifier = Modifier.width(16.dp))

            val label = when (displayMode) {
                DisplayMode.COLOR_ONLY -> "Выбран цвет!"
                DisplayMode.COLOR_AND_NUMBER -> "Игрок №${player.id}"
                DisplayMode.COLOR_AND_NAME -> player.name
            }

            Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = CheeseBrown
            )
        }
    }
}