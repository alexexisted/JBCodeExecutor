import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import utils.execute
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState = _uiState.asStateFlow()

    fun showProgress() {
        _uiState.update {
            it.copy(
                isRunning = true
            )
        }
    }

    fun executeScript() {
        _uiState.update {
            it.copy(
                outputText = "",
                showTerminal = true,
                isRunning = true
            )
        }

        viewModelScope.execute(
            source = {
                runKotlinScript(
                    script = _uiState.value.enteredText,
                    onOutput = { line ->
                        _uiState.update {
                            it.copy(outputText = it.outputText + line + "\n")
                        }
                    },
                    onExitCode = { exitCode ->
                        _uiState.update {
                            it.copy(
                                isRunning = false,
                                outputText = it.outputText + "\nExit Code: $exitCode",
                            )
                        }
                    }
                )
            },
            onSuccess = {
                _uiState.update {
                    it.copy(
                        isRunning = false
                    )
                }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        outputText = error.toString(),
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

    fun updateHighlightedText(text: AnnotatedString) {
        _uiState.update {
            it.copy(
                highlightedText = text
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

    private suspend
    fun runKotlinScript(script: String, onOutput: (String) -> Unit, onExitCode: (Int) -> Unit) {
        withContext(Dispatchers.IO) {
            val kotlincPath = getKotlinCompilerPath()

            val scriptFile = File.createTempFile("script", ".kts").apply {
                writeText(script)
            }

            val process = ProcessBuilder(kotlincPath, "-script", scriptFile.absolutePath)
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))

            try {
                while (true) {
                    val line = reader.readLine() ?: break
                    withContext(Dispatchers.Main) {
                        onOutput(line)
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    onOutput("Error: ${e.message}")
                }
            } finally {
                process.waitFor()
                val exitCode = process.exitValue()
                withContext(Dispatchers.Main) { onExitCode(exitCode) }
                reader.close()
            }
        }
    }

}

