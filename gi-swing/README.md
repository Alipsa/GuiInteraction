# gi-swing

Swing implementation of the GuiInteraction library.

## Overview

This module provides Swing-based dialogs and viewers for user interaction. It works with any standard JDK without requiring JavaFX.

## Requirements

- **JDK 21+** (any distribution)

## Features

- Standard Swing file and directory choosers
- Date and year-month pickers (via LGoodDatePicker)
- HTML viewing via JEditorPane
- Table display with JTable
- Clipboard operations
- Chart display via Matrix Charts integration

## Installation

### Gradle

```groovy
dependencies {
    implementation 'se.alipsa.gi:gi-swing:0.2.0'
}
```

### Maven

```xml
<dependency>
    <groupId>se.alipsa.gi</groupId>
    <artifactId>gi-swing</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Grape (Groovy Script)

```groovy
@Grab(group:'se.alipsa.gi', module:'gi-swing', version:'0.2.0')
import se.alipsa.gi.swing.InOut

def io = new InOut()
```

## Quick Start

```groovy
import se.alipsa.gi.swing.InOut

def io = new InOut()

// File selection
def file = io.chooseFile("Open", ".", "Select a file", "txt", "csv")

// User prompts
def name = io.prompt("Enter your name:")

// Display HTML
io.view("<h1>Hello</h1><p>Welcome!</p>", "Greeting")

// Display table data
io.view([
    ["Name", "Age"],
    ["Alice", 30],
    ["Bob", 25]
], "Users")
```

## Fat JAR

A fat JAR (with all dependencies bundled) is available:

```groovy
dependencies {
    implementation 'se.alipsa.gi:gi-swing:0.2.0:fatjar'
}
```

## Limitations

- **SVG Support**: SVG files are not currently rendered (displays a message instead)
- **Look and Feel**: Uses system look and feel, which varies by platform

## Dependencies

- [LGoodDatePicker](https://github.com/LGoodDatePicker/LGoodDatePicker) - Date picker component
- [swing-yearmonth-picker](https://github.com/Alipsa/swing-yearmonth-picker) - Year-month picker

## API Documentation

See the [API Guide](../docs/API-Guide.md) for detailed usage examples.

## License

MIT License - see [LICENSE](../LICENSE)
