# GuiInteraction

A library providing GUI interaction capabilities for Groovy applications, compatible with [Gade](https://github.com/Alipsa/gade).

## Overview

GuiInteraction enables standalone Groovy applications to have the same user interaction capabilities as when running in Gade. Choose the implementation that fits your environment:

| Module | UI Technology | JDK Requirements | Best For |
|--------|--------------|------------------|----------|
| [gi-fx](gi-fx/) | JavaFX | JDK with JavaFX (e.g., BellSoft Liberica Full) | Rich desktop apps |
| [gi-swing](gi-swing/) | Swing | Any JDK 21+ | Cross-platform desktop apps |
| [gi-console](gi-console/) | Console/Text | Any JDK 21+ | Headless/CI environments |

## Features

- File and directory choosers
- User prompts (text, password, selections)
- Date and year-month pickers
- HTML and Markdown content viewing
- Table/Matrix display
- Clipboard operations
- Content type detection (via Apache Tika)
- Resource loading utilities

## Quick Start

### Gradle

```groovy
dependencies {
    implementation 'se.alipsa.gi:gi-swing:0.2.0'  // or gi-fx, gi-console
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

### Groovy Script with Grape

```groovy
@Grab(group:'se.alipsa.gi', module:'gi-swing', version:'0.2.0')
import se.alipsa.gi.swing.InOut

def io = new InOut()
def file = io.chooseFile("Choose a file", ".", "Pick a file please!")
println("File chosen was $file")
```

## Gade Compatibility

Scripts can run both in Gade and standalone by checking for the `io` variable:

```groovy
// This makes the code run equally in Gade and in a standalone Groovy script
if (!binding.hasVariable('io')) {
    @Grab(group:'se.alipsa.gi', module:'gi-swing', version:'0.2.0')
    import se.alipsa.gi.swing.InOut
    binding.setVariable('io', new InOut())
}

def file = io.chooseFile("Choose a file", ".", "Pick a file please!")
println("File chosen was $file")
```

## Documentation

- [API Guide](docs/API-Guide.md) - Detailed usage examples
- [gi-common](gi-common/) - Core interfaces and utilities
- [gi-fx](gi-fx/) - JavaFX implementation
- [gi-swing](gi-swing/) - Swing implementation
- [gi-console](gi-console/) - Console implementation

## Building from Source

```bash
./gradlew build
```

## Requirements

- JDK 21 or later
- For gi-fx: JDK with JavaFX support (e.g., BellSoft Liberica Full JDK)

## License

MIT License - see [LICENSE](LICENSE)

