package com.example.tarefas.data

import kotlinx.coroutines.flow.Flow

// O repositório serve como uma camada intermediária entre o ViewModel e a fonte de dados (DAO).
class NotaRepository(private val notaDao: NotaDao) {

    // Expõe o Flow de notas diretamente do DAO. O ViewModel irá coletar isso.
    val todasAsNotasNaoExcluidas: Flow<List<NotaEntity>> = notaDao.getAllNotasNaoExcluidas()
    val todasAsNotasNaLixeira: Flow<List<NotaEntity>> = notaDao.getNotasNaLixeira()

    // Função para inserir ou atualizar uma nota. O DAO já lida com o conflito.
    suspend fun upsert(nota: NotaEntity) {
        notaDao.insertOrUpdateNota(nota)
    }

    // Função para obter uma nota específica pelo ID.
    fun getNotaById(id: Int): Flow<NotaEntity?> {
        return notaDao.getNotaById(id)
    }

    // Função para mover uma nota para a lixeira.
    suspend fun moverParaLixeira(nota: NotaEntity) {
        val notaAtualizada = nota.copy(
            isInTrash = true,
            trashTimestamp = System.currentTimeMillis()
        )
        notaDao.insertOrUpdateNota(notaAtualizada)
    }

    // Função para restaurar uma nota da lixeira.
    suspend fun restaurarDaLixeira(nota: NotaEntity) {
        val notaAtualizada = nota.copy(
            isInTrash = false,
            trashTimestamp = null
        )
        notaDao.insertOrUpdateNota(notaAtualizada)
    }

    // Função para excluir uma nota permanentemente.
    suspend fun excluirPermanente(nota: NotaEntity) {
        notaDao.deleteNota(nota)
    }

    // Função para fixar ou desafixar uma nota.
    suspend fun togglePin(nota: NotaEntity) {
        val notaAtualizada = nota.copy(isPinned = !nota.isPinned)
        notaDao.insertOrUpdateNota(notaAtualizada)
    }
}
