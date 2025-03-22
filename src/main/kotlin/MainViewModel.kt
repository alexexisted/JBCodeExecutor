import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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

//main place for the logic
class MainViewModel : ViewModel() {
    //state flow variables(mutable and immutable)
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState = _uiState.asStateFlow()

    //change state to show progress circle
    fun showProgress() {
        _uiState.update {
            it.copy(
                isRunning = true
            )
        }
    }

    //fun to start execution of the script
    fun executeScript() {
        _uiState.update { //update state
            it.copy(
                outputText = "",
                showTerminal = true,
                isRunning = true
            )
        }

        viewModelScope.execute(
            source = {
                //initially start method to get output from the script
                runKotlinScript(
                    script = _uiState.value.enteredText,
                    onOutput = { line ->
                        _uiState.update {
                            it.copy(outputText = it.outputText + line + "\n") //update uotput with executed part
                        }
                    },
                    onExitCode = { exitCode ->
                        _uiState.update {
                            it.copy(
                                isRunning = false,
                                outputText = it.outputText + "\nExit Code: $exitCode", //add exit code to result of the script
                            )
                        }
                    }
                )
            },
            onSuccess = {
                _uiState.update {
                    it.copy(
                        isRunning = false //when code successfully finishes
                    )
                }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        outputText = error.toString(), //get the error when execution threw err
                        isRunning = false
                    )
                }
            },
            onComplete = {
                _uiState.update {
                    it.copy(
                        isRunning = false //anyway set to false
                    )
                }
            }
        )
    }

    //update state to close terminal
    fun closeTerminal() {
        _uiState.update {
            it.copy(
                showTerminal = false
            )
        }
    }

    //update state when user entered code
    fun updateText(text: String) {
        _uiState.update {
            it.copy(
                enteredText = text
            )
        }
    }

    //highlight words
    fun highlightKotlinSyntax(text: String): AnnotatedString {
        val regex = "\\b(${_uiState.value.kotlinKeywords.joinToString("|")})\\b".toRegex()
        return buildAnnotatedString {
            var lastIndex = 0 //track end of last match
            regex.findAll(text).forEach { matchResult -> //find all keywords in text on a pane
                //first and last indexes of matched word
                val start = matchResult.range.first
                val end = matchResult.range.last + 1

                append(text.substring(lastIndex, start)) //firstly append not matched text

                withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                    append(text.substring(start, end)) //style actual matched word
                }

                lastIndex = end //set last index to the end of last matched word
            }
            append(text.substring(lastIndex))
        }
    }

    //highlighted text in state
    fun updateHighlightedText(text: AnnotatedString) {
        _uiState.update {
            it.copy(
                highlightedText = text
            )
        }
    }

    //get the path of kotlinc
    private fun getKotlinCompilerPath(): String {
        //check the most possible paths
        val pathsToCheck = listOf(
            "/opt/homebrew/bin/kotlinc",
            "/usr/local/bin/kotlinc",
            "/usr/bin/kotlinc",
            System.getenv("KOTLIN_HOME")?.let { "$it/bin/kotlinc" } ?: ""
            //getting env path where kotlin is installed and if not null add it to compiler path
        )

        return pathsToCheck.firstOrNull { File(it).exists() } //if kotlinc in path true
            ?: throw IllegalStateException("Kotlin compiler not found. Please install it.")
    }

    //execution method
    private suspend fun runKotlinScript(script: String, onOutput: (String) -> Unit, onExitCode: (Int) -> Unit) {
        withContext(Dispatchers.IO) { //run coroutine in coroutine
            val kotlincPath = getKotlinCompilerPath() //getting the path

            //create script file to write there entered script
            val scriptFile = File.createTempFile("script", ".kts").apply {
                writeText(script)
            }

            //start process
            val process = ProcessBuilder(kotlincPath, "-script", scriptFile.absolutePath)
                .redirectErrorStream(true) //merge stderr and stdout to get errors from the same stdout
                .start()

            //read chunks of data from buffer
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            try {
                while (true) {
                    val line = reader.readLine() ?: break //read a line
                    withContext(Dispatchers.Main) { //coroutine to update ui with line of text
                        onOutput(line)
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    onOutput("Error: ${e.message}") //throws exception to ui
                }
            } finally {
                process.waitFor()
                val exitCode = process.exitValue() //catching exit code
                withContext(Dispatchers.Main) { onExitCode(exitCode) } //update ui with exit code
                reader.close()
            }
        }
    }

}

