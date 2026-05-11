package com.narkolep.skkimmer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.narkolep.skkimmer.data.HistoryViewModel
import com.narkolep.skkimmer.ui.components.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDictionaryScreen(
    viewModel: HistoryViewModel
) {
    // ViewModelのStateFlowをComposeのStateとして監視
    val histories by viewModel.histories.collectAsState()
    // 検索ボックスの入力状態を管理する変数
    var searchQuery by remember { mutableStateOf("") }
    // 絞り込み状態を管理する変数
    var showOnlyUserWords by remember { mutableStateOf(false) }
    // メニューの開閉状態
    var menuExpanded by remember { mutableStateOf(false) }
    // 削除ダイアログ
    var showResetUserDictDialog by remember { mutableStateOf(false) }

    // 表示用リストのフィルタリング
    val displayedHistories = if (showOnlyUserWords) {
        histories.filter { !it.isFromSystemDict }
    } else {
        histories
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ユーザー辞書データ管理") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "メニュー")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // 選択中かどうかわかるようにチェックマークを表示
                                    if (showOnlyUserWords) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        // チェックがない時もスペースだけ空けて文字の位置を揃える
                                        Spacer(modifier = Modifier.width(26.dp))
                                    }
                                    Text("ユーザー辞書のみ表示")
                                }
                            },
                            onClick = {
                                showOnlyUserWords = !showOnlyUserWords
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("すべて削除")
                            },
                            onClick = {
                                showResetUserDictDialog = true
                                menuExpanded = false
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 検索バー
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newText ->
                    searchQuery = newText
                    viewModel.onQueryChange(newText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("検索...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "検索") },
                trailingIcon = {
                    // 入力がある時だけクリアボタンを表示
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.onQueryChange("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "クリア")
                        }
                    }
                },
                singleLine = true
            )

            // 検索結果の件数表示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "登録数: ${displayedHistories.size}件" + if (showOnlyUserWords) "(ユーザー辞書のみを表示中)" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            // 履歴のリスト
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // items() に ViewModel から受け取ったリストを渡す
                items(displayedHistories) { entry ->
                    ListItem(
                        headlineContent = { Text(entry.candidate) },
                        supportingContent = {
                            Text(
                                text = entry.midashi + if (!entry.isFromSystemDict && !showOnlyUserWords) " [ユーザー辞書]" else "",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        },
                        trailingContent = {
                            // 削除ボタン
                            IconButton(onClick = { viewModel.delete(entry) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "削除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }

    if (showResetUserDictDialog) {
        ConfirmDialog(
            title = "履歴を初期化しますか？",
            text = "保存された履歴とユーザー辞書データをすべて削除します。この操作は元に戻せません。",
            onConfirm = {
                viewModel.deleteAll()
                showResetUserDictDialog = false
            },
            onDismiss = { showResetUserDictDialog = false }
        )
    }
}