# Compose HTML-based Connect Four

### Features
- **Configurable board size**
- **Configurable win condition**
- **Two-player local play**
- **Win & draw detection** - the game automatically detects a winner or a full-board draw
- **Responsive layout** — adapts to both desktop and mobile browsers
- **Falling piece animation!**
- **Game state persistence** — game state is saved to the browser's local storage so the game can be resumed

<br>

## Getting Started

### Prerequisites

- JDK 11 to 24

### Running the game

```bash
./gradlew jsBrowserDevelopmentRun
```

Opens the game at `http://localhost:8080` with live reload.

### Or alternatively

Run the project from IntelliJ UI.

<br>

## Running Tests

```bash
./gradlew jsTest
```

>**Note:** Unit tests are not yet implemented.

Or, from IntelliJ UI.

<br>

## Based On

Project scaffolded from the [Kotlin JS Wizard](https://github.com/Kotlin/kmp-js-wizard/) template.