# GuiInteraction

A library providing GUI interaction capabilities for Groovy applications, compatible with [Gade](https://github.com/Alipsa/gade).

## Overview

GuiInteraction enables standalone Groovy applications to have the same user interaction capabilities as when running in Gade. Choose the implementation that fits your environment:

| Module                    | UI Technology | JDK Requirements                               | Best For                              |
|---------------------------|---------------|------------------------------------------------|---------------------------------------|
| [gi-fx](gi-fx/)           | JavaFX        | JDK with JavaFX (e.g., BellSoft Liberica Full) | Rich desktop apps                     |
| [gi-swing](gi-swing/)     | Swing         | Any JDK 21+                                    | Cross-platform desktop apps           |
| [gi-console](gi-console/) | Console/Text  | Any JDK 21+                                    | Terminal and Headless/CI environments |

## Features

- File and directory choosers
- User prompts (text, password, selections)
- Date and year-month pickers
- HTML and Markdown content viewing
- Table/Matrix display
- Clipboard operations
- Shell command execution with captured output and exit status
- Content type detection (via Apache Tika)
- Resource loading utilities

## Quick Start

### Gradle

```groovy
dependencies {
    implementation 'se.alipsa.gi:gi-swing:0.4.1'  // or gi-fx, gi-console
}
```

### Maven

```xml
<dependency>
    <groupId>se.alipsa.gi</groupId>
    <artifactId>gi-swing</artifactId>
    <version>0.4.1</version>
</dependency>
```

### Groovy Script with Grape

```groovy
@Grab('se.alipsa.gi:gi-swing:0.4.1')
import se.alipsa.gi.swing.InOut

def io = new InOut()
def file = io.chooseFile("Choose a file", ".", "Pick a file please!")
println("File chosen was $file")
```

## Gade Compatibility

Scripts can run both in Gade and standalone by checking for the `io` variable.
The dependency is loaded only when Gade has not already supplied `io`:

```groovy
import static groovy.grape.Grape.grab

// This makes the code run equally in Gade and in a standalone Groovy script
if (!binding.hasVariable('io')) {
    grab(group: 'se.alipsa.gi', module: 'gi-swing', version: '0.4.1')
    def inOutClass = this.class.classLoader.loadClass('se.alipsa.gi.swing.InOut')
    binding.setVariable('io', inOutClass.getDeclaredConstructor().newInstance())
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

Install a JDK 21 toolchain before building. Gradle uses that toolchain even if
the Gradle daemon runs on a newer JDK.

```bash
./gradlew build
```

`build` runs tests, Spotless, and SpotBugs, including when Gradle's configuration
cache is enabled. Before pushing, you can run the formatting-and-check wrapper:

```bash
./check.sh
```

`check.sh` tests the release helpers, then runs `spotlessApply` and `check`.

`urlExists` reports unsupported URL schemes and broken redirect chains as
warnings. Connection and HTTP failures are logged at debug level. To see those
details, set the matrix logger to DEBUG and enable `FINE` for the underlying
Java Util Logging configuration.

Table views use `Table` as the window or tab title when neither a nonblank
explicit title nor a matrix name is available.

CI builds all four modules on Linux and Windows. The full dependency CVE scan
runs on the weekly schedule or by manual dispatch because its NVD database
refresh needs an API key and can be rate limited; it is not a PR gate. Run
`./checkCVE.sh` with `NVD_API_KEY` set for a fresh scan before release.

## Requirements

- Java 21 or later to use the published libraries; JDK 21 to build from source
- For gi-fx: JDK with JavaFX support (e.g., BellSoft Liberica Full JDK)

## License

MIT License - see [LICENSE](LICENSE)
