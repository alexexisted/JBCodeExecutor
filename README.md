

- Basic script editing with syntax highlighting for Kotlin keywords.
- Executes Kotlin scripts using kotlinc -script.
- Displays real-time output while the script is running.
- Shows execution errors and exit codes.
- Provides an indicator for script execution status.

###Prerequisites

Java Development Kit (JDK) 11+
Kotlin Compiler (kotlinc)
install it via 
~~~bash
brew install kotlin
~~~
Gradle (if building the project manually)

###Steps
**Clone git repo:**
~~~bash
git clone git@github.com:alexexisted/JBCodeExecutor.git
~~~

**Navigate to the Project Directory:**
~~~bash
cd kotlin-script-editor
~~~

**or**

Open the project in Android Studio or a preferred Kotlin IDE.
Build and run the project.

**How to Start**

Write or paste Kotlin script into the editor.
Click the Run Script button.

Observe the output pane for results.

Running from Command Line


You can run the project using Gradle:
~~~bash
./gradlew run
~~~~

**Known Issues**
Syntax highlighting does not currently support custom user-defined keywords.

Feel free to open issues and submit pull requests to improve the editor!

**Video:**
https://github.com/user-attachments/assets/073372a2-a86d-464c-b6c6-31273f770602

