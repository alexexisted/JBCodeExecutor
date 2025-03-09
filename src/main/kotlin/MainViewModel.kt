import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import utils.execute
import java.io.File
import java.io.InputStreamReader

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState = _uiState.asStateFlow()

    fun executeScript() {
        viewModelScope.execute(
            source = {
                runKotlinScript(_uiState.value.enteredText)
            },
            onSuccess = { output ->
                _uiState.update {
                    it.copy(
                        outputText = output,
                        showTerminal = true,
                        isRunning = true
                    )
                }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        outputText = error.toString(),
                        showTerminal = true,
                        isRunning = false
                    )
                }
            },
            onComplete = {
                _uiState.update {
                    it.copy(
                        isRunning = false
                    )
                }
            }
        )
    }

    fun closeTerminal() {
        _uiState.update {
            it.copy(
                showTerminal = false
            )
        }
    }

    fun updateText(text: String) {
        _uiState.update {
            it.copy(
                enteredText = text
            )
        }
    }

    private fun getKotlinCompilerPath(): String {
        val pathsToCheck = listOf(
            "/opt/homebrew/bin/kotlinc",
            "/usr/local/bin/kotlinc",
            "/usr/bin/kotlinc",
            System.getenv("KOTLIN_HOME")?.let { "$it/bin/kotlinc" } ?: ""
        )

        return pathsToCheck.firstOrNull { File(it).exists() }
            ?: throw IllegalStateException("Kotlin compiler not found. Please install it.")
    }

    private fun runKotlinScript(script: String): String {
        val kotlincPath = getKotlinCompilerPath()

        val scriptFile = File.createTempFile("script", ".kts").apply {
            writeText(script)
        }

        val process = ProcessBuilder(kotlincPath, "-script", scriptFile.absolutePath)
            .redirectErrorStream(true)
            .start()

        return InputStreamReader(process.inputStream).readText()
    }
}

