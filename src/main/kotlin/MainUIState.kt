import androidx.compose.ui.text.AnnotatedString

data class MainUIState (
    var enteredText: String = "",
    val outputText: String = "",
    val isRunning: Boolean = false,
    val showTerminal: Boolean = false,
    val kotlinKeywords: Set<String> = setOf(
        "val", "var", "fun", "if", "else", "for", "while", "when",
        "class", "object", "return", "break", "continue",
        "try", "catch", "finally", "import", "print", "String", "Int",
        "List", "Thread", "System", "println"),
    val highlightedText: AnnotatedString = AnnotatedString("")
)