package com.example.tarefas.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tarefas.data.NotaEntity
import com.example.tarefas.ui.NotaViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    navController: NavController,
    viewModel: NotaViewModel,
    noteId: Int
) {
    val isNewNote = noteId == -1
    val notaState by if (isNewNote) {
        remember { mutableStateOf<NotaEntity?>(null) }
    } else {
        viewModel.getNotaById(noteId).collectAsState(initial = null)
    }

    // Variáveis de estado para os campos editáveis
    var title by remember(notaState?.id) { mutableStateOf(notaState?.title ?: "") }
    var content by remember(notaState?.id) { mutableStateOf(notaState?.content ?: "") }
    var selectedColorHex by remember(notaState?.id) { mutableStateOf(notaState?.colorHex) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewNote) "Nova Nota" else "Editar Nota") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (!isNewNote) {
                        IconButton(onClick = {
                            notaState?.let { viewModel.togglePinNota(it) }
                        }) {
                            Icon(
                                if (notaState?.isPinned == true) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Fixar/Desafixar"
                            )
                        }
                    }
                    IconButton(onClick = {
                        // --- CORREÇÃO APLICADA AQUI ---
                        val notaParaSalvar = if (isNewNote) {
                            // Cria uma nova nota
                            NotaEntity(
                                title = title,
                                content = content,
                                colorHex = selectedColorHex
                                // Outros campos terão valores padrão
                            )
                        } else {
                            // Atualiza a nota existente, preservando campos não editáveis
                            notaState!!.copy(
                                title = title,
                                content = content,
                                colorHex = selectedColorHex,
                                lastModified = System.currentTimeMillis()
                            )
                        }
                        // Chama a única função 'salvarNota' que aceita um objeto NotaEntity
                        viewModel.salvarNota(notaParaSalvar)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.Done, contentDescription = "Salvar")
                    }
                }
            )
        }
    ) { paddingValues ->
        // ... O resto do seu código (Column, OutlinedTextField, etc.) permanece o mesmo ...
        // Nenhuma alteração necessária abaixo desta linha
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Para rolar se o conteúdo for grande
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Conteúdo da nota...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Seletor de Cor
            Text("Cor da Nota:", style = MaterialTheme.typography.titleSmall)
            ColorSelector(predefinedColors = listOf(null, "#FFFFFF", "#FFCDD2", "#FFF9C4", "#C8E6C9", "#BBDEFB", "#D1C4E9"), selectedColorHex = selectedColorHex) { newColor ->
                selectedColorHex = newColor
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Seção de Anexos
            AttachmentSection(
                noteState = notaState,
                isNewNote = isNewNote,
                onAddAttachment = { uri -> if (!isNewNote) viewModel.adicionarAnexo(noteId, uri) },
                onRemoveAttachment = { path -> if (!isNewNote) viewModel.removerAnexo(noteId, path) }
            )
        }
    }
}

// Para melhor organização, podemos extrair a lógica de UI em Composables menores
@Composable
private fun ColorSelector(
    predefinedColors: List<String?>,
    selectedColorHex: String?,
    onColorSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(predefinedColors) { colorHex ->
            val color = colorHex?.let { try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { Color.Transparent } } ?: Color.Transparent
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (colorHex == null) MaterialTheme.colorScheme.surfaceVariant else color)
                    .border(
                        width = 2.dp,
                        color = if (selectedColorHex == colorHex) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(colorHex) }
            ) {
                if (colorHex == null) {
                    Icon(Icons.Filled.FormatColorReset, contentDescription = "Sem cor", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
private fun AttachmentSection(
    noteState: NotaEntity?,
    isNewNote: Boolean,
    onAddAttachment: (Uri) -> Unit,
    onRemoveAttachment: (String) -> Unit
) {
    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAddAttachment(it) }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Anexos:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        if (!isNewNote) {
            IconButton(onClick = { attachmentLauncher.launch("*/*") }) {
                Icon(Icons.Filled.AttachFile, contentDescription = "Anexar Arquivo")
            }
        } else {
            Text("(Salve a nota para adicionar anexos)", style = MaterialTheme.typography.bodySmall)
        }
    }

    val currentNoteAttachments = noteState?.attachments ?: emptyList()
    if (currentNoteAttachments.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        currentNoteAttachments.forEach { path ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (path.endsWith(".jpg", true) || path.endsWith(".png", true) || path.endsWith(".jpeg", true)) {
                    AsyncImage(
                        model = File(path),
                        contentDescription = "Anexo ${File(path).name}",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                } else {
                    Icon(Icons.Filled.InsertDriveFile, contentDescription = "Arquivo", modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(File(path).name, modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemoveAttachment(path) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Remover Anexo")
                }
            }
        }
    }
}
