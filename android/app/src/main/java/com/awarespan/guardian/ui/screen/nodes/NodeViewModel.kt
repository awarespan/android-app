package com.awarespan.guardian.ui.screen.nodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.domain.model.Node
import com.awarespan.guardian.domain.repository.NodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NodeUiState(
    val loading: Boolean = true,
    val nodes: List<Node> = emptyList(),
    val selectedNode: Node? = null,
    val error: String? = null,
)

@HiltViewModel
class NodeViewModel @Inject constructor(
    private val nodeRepository: NodeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NodeUiState())
    val uiState: StateFlow<NodeUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = nodeRepository.getNodes()
                _uiState.value = NodeUiState(loading = false, nodes = list)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Unable to fetch nodes"
                )
            }
        }
    }

    fun selectNode(nodeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val node = nodeRepository.getNodeById(nodeId)
            _uiState.value = _uiState.value.copy(selectedNode = node)
        }
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedNode = null)
    }
}
