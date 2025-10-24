package com.example.tarefas.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarefas.data.AppDatabase
import com.example.tarefas.data.NotaEntity
import com.example.tarefas.data.NotaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
open class NotaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotaRepository
    private val _searchQuery = MutableStateFlow("")
    open val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        val notaDao = AppDatabase.getDatabase(application).notaDao()
        repository = NotaRepository(notaDao)
    }

    open fun adicionarAnexo(noteId: Int, uri: Uri) {
        viewModelScope.launch {
            val nota = getNotaById(noteId).first()
            if (nota != null) {
                val newAttachments = nota.attachments.toMutableList().apply {
                    add(uri.toString())
                }
                repository.upsert(nota.copy(attachments = newAttachments))
            }
        }
    }

    // --- CÓDIGO ADICIONADO AQUI ---
    /**
     * Remove um anexo de uma nota existente.
     */
    open fun removerAnexo(notaId: Int, anexoPath: String) {
        viewModelScope.launch {
            // Busca a nota atual pelo ID
            val nota = getNotaById(notaId).first()
            if (nota != null) {
                // Cria uma nova lista de anexos sem o anexo a ser removido
                val novosAnexos = nota.attachments.toMutableList().apply {
                    remove(anexoPath)
                }
                // Salva a nota atualizada com a nova lista de anexos
                repository.upsert(nota.copy(attachments = novosAnexos))

                // Opcional mas recomendado: Excluir o arquivo físico para economizar espaço
                try {
                    val arquivoParaExcluir = File(anexoPath)
                    if (arquivoParaExcluir.exists()) {
                        arquivoParaExcluir.delete()
                    }
                } catch (e: Exception) {
                    // Logar o erro se a exclusão do arquivo falhar
                    Log.e("NotaViewModel", "Erro ao excluir arquivo de anexo: $anexoPath", e)
                }
            }
        }
    }
    // --- FIM DO CÓDIGO ADICIONADO ---

    open val notasVisiveis: StateFlow<List<NotaEntity>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.todasAsNotasNaoExcluidas
            } else {
                repository.todasAsNotasNaoExcluidas.map { notas ->
                    notas.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.content.contains(query, ignoreCase = true)
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    open val notasNaLixeira: StateFlow<List<NotaEntity>> = repository.todasAsNotasNaLixeira
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    open fun getNotaById(id: Int): Flow<NotaEntity?> {
        return repository.getNotaById(id)
    }

    open fun salvarNota(nota: NotaEntity) {
        viewModelScope.launch {
            repository.upsert(nota)
        }
    }

    open fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    open fun moverParaLixeira(nota: NotaEntity) {
        viewModelScope.launch {
            repository.moverParaLixeira(nota)
        }
    }

    open fun restaurarDaLixeira(nota: NotaEntity) {
        viewModelScope.launch {
            repository.restaurarDaLixeira(nota)
        }
    }

    open fun excluirPermanente(nota: NotaEntity) {
        viewModelScope.launch {
            repository.excluirPermanente(nota)
        }
    }

    open fun togglePinNota(nota: NotaEntity) {
        viewModelScope.launch {
            repository.togglePin(nota)
        }
    }

    open fun updateNotaColor(notaId: Int, colorHex: String?) {
        viewModelScope.launch {
            getNotaById(notaId).firstOrNull()?.let { nota ->
                val notaAtualizada = nota.copy(colorHex = colorHex)
                repository.upsert(notaAtualizada)
            }
        }
    }
}
