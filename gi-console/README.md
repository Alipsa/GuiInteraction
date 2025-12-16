# gi-console

Console implementation of the GuiInteraction library.

## Overview

This module provides console-based (text mode) interaction capabilities. It's designed for headless environments, CI/CD pipelines, and terminal-based applications.

## Requirements

- **JDK 21+** (any distribution)
- No GUI required (headless compatible)

## Features

- Text-based prompts for user input
- File path input via console
- Table output using Matrix text formatting
- HTML content displayed as plain text (via Jsoup)
- System clipboard access (when Desktop is available)

## Installation

### Gradle

```groovy
dependencies {
    implementation 'se.alipsa.gi:gi-console:0.2.0'
}
```

### Maven

```xml
<dependency>
    <groupId>se.alipsa.gi</groupId>
    <artifactId>gi-console</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Grape (Groovy Script)

```groovy
@Grab(group:'se.alipsa.gi', module:'gi-console', version:'0.2.0')
import se.alipsa.gi.txt.InOut

def io = new InOut()
```

## Quick Start

```groovy
import se.alipsa.gi.txt.InOut

def io = new InOut()

// User prompts (text input)
def name = io.prompt("Enter your name: ")

// File selection (user types path)
def file = io.chooseFile("Open", ".", "Enter file path")

// Display table (outputs formatted text)
io.view([
    ["Name", "Age"],
    ["Alice", 30],
    ["Bob", 25]
], "Users")

// Display HTML (outputs plain text)
io.view("<h1>Hello</h1><p>Welcome!</p>", "Greeting")
// Output: Greeting: Hello Welcome!
```

## Fat JAR

A fat JAR (with all dependencies bundled) is available:

```groovy
dependencies {
    implementation 'se.alipsa.gi:gi-console:0.2.0:fatjar'
}
```

## Limitations

- **Password Input**: Requires `System.console()` to be available. Returns `null` in IDEs and some CI environments where console is unavailable.
- **Charts/Images**: `display(Chart)` and `display(File)` for images print a message instead of showing graphics
- **Swing Components**: `display(JComponent)` is not supported
- **File Choosers**: User manually types file paths instead of browsing

## Console Availability

The password prompt requires a system console:

```groovy
def password = io.promptPassword("Login", "Enter password")
if (password == null) {
    println("Console not available - cannot read password")
}
```

To ensure console availability:
- Run from a terminal (not IDE "Run" button)
- Use `java -jar app.jar` from command line

## Dependencies

- [Jsoup](https://jsoup.org/) - HTML parsing and text extraction

## API Documentation

See the [API Guide](../docs/API-Guide.md) for detailed usage examples.

## License

MIT License - see [LICENSE](../LICENSE)
