package com.example.tarefas.ui

import android.app.Application
import android.net.Uri // Importar Uri
import android.util.Log // Importar Log para placeholders
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarefas.data.NotaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File // Para manipulação de arquivos (exemplo)
import java.io.FileOutputStream // Para manipulação de arquivos (exemplo)
import java.io.IOException // Para tratamento de exceções de arquivo (exemplo)

open class NotaViewModel(application: Application) : AndroidViewModel(application) {

    // --- Suas propriedades e funções existentes ---
    open val notasVisiveis: StateFlow<List<NotaEntity>> = MutableStateFlow(emptyList())
    open val searchQuery: StateFlow<String> = MutableStateFlow("")
    open val notasNaLixeira: StateFlow<List<NotaEntity>> = MutableStateFlow(emptyList())

    open fun getNotaById(id: Int): StateFlow<NotaEntity?> {
        Log.d("NotaViewModel", "CHAMADO: getNotaById para id $id. Retornando null por enquanto.")
        // TODO: Implementar busca da nota pelo ID (ex: via _notaRepository.getNotaByIdFlow(id))
        return MutableStateFlow(null)
    }

    // Renomeado para 'upsertNotaComObjeto' para evitar conflito com 'salvarNota'
    // Se 'salvarNota' é o método principal, você pode remover este ou refatorar.
    open fun upsertNotaComObjeto(nota: NotaEntity) {
        viewModelScope.launch {
            Log.d("NotaViewModel", "CHAMADO: upsertNotaComObjeto para nota ${nota.title}")
            // TODO: Implementar lógica de inserção/atualização com objeto NotaEntity completo
            // Ex: if (nota.id == 0) _notaRepository.inserir(nota) else _notaRepository.atualizar(nota)
        }
    }

    open fun moverParaLixeira(nota: NotaEntity) {
        viewModelScope.launch {
            Log.d("NotaViewModel", "CHAMADO: moverParaLixeira para nota ${nota.title}")
            // TODO: Implementar lógica de mover para lixeira
            // Ex: _notaRepository.atualizar(nota.copy(isInTrash = true, trashTimestamp = System.currentTimeMillis()))
        }
    }

    open fun restaurarDaLixeira(nota: NotaEntity) {
        viewModelScope.launch {
            Log.d("NotaViewModel", "CHAMADO: restaurarDaLixeira para nota ${nota.title}")
            // TODO: Implementar lógica de restaurar da lixeira
            // Ex: _notaRepository.atualizar(nota.copy(isInTrash = false, trashTimestamp = null))
        }
    }

    open fun excluirPermanente(nota: NotaEntity) {
        viewModelScope.launch {
            Log.d("NotaViewModel", "CHAMADO: excluirPermanente para nota ${nota.title}")
            // TODO: Implementar lógica de exclusão permanente
            // Ex: _notaRepository.excluir(nota)
            // Opcional: excluir anexos do sistema de arquivos
        }
    }

    open fun togglePinNota(nota: NotaEntity) {
        viewModelScope.launch {
            val novaNota = nota.copy(isPinned = !nota.isPinned)
            Log.d("NotaViewModel", "CHAMADO: togglePinNota para nota ${novaNota.title}, novo estado Pinned: ${novaNota.isPinned}")
            // TODO: Implementar lógica de atualizar o estado 'pinned'
            // Ex: _notaRepository.atualizar(novaNota)
        }
    }

    open fun onSearchQueryChanged(query: String) {
        (searchQuery as? MutableStateFlow<String>)?.value = query
        Log.d("NotaViewModel", "CHAMADO: onSearchQueryChanged com query: $query")
        // TODO: Implementar lógica de filtragem de notasVisiveis com base na query
    }

    open fun updateNotaColor(notaId: Int, colorHex: String?) {
        viewModelScope.launch {
            Log.d("NotaViewModel", "CHAMADO: updateNotaColor para nota $notaId com cor $colorHex")
            // TODO: Implementar lógica de atualizar a cor da nota
            // Ex: val nota = _notaRepository.getNotaById(notaId)
            //     nota?.let { _notaRepository.atualizar(it.copy(colorHex = colorHex)) }
        }
    }

    // --- Novas funções adicionadas e assinaturas corrigidas ---

