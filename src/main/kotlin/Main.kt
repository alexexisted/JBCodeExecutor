import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    fun highlightKotlinSyntax(text: String): AnnotatedString {
        val regex = "\\b(${uiState.kotlinKeywords.joinToString("|")})\\b".toRegex()
        return buildAnnotatedString {
            var lastIndex = 0
            regex.findAll(text).forEach { matchResult ->
                val start = matchResult.range.first
                val end = matchResult.range.last + 1

                append(text.substring(lastIndex, start))

                withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                    append(text.substring(start, end))
                }

                lastIndex = end
            }
            append(text.substring(lastIndex))
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("executor", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.Gray)
                .padding(8.dp)
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(40.dp)
                    .background(Color.LightGray)
                    .verticalScroll(scrollState)
            ) {
                Column {
                    val lines = uiState.enteredText.lines().size
                    for (i in 1..lines.coerceAtLeast(1)) {
                        Text(
                            text = "$i",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }

            Box(Modifier.fillMaxSize()) {
                Text(
                    text = uiState.highlightedText,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.matchParentSize()
                )

                BasicTextField(
                    value = uiState.enteredText,
                    onValueChange = { newText ->
                        viewModel.updateText(newText)
                        viewModel.updateHighlightedText(highlightKotlinSyntax(newText))
                    },
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.Transparent),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    viewModel.showProgress()
                    viewModel.executeScript()
                },
                enabled = !uiState.isRunning
            ) {
                Text(if (uiState.isRunning) "Running..." else "Run Script")
            }

            if (uiState.isRunning) {
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }

        if (uiState.showTerminal) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Black)
                    .padding(8.dp)
            ) {
                val listState = rememberLazyListState()

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.outputText.lines()) { line ->
                        Text(
                            text = line,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                LaunchedEffect(uiState.outputText) {
                    listState.scrollToItem(uiState.outputText.lines().size - 1)
                }

                TextButton(
                    onClick = { viewModel.closeTerminal() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close Terminal")
                }
            }
        }


    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MainScreen(MainViewModel())
    }
}
