# gi-fx

JavaFX implementation of the GuiInteraction library.

## Overview

This module provides JavaFX-based dialogs and viewers for user interaction. It offers the richest UI experience with native-looking dialogs, date pickers, and web-based content viewing.

## Requirements

- **JDK 21+** with JavaFX support
- Recommended: [BellSoft Liberica Full JDK](https://bell-sw.com/pages/downloads/) which includes JavaFX

## Features

- Native JavaFX dialogs for file/directory selection
- Rich date and year-month pickers with calendar UI
- WebView-based HTML and SVG rendering
- Markdown viewing with full formatting support
- Chart display via Matrix Charts integration

## Installation

### Gradle

```groovy
dependencies {
    implementation 'se.alipsa.gi:gi-fx:0.2.0'
}
```

### Maven

```xml
<dependency>
    <groupId>se.alipsa.gi</groupId>
    <artifactId>gi-fx</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Grape (Groovy Script)

```groovy
@Grab(group:'se.alipsa.gi', module:'gi-fx', version:'0.2.0')
import se.alipsa.gi.fx.InOut

def io = new InOut()
```

## Quick Start

```groovy
import se.alipsa.gi.fx.InOut

def io = new InOut()

// File selection
def file = io.chooseFile("Open", ".", "Select a file", "txt", "csv")

// User prompts
def name = io.prompt("Enter your name:")

// Display HTML
io.view("<h1>Hello</h1><p>Welcome!</p>", "Greeting")

// Display Markdown
io.viewMarkdown("# Title\n\nSome **bold** text", "Document")
```

## Fat JAR

A fat JAR (with all dependencies bundled) is available:

```groovy
dependencies {
    implementation 'se.alipsa.gi:gi-fx:0.2.0:fatjar'
}
```

## Notes

- JavaFX must be available at runtime (either bundled in JDK or added as dependencies)
- Thread safety: UI operations are automatically dispatched to the FX Application Thread
- SVG files are rendered using WebView

## API Documentation

See the [API Guide](../docs/API-Guide.md) for detailed usage examples.

## License

MIT License - see [LICENSE](../LICENSE)