    /**
     * Adiciona um anexo a uma nota existente.
     * Esta função deve:
     * 1. Copiar o arquivo da Uri para o armazenamento interno do aplicativo.
     * 2. Obter o caminho do arquivo copiado.
     * 3. Atualizar a entidade da nota no banco de dados com o novo caminho do anexo.
     * 4. Idealmente, fazer com que o StateFlow da nota (ou lista de notas) seja atualizado.
     */
    open fun adicionarAnexo(notaId: Int, anexoUri: Uri) {
        viewModelScope.launch {
            Log.d("NotaViewModel", "CHAMADO: adicionarAnexo para nota $notaId com uri $anexoUri")
            // Placeholder para lógica de cópia de arquivo e atualização do DB
            // Esta é uma implementação MUITO simplificada e requer tratamento robusto de erros e arquivos.
            val appContext = getApplication<Application>().applicationContext
            val fileName = "anexo_${System.currentTimeMillis()}_${anexoUri.lastPathSegment ?: "file"}"
            val destinationFile = File(appContext.filesDir, fileName)

            try {
                appContext.contentResolver.openInputStream(anexoUri)?.use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                val anexoPath = destinationFile.absolutePath
                Log.d("NotaViewModel", "Anexo salvo em: $anexoPath")
                // TODO: Chamar o repositório para adicionar o anexoPath à nota com notaId
                // Ex: _notaRepository.adicionarAnexo(notaId, anexoPath)
                //      Isso implicaria que seu repositório tem um método para buscar a nota,
                //      adicionar o path à lista de attachments e atualizar a nota.
            } catch (e: IOException) {
                Log.e("NotaViewModel", "Erro ao salvar anexo", e)
                // Tratar erro (ex: mostrar mensagem ao usuário)
            }
        }
    }

    /**
     * Salva (insere ou atualiza) uma nota.
     */
    open fun salvarNota(
        id: Int,
        titulo: String,
        conteudo: String,
        isPinned: Boolean,
        attachments: List<String>, // A lista de anexos atual da nota
        colorHex: String?
        // Adicione outros campos da NotaEntity se necessário (category, etc.)
    ) {
        viewModelScope.launch {
            Log.d("NotaViewModel", "CHAMADO: salvarNota para id $id, titulo $titulo")

            // Se for uma atualização, você pode querer buscar a nota existente para preservar
            // campos não editáveis diretamente na tela (ex: isInTrash, trashTimestamp, category)
            // Se o ID é 0 (ou o seu indicador de nova nota), criamos uma nova.
            val notaParaSalvar = if (id != 0) { // Supõe que 0 é para nova nota, ajuste se necessário
                // Lógica para buscar a nota existente e atualizá-la
                // val notaExistente = _notaRepository.getNotaById(id) // Método síncrono ou coletar de um Flow
                // notaExistente?.copy(...) ou criar um novo objeto com o ID existente
                // Este é um placeholder, você precisa de uma lógica real aqui
                getNotaById(id).value?.copy( // Usando o getNotaById existente como exemplo, mas pode ser problemático
                    title = titulo,
                    content = conteudo,
                    isPinned = isPinned,
                    attachments = attachments, // Assume que 'attachments' já é a lista correta
                    colorHex = colorHex,
                    lastModified = System.currentTimeMillis()
                ) ?: NotaEntity( // Fallback para nova nota se não encontrar, mas deveria ser tratado
                    id = 0, // Ou id se você quer garantir que está atualizando
                    title = titulo,
                    content = conteudo,
                    isPinned = isPinned,
                    attachments = attachments,
                    colorHex = colorHex,
                    lastModified = System.currentTimeMillis()
                )
            } else {
                NotaEntity(
                    // id = 0, Room gerará o ID se for 0
                    title = titulo,
                    content = conteudo,
                    isPinned = isPinned,
                    attachments = attachments,
                    colorHex = colorHex,
                    lastModified = System.currentTimeMillis()
                    // Outros campos da NotaEntity podem ter valores padrão
                )
            }

            Log.d("NotaViewModel", "Nota para salvar: $notaParaSalvar")
            // TODO: Chamar _notaRepository.inserir(notaParaSalvar) ou _notaRepository.atualizar(notaParaSalvar)
            // Ex: if (notaParaSalvar.id == 0) _notaRepository.inserir(notaParaSalvar) else _notaRepository.atualizar(notaParaSalvar)
        }
    }


    /**
     * Remove um anexo de uma nota.
     * Esta função deve:
     * 1. Remover o caminho do anexo da entidade da nota no banco de dados.
     * 2. Opcionalmente, excluir o arquivo físico do armazenamento interno.
     * 3. Atualizar o StateFlow relevante.
     */
    open fun removerAnexo(notaId: Int, anexoPath: String) { // Assinatura corrigida para anexoPath como String
        viewModelScope.launch {
            Log.d("NotaViewModel", "CHAMADO: removerAnexo para nota $notaId, path $anexoPath")
            // TODO: Implementar lógica de remoção do DB
            // Ex: _notaRepository.removerAnexo(notaId, anexoPath)

            // Opcional: Excluir o arquivo físico
            try {
                val fileToDelete = File(anexoPath)
                if (fileToDelete.exists()) {
                    if (fileToDelete.delete()) {
                        Log.d("NotaViewModel", "Arquivo de anexo excluído: $anexoPath")
                    } else {
                        Log.w("NotaViewModel", "Falha ao excluir arquivo de anexo: $anexoPath")
                    }
                }
            } catch (e: Exception) {
                Log.e("NotaViewModel", "Erro ao excluir arquivo de anexo", e)
            }
        }
    }

    // Removido addAttachmentToNota e removeAttachmentFromNota com 'attachmentUri: String'
    // pois as chamadas em EditNoteScreen usam Uri para adicionar e path (String) para remover.
    // Se você ainda precisar deles com essas assinaturas específicas, pode adicioná-los de volta.
}

