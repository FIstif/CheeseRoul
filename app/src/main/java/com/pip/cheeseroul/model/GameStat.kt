// model/GameStat.kt
package com.pip.cheeseroul.model

import java.util.UUID

data class GameStat(
    val id: String = UUID.randomUUID().toString(),
    val dateMs: Long = System.currentTimeMillis(), // Дата игры
    val durationMs: Long,                          // Длительность в миллисекундах
    val initialPlayerCount: Int,                   // Сколько было в начале
    val winner: Player,                            // Победитель
    val firstEliminated: Player?,                  // Кто вылетел первым
    val spinCounts: Map<Int, Int>                  // Кто сколько раз выпадал (ID -> кол-во)
)