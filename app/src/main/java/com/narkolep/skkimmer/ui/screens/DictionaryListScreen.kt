package com.narkolep.skkimmer.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.narkolep.skkimmer.data.AppDatabase
import com.narkolep.skkimmer.data.SkkDictionaryManager
import com.narkolep.skkimmer.ui.components.ConfirmDialog
import com.narkolep.skkimmer.ui.components.Divider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryListScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dictList by db.dictionaryInfoDao().getAllFlow().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showResetDictDialog by remember { mutableStateOf(false) }

    // ファイル名取得のためのヘルパー（Uriからファイル名を取り出す）
    fun getFileName(uri: Uri): String {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "不明な辞書"
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            val fileName = getFileName(selectedUri)
            isLoading = true
            scope.launch {
                val manager = SkkDictionaryManager(context)
                val result = manager.importDictionaryFromUri(selectedUri, fileName)

                result.onSuccess {
                    Toast.makeText(context, "辞書を追加しました", Toast.LENGTH_SHORT).show()
                }.onFailure { exception ->
                    val errorMessage = exception.message ?: "不明なエラー"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("辞書一覧") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "メニュー")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("ファイルの追加...") },
                            onClick = {
                                filePickerLauncher.launch(arrayOf("*/*"))
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("辞書の初期化") },
                            onClick = {
                                showResetDictDialog = true
                                menuExpanded = false
                            }
                        )
                    }
                }
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (dictList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("まだ何も読み込まれていないようです。")
                }
            } else {
                LazyColumn {
                    item { Divider() }
                    items(dictList) { info ->
                        ListItem(
                            headlineContent = { Text(info.name) },
                            leadingContent = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                            supportingContent = { Text(text = "${info.entryCount} 件") }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    if (showResetDictDialog) {
        ConfirmDialog(
            title = "辞書を初期化しますか？",
            text = "手動で追加した辞書ファイルはすべて削除されます。この操作は元に戻せません。",
            onConfirm = {
                // コルーチンの中で削除処理を実行
                isLoading = true
                showResetDictDialog = false
                scope.launch {
                    val manager = SkkDictionaryManager(context)
                    // すべて削除
                    manager.clearAllDictionaries()
                    Toast.makeText(context, "辞書を削除しました", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            },
            onDismiss = { showResetDictDialog = false }
        )
    }
}