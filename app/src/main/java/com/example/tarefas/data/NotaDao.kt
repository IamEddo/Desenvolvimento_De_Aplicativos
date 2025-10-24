package com.example.tarefas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNota(nota: NotaEntity)

    @Delete
    suspend fun deleteNota(nota: NotaEntity) // Para exclusão permanente

    @Query("SELECT * FROM notas WHERE NOT isInTrash ORDER BY isPinned DESC, lastModified DESC")
    fun getAllNotasNaoExcluidas(): Flow<List<NotaEntity>>

    @Query("SELECT * FROM notas WHERE isInTrash ORDER BY trashTimestamp DESC")
    fun getNotasNaLixeira(): Flow<List<NotaEntity>>

    @Query("SELECT * FROM notas WHERE id = :notaId")
    fun getNotaById(notaId: Int): Flow<NotaEntity?>

    // Item 6: Pesquisa (simples por título e conteúdo)
    @Query("SELECT * FROM notas WHERE NOT isInTrash AND (title LIKE :query OR content LIKE :query) ORDER BY isPinned DESC, lastModified DESC")
    fun searchNotas(query: String): Flow<List<NotaEntity>>

    // Item 10: Lixeira
    @Query("UPDATE notas SET isInTrash = 1, trashTimestamp = :timestamp WHERE id = :notaId")
    suspend fun moverParaLixeira(notaId: Int, timestamp: Long)

    @Query("UPDATE notas SET isInTrash = 0, trashTimestamp = null WHERE id = :notaId")
    suspend fun restaurarDaLixeira(notaId: Int)

    // Item 7: Fixar
    @Query("UPDATE notas SET isPinned = :pinned WHERE id = :noteId")
    suspend fun setPinnedStatus(noteId: Int, pinned: Boolean)
}