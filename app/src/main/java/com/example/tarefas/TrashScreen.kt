package com.example.tarefas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tarefas.data.NotaEntity
import com.example.tarefas.ui.NotaViewModel
import kotlin.text.forEach
import kotlin.text.format
import kotlin.text.isNotEmpty
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    navController: NavController,
    viewModel: NotaViewModel
) {
    val notasNaLixeira by viewModel.notasNaLixeira.collectAsState(initial = emptyList())
    var showConfirmDialog by remember { mutableStateOf<NotaEntity?>(null) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lixeira") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (notasNaLixeira.isNotEmpty()) {
                        TextButton(onClick = {
                            // Adicionar confirmação para esvaziar lixeira inteira
                            notasNaLixeira.forEach { viewModel.excluirPermanente(it) }
                        }) {
                            Text("Esvaziar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            if (notasNaLixeira.isEmpty()) {
                Text(
                    "A lixeira está vazia.",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(notasNaLixeira, key = { it.id }) { nota ->
                        TrashNotaItem(
                            nota = nota,
                            onRestore = { viewModel.restaurarDaLixeira(nota) },
                            onDeletePermanently = { showConfirmDialog = nota }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    // Diálogo de confirmação para exclusão permanente
    showConfirmDialog?.let { notaParaExcluir ->
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text("Excluir Permanentemente?") },
            text = { Text("A nota \"${notaParaExcluir.title}\" será excluída permanentemente. Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.excluirPermanente(notaParaExcluir)
                    showConfirmDialog = null
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun TrashNotaItem(
    nota: NotaEntity,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(nota.title, style = MaterialTheme.typography.titleMedium)
            nota.trashTimestamp?.let {
                Text("Excluído em: ${java.text.SimpleDateFormat("dd/MM/yy HH:mm",
                    Locale.getDefault()).format(java.util.Date(it))}", style = MaterialTheme.typography.bodySmall)
            }
        }
        IconButton(onClick = onRestore) {
            Icon(Icons.Filled.RestoreFromTrash, contentDescription = "Restaurar")
        }
        IconButton(onClick = onDeletePermanently) {
            Icon(Icons.Filled.DeleteForever, contentDescription = "Excluir Permanentemente")
        }
    }
}