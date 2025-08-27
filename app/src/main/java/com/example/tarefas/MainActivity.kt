@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tarefas

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
// import androidx.activity.viewModels // Para by viewModels() - Descomente se usar
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.runtime.collectAsState // Descomente se precisar
// import androidx.compose.runtime.getValue // Descomente se precisar
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel // Para viewModel()
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tarefas.data.NotaEntity
import com.example.tarefas.ui.NotaViewModel
import com.example.tarefas.ui.theme.TarefasTheme
import com.example.tarefas.ui.screens.EditNoteScreen
import com.example.tarefas.ui.screens.NoteListScreen
import com.example.tarefas.ui.screens.TrashScreen
import com.example.tarefas.ui.theme.DarkColorScheme
import com.example.tarefas.ui.theme.LightColorScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object Routes {
    const val NOTE_LIST = "note_list"
    const val EDIT_NOTE = "edit_note"
    const val TRASH = "trash"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppPrincipal()
        }
    }
}

@Composable
fun AppPrincipal(notaViewModel: NotaViewModel = viewModel()) {
    val navController = rememberNavController()
    val useDarkTheme = isSystemInDarkTheme()

    TarefasTheme(darkTheme = useDarkTheme) {
        NavHost(navController = navController, startDestination = Routes.NOTE_LIST) {
            composable(Routes.NOTE_LIST) {
                NoteListScreen(
                    navController = navController,
                    viewModel = notaViewModel
                )
            }
            composable(
                route = "${Routes.EDIT_NOTE}?noteId={noteId}",
                arguments = listOf(navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
                EditNoteScreen(
                    navController = navController,
                    viewModel = notaViewModel,
                    noteId = noteId
                )
            }
            composable(Routes.TRASH) {
                TrashScreen(
                    navController = navController,
                    viewModel = notaViewModel
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TarefasTheme {}
}

@Preview(showBackground = true, name = "App Principal Preview")
@Composable
fun AppPrincipalPreview() {
    class FakeNotaViewModel(application: Application) : NotaViewModel(application) {
        override val notasVisiveis: StateFlow<List<NotaEntity>> =
            MutableStateFlow(
                listOf(
                    NotaEntity(id = 1, title = "Nota de Exemplo no Preview", content = "Este é o conteúdo da nota de exemplo para o preview.", isPinned = true, colorHex = "#FF amarelo", lastModified = System.currentTimeMillis()),
                    NotaEntity(id = 2, title = "Outra Nota", content = "Mais conteúdo aqui.", colorHex = "#ADD8E6", lastModified = System.currentTimeMillis())
                )
            )
        override val searchQuery: StateFlow<String> = MutableStateFlow("")
        override val notasNaLixeira: StateFlow<List<NotaEntity>> =
            MutableStateFlow(emptyList())

        override fun getNotaById(id: Int): StateFlow<NotaEntity?> =
            MutableStateFlow(null)

        // DECISÃO AQUI:
        // Se NotaViewModel.kt tem 'open fun upsertNotaComObjeto(nota: NotaEntity)', então use:
        // override fun upsertNotaComObjeto(nota: NotaEntity) {}
        // Se NotaViewModel.kt tem 'open fun upsertNota(nota: NotaEntity)' (e não upsertNotaComObjeto), então mantenha:
        override fun upsertNotaComObjeto(nota: NotaEntity) {} // Verifique se isso corresponde ao NotaViewModel.kt

        // Sobrescreva salvarNota se esta é a principal forma de salvar
        override fun salvarNota(
            id: Int,
            titulo: String,
            conteudo: String,
            isPinned: Boolean,
            attachments: List<String>,
            colorHex: String?
        ) {
            Log.d("FakeNotaViewModel", "salvarNota chamada no fake: id=$id, titulo=$titulo")
            // Você pode adicionar lógica aqui para modificar 'notasVisiveis' se quiser ver o efeito no preview
        }

        override fun moverParaLixeira(nota: NotaEntity) {}
        override fun restaurarDaLixeira(nota: NotaEntity) {}
        override fun excluirPermanente(nota: NotaEntity) {}
        override fun togglePinNota(nota: NotaEntity) {}
        override fun onSearchQueryChanged(query: String) {}
        override fun updateNotaColor(notaId: Int, colorHex: String?) {}

        // Assinatura corrigida para corresponder à de NotaViewModel.kt
        // O segundo parâmetro agora é Uri, e o nome da função é 'adicionarAnexo'
        override fun adicionarAnexo(notaId: Int, anexoUri: android.net.Uri) { // Note o android.net.Uri
            Log.d("FakeNotaViewModel", "adicionarAnexo chamada no fake: notaId=$notaId, uri=$anexoUri")
        }

        // Nome e assinatura corrigidos para corresponder à de NotaViewModel.kt
        override fun removerAnexo(notaId: Int, anexoPath: String) {
            Log.d("FakeNotaViewModel", "removerAnexo chamada no fake: notaId=$notaId, path=$anexoPath")
        }
    }

    val context = LocalContext.current
    val fakeApp = try {
        context.applicationContext as Application
    } catch (e: Exception) {
        Application()
    }

    val fakeViewModel =
        remember { FakeNotaViewModel(fakeApp) }

    TarefasTheme {
        AppPrincipal(notaViewModel = fakeViewModel)
    }
}
