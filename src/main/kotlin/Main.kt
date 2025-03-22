import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState() //scroll state for line's numbers and code-editor itself

    Column(
        modifier = Modifier
            .height(920.dp)
            .width(1080.dp)
            .padding(16.dp)
    ) {
        Text("executor", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.Gray)
                .padding(8.dp)
                .weight(1f)
        ) {
            //box with line's numbers
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .padding(top = 5.dp)
                    .verticalScroll(scrollState)
            ) {
                Column {
                    val lines = uiState.enteredText.lines().size //amount of lines
                    for (i in 1..lines.coerceAtLeast(1)) {
                        Text(
                            text = "$i", //set line count
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
            //box with editor pane
            Box(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .weight(0.9f)
                    .padding(5.dp)
            ) {

                Text( //display highlighted text
                    text = uiState.highlightedText,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                //get user's input but make it transparent
                BasicTextField(
                    value = uiState.enteredText,
                    onValueChange = { newText ->
                        viewModel.updateText(newText) //update entered text
                        //highlight words and update them in state, and then display in a box layer
                        viewModel.updateHighlightedText(viewModel.highlightKotlinSyntax(newText))
                    },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Transparent,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        //row with buttons and progress circle
        Row(verticalAlignment = Alignment.CenterVertically) {
            //run script button
            Button(
                onClick = {
                    viewModel.showProgress()
                    viewModel.executeScript()
                },
                enabled = !uiState.isRunning
            ) {
                Text(if (uiState.isRunning) "Running..." else "Run Script")
            }
            //progress circle
            if (uiState.isRunning) {
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }

            Spacer(modifier = Modifier.weight(1f))

            //close ternminal button
            if (uiState.showTerminal) {
                Button(
                    onClick = { viewModel.closeTerminal() },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Close Terminal", color = Color.White)
                }
            }
        }

        //terminal output part
        if (uiState.showTerminal) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black)
                    .padding(8.dp)
            ) {

                //using lazy to ensure that scroll state saves after recomposition
                val listState = rememberLazyListState()

                //selection container t oallow user copy output/error
                SelectionContainer {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier,
                    ) {
                        //displaying each line separately
                        items(uiState.outputText.lines()) { line ->
                            Text(
                                text = line,
                                color = Color.Green,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp,
                            )
                        }
                    }
                }

                //when output updates terminal scrolls to the last item
                LaunchedEffect(uiState.outputText) {
                    listState.scrollToItem(uiState.outputText.lines().size - 1)
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
