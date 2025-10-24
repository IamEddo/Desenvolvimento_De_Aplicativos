@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tarefas

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
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

        override fun moverParaLixeira(nota: NotaEntity) {}
        override fun restaurarDaLixeira(nota: NotaEntity) {}
        override fun excluirPermanente(nota: NotaEntity) {}
        override fun togglePinNota(nota: NotaEntity) {}
        override fun onSearchQueryChanged(query: String) {}
        override fun updateNotaColor(notaId: Int, colorHex: String?) {}
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
