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
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage // Para exibir imagens de anexos
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

    var title by remember(notaState) { mutableStateOf(notaState?.title ?: "") }
    var content by remember(notaState) { mutableStateOf(notaState?.content ?: "") }
    var selectedColorHex by remember(notaState) { mutableStateOf(notaState?.colorHex) }
    val attachments by remember(notaState) { mutableStateOf(notaState?.attachments?.toMutableList() ?: mutableListOf()) }

    val context = LocalContext.current

    // Para Item 15: Seletor de Anexos
    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent() // Para um único arquivo
        // contract = ActivityResultContracts.OpenMultipleDocuments() // Para múltiplos
    ) { uri: Uri? ->
        uri?.let {
            if (!isNewNote) { // Só pode adicionar anexo a uma nota existente
                viewModel.adicionarAnexo(noteId, it)
                // O ideal seria o ViewModel atualizar o Flow da nota, e attachments aqui ser um collectAsState
            } else {
                // Informar usuário que precisa salvar a nota primeiro
            }
        }
    }

    // Cores predefinidas para Item 17
    val predefinedColors = listOf(
        null, // Sem cor
        "#FFFFFF", // Branco (pode precisar de borda se o fundo for branco)
        "#FFCDD2", // Rosa claro
        "#FFF9C4", // Amarelo claro
        "#C8E6C9", // Verde claro
        "#BBDEFB", // Azul claro
        "#D1C4E9"  // Roxo claro
    )

    LaunchedEffect(key1 = noteId) {
        if (!isNewNote && noteId != (notaState?.id ?: -1)) {
            // Recarregar a nota se o ID mudar (raro, mas para segurança)
        }
    }


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
                        val currentAttachments = if (isNewNote) emptyList() else notaState?.attachments ?: emptyList()
                        viewModel.salvarNota(
                            id = if (isNewNote) 0 else noteId,
                            titulo = title,
                            conteudo = content,
                            isPinned = notaState?.isPinned ?: false,
                            attachments = currentAttachments, // attachments aqui são os recuperados do DB
                            colorHex = selectedColorHex
                        )
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.Done, contentDescription = "Salvar")
                    }
                }
            )
        }
    ) { paddingValues ->
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

            // Item 8: Formatação de Texto (MUITO BÁSICO - apenas o campo de texto)
            // A formatação rica requer um editor de texto rico ou manipulação de AnnotatedString.
            // Por agora, um TextField simples.
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Conteúdo da nota...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 200.dp) // Altura mínima
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Item 17: Seletor de Cor da Nota
            Text("Cor da Nota:", style = MaterialTheme.typography.titleSmall)
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
                            .clickable { selectedColorHex = colorHex }
                    ) {
                        if (colorHex == null) {
                            Icon(Icons.Filled.FormatColorReset, contentDescription = "Sem cor", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            // Item 15: Anexos
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Anexos:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (!isNewNote) { // Só permite anexar se a nota já existe
                    IconButton(onClick = { attachmentLauncher.launch("*/*") }) {
                        Icon(Icons.Filled.AttachFile, contentDescription = "Anexar Arquivo")
                    }
                } else {
                    Text("(Salve a nota para adicionar anexos)", style = MaterialTheme.typography.bodySmall)
                }
            }

            // É importante que 'attachments' aqui reflita o estado atual da nota no ViewModel
            // Se você adiciona um anexo e o ViewModel atualiza o Flow da nota,
            // 'notaState' deve ser recomposto, e 'attachments' aqui deve refletir isso.
            // Para simplificar, estamos usando o 'attachments' do 'notaState' inicial.
            // Para uma atualização em tempo real, você precisaria observar o Flow da nota específica.
            val currentNoteAttachments = notaState?.attachments ?: emptyList()
            if (currentNoteAttachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                currentNoteAttachments.forEach { path ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tenta exibir imagem, senão ícone genérico
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
                        IconButton(onClick = {
                            if (!isNewNote) viewModel.removerAnexo(noteId, path)
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remover Anexo")
                        }
                    }
                }
            }
        }
    }
}
