// main screen - scrollable view window with bar on the left with number for each line
// fun for terminal - just like in any ide, terminal appears after clicking on run button and closes after
// clicking on exit button on this terminal
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Kotlin Script Editor", fontSize = 20.sp, fontWeight = FontWeight.Bold)

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

            BasicTextField(
                value = uiState.enteredText,
                onValueChange = { newText -> viewModel.updateText(newText) },
                textStyle = TextStyle(fontSize = 14.sp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.executeScript() }, enabled = true) {
            Text(if (uiState.isRunning) "Running..." else "Run Script")
        }

        if (uiState.showTerminal) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Black)
                    .padding(8.dp)

            ) {
                Text(
                    text = if (uiState.isRunning) "Running..." else uiState.outputText,
                    color = Color.White
                )

                Button(onClick = { viewModel.closeTerminal() }, modifier = Modifier.align(Alignment.End)) {
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
