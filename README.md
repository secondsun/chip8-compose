# Chip-8 Compose

This is an experimental IDE for Chip-8, SuperChip, and XO-Chip. It is heavily inspired by the [Octo](https://github.com/JohnEarnest/Octo)
Chip8 assembler project. The goal of this project is to replicate most of the functionality in Octo in Compose and support
Desktop (Mac, Linux and Windows), Android, and Web targets. 

My previous work on Chip8 can be found [here](https://github.com/secondsun/chip8).

# Project Goals

* Implement Chip8, SuperChip, and Xo-Chip
* Include a built-in Editor, Assembler, and Debugger
  * Use [Monaco](https://microsoft.github.io/monaco-editor/) for editing
  * Other tools to be homebrewed at this time
  * Implement the [Octo assembly language](https://github.com/JohnEarnest/Octo/blob/gh-pages/docs/Manual.md)
* Include good UI and configuration tools
* Support theming including accent colors and light/dark modes 

# Running the Project

For now, `./gradlew :composeApp:run`

To run with hot-reload, `./gradlew clean :compose:desktopRunHot -PmainClass="dev.secondsun.chip8.compose.MainKt" --auto` 

# Starter README from KMP wizard 

This is a Kotlin Multiplatform project targeting Android, Web, Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.