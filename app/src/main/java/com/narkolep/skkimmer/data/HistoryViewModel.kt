package com.narkolep.skkimmer.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val dao: HistoryDao
) : ViewModel() {

    private val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val histories: StateFlow<List<HistoryEntry>> =
        query
            .flatMapLatest { q ->
                if (q.isBlank()) {
                    dao.observeAll()
                } else {
                    dao.searchFlow(q)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.Companion.WhileSubscribed(5000),
                emptyList()
            )

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun delete(entry: HistoryEntry) {
        viewModelScope.launch {
            dao.deleteEntry(entry)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }
}