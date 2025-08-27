package com.example.tarefas.data // Ou o nome do seu pacote

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "notas")
@TypeConverters(StringListConverter::class) // Para List<String>
data class NotaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String,
    var content: String, // Para item 8 (Formatação), por enquanto texto simples
    var lastModified: Long = System.currentTimeMillis(),
    var isPinned: Boolean = false,        // Item 7
    var category: String? = null,       // Item 7 (Simples)
    // var tags: List<String> = emptyList(), // Item 7 (Adiaremos para simplificar inicialmente)
    var isInTrash: Boolean = false,       // Item 10
    var trashTimestamp: Long? = null,   // Item 10
    var attachments: List<String> = emptyList(), // Item 15
    var colorHex: String? = null        // Item 17
)

class StringListConverter {
    @androidx.room.TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @androidx.room.TypeConverter
    fun toString(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
}
