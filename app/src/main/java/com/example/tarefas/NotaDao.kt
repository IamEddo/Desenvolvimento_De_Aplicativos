package com.example.tarefas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@androidx.room.Dao
interface NotaDao {
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNota(nota: NotaEntity)

    @androidx.room.Delete
    suspend fun deleteNota(nota: NotaEntity) // Para exclusão permanente

    @androidx.room.Query("SELECT * FROM notas WHERE NOT isInTrash ORDER BY isPinned DESC, lastModified DESC")
    fun getAllNotasNaoExcluidas(): Flow<List<NotaEntity>>

    @androidx.room.Query("SELECT * FROM notas WHERE isInTrash ORDER BY trashTimestamp DESC")
    fun getNotasNaLixeira(): Flow<List<NotaEntity>>

    @androidx.room.Query("SELECT * FROM notas WHERE id = :notaId")
    fun getNotaById(notaId: Int): Flow<NotaEntity?>

    // Item 6: Pesquisa (simples por título e conteúdo)
    @androidx.room.Query("SELECT * FROM notas WHERE NOT isInTrash AND (title LIKE :query OR content LIKE :query) ORDER BY isPinned DESC, lastModified DESC")
    fun searchNotas(query: String): Flow<List<NotaEntity>>

    // Item 10: Lixeira
    @androidx.room.Query("UPDATE notas SET isInTrash = 1, trashTimestamp = :timestamp WHERE id = :notaId")
    suspend fun moverParaLixeira(notaId: Int, timestamp: Long)

    @androidx.room.Query("UPDATE notas SET isInTrash = 0, trashTimestamp = null WHERE id = :notaId")
    suspend fun restaurarDaLixeira(notaId: Int)

    // Item 7: Fixar
    @androidx.room.Query("UPDATE notas SET isPinned = :pinned WHERE id = :noteId")
    suspend fun setPinnedStatus(noteId: Int, pinned: Boolean)
}