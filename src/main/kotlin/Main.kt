import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
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

// main screen - scrollable view window with bar on the left with number for each line
// fun for terminal - just like in any ide, terminal appears after clicking on run button and closes after
// clicking on exit button on this terminal

//add source, onSuccess, OnFailure

@Composable
@Preview
fun ScriptEditor() {

    var scriptText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("Output will be shown here...") }
    var isRunning by remember { mutableStateOf(false) }
    val lines = scriptText.lines().size

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Enter Kotlin Script:", fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .background(Color.LightGray)
                    .padding(end = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 0..lines.coerceAtLeast(0)) {
                    Text("$i", fontSize = 14.sp, fontWeight = FontWeight.Light)
                }
            }

            TextField(
                value = scriptText,
                onValueChange = { scriptText = it },
                modifier = Modifier.fillMaxSize().weight(1f),
                textStyle = TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
//            onClick = { executeScript(scriptText) { output -> outputText = output } },
            onClick = { outputText = scriptText },
            enabled = !isRunning
        ) {
            Text(if (isRunning) "Running..." else "Run Script")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Output Area
        Text("Output:", fontWeight = FontWeight.Bold)
        Text(
            outputText,
            modifier = Modifier.fillMaxWidth().border(1.dp, Color.Gray).padding(8.dp),
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        ScriptEditor()
    }
}
