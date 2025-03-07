import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState = _uiState.asStateFlow()

    fun executeScript() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isRunning = true,
                showTerminal = true,
                outputText = "Running script...\n"
            ) }

            try {
//                outputText = runKotlinScript(scriptText)
            } catch (e: Exception) {
//                outputText = "Error: ${e.message}"
            } finally {
//                isRunning = false
            }
        }
    }

}