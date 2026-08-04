# GuiInteraction API Guide

This guide provides usage examples for the GuiInteraction library.

## Getting Started

### Choosing an Implementation

| Module | Description | JDK Requirements |
|--------|-------------|------------------|
| `gi-fx` | JavaFX-based dialogs | JDK with JavaFX (e.g., BellSoft Liberica Full) |
| `gi-swing` | Swing-based dialogs | Any JDK 21+ |
| `gi-console` | Console-based prompts | Any JDK 21+ (headless compatible) |

### Basic Setup

```groovy
// Using Grape for dependency resolution
@Grab(group:'se.alipsa.gi', module:'gi-swing', version:'0.4.0')
import se.alipsa.gi.swing.InOut

def io = new InOut()
```

Or in your `build.gradle`:

```groovy
dependencies {
    implementation 'se.alipsa.gi:gi-swing:0.4.0'
}
```

## Implementation Notes

- `gi-fx` dialog and clipboard APIs are safe to call from either the FX Application Thread or a background thread; calls are executed on the FX thread internally.
- `gi-fx` file and directory choosers ignore invalid initial directories and fall back to the platform default.

## File Operations

### Choosing Files

```groovy
// Basic file chooser
def file = io.chooseFile("Select File", ".", "Choose a data file", "csv", "txt")
if (file != null) {
    println("Selected: ${file.absolutePath}")
}

// With specific directory
def dataFile = io.chooseFile(
    "Open Data File",
    new File("/home/user/data"),
    "Select a CSV file",
    "csv"
)
```

### Choosing Directories

```groovy
def outputDir = io.chooseDir("Select Output Directory", ".")
if (outputDir != null) {
    println("Will save to: ${outputDir.absolutePath}")
}
```

### Working with Project Files

```groovy
// Get a file relative to the project root (user.dir)
def configFile = io.projectFile("config/settings.xml")
```

## User Prompts

### Simple Text Input

```groovy
def name = io.prompt("Enter your name:")
println("Hello, $name!")
```

### Prompts with Titles and Defaults

```groovy
def value = io.prompt(
    "Configuration",           // title
    "Database Settings",       // header
    "Enter connection string:", // message
    "localhost:5432"           // default value
)
```

### Named Parameters (Groovy Style)

```groovy
def appId = io.prompt(
    title: "Application Setup",
    headerText: "Enter application details",
    message: "Application ID:",
    defaultValue: "app-001"
)
```

### Password Input

```groovy
def password = io.promptPassword("Login", "Enter your password:")
if (password != null) {
    // Use password
}
```

### Selection from Options

```groovy
def options = ["Option A", "Option B", "Option C"]
def selected = io.promptSelect("Choose an option:", options)
println("You selected: $selected")

// With full parameters
def choice = io.promptSelect(
    "Settings",
    "Theme Selection",
    "Choose your preferred theme:",
    ["Light", "Dark", "System"],
    "System"  // default
)
```

### Date Selection

```groovy
import java.time.LocalDate
import java.time.YearMonth

// Date picker
def date = io.promptDate("Schedule", "Select a date:", LocalDate.now())

// Year-Month picker
def period = io.promptYearMonth("Select month:")

// Year-Month with range
def reportMonth = io.promptYearMonth(
    "Report Period",
    "Select reporting month:",
    YearMonth.of(2024, 1),   // from
    YearMonth.of(2025, 12),  // to
    YearMonth.now()          // initial
)
```

## Displaying Content

### HTML Content

```groovy
io.view("<h1>Report</h1><p>Data processed successfully.</p>", "Status")
```

### Markdown Content

```groovy
io.viewMarkdown("""
# Monthly Report

| Month | Sales |
|-------|-------|
| Jan   | 1000  |
| Feb   | 1200  |

**Total:** 2200
""", "Sales Report")
```

### Tables (Matrix)

```groovy
import se.alipsa.matrix.core.Matrix

// From a Matrix object
def matrix = Matrix.builder()
    .columnNames("Name", "Age", "City")
    .rows([
        ["Alice", 30, "Stockholm"],
        ["Bob", 25, "Gothenburg"]
    ])
    .build()

io.view(matrix, "User Data")

// From raw list-of-lists
io.view([
    ["Product", "Price"],
    ["Widget", 9.99],
    ["Gadget", 19.99]
], "Products")
```

### Files and Images

```groovy
// Display an image
io.display("/path/to/chart.png", "Chart")

// gi-console opens unknown files with the system default application;
// gi-swing and gi-fx expect a displayable image resource instead.
io.display(new File("document.pdf"))
```

## Clipboard Operations

```groovy
// Copy text to clipboard
io.saveToClipboard("Hello, World!")

// Copy file to clipboard
io.saveToClipboard(new File("/path/to/file.txt"))

// Get text from clipboard
def clipboardText = io.getFromClipboard()
println("Clipboard contains: $clipboardText")

// Get file from clipboard
def clipboardFile = io.getFileFromClipboard()
if (clipboardFile != null) {
    println("File on clipboard: ${clipboardFile.absolutePath}")
}
```

## Utility Methods

### Shell Commands

`sh` is the concise form for interactive or ad-hoc shell commands. It returns
standard output as a `String` and streams standard output and standard error
while the command runs unless `quiet` is `true`:

```groovy
def listing = io.sh('ls -a')
def filtered = io.sh('ls re* | grep matrix', true)
```

`sh` intentionally does not expose the exit status; use `shell` when the
success or failure of the command matters.

Use `shell` when a script needs the exit status and both output streams:

```groovy
def result = io.shell('ls re* | grep matrix > out.txt')

if (!result.success) {
    println("Command failed with exit code ${result.exitCode}: ${result.stderr}")
} else {
    println(new File('out.txt').text)
}
```

`ShellResult` provides `stdout`, `stderr`, `exitCode`, and `success`. Shell
redirection is performed by the operating system shell, so redirected output
is written to the target file and is not present in `result.stdout`.

Commands are executed through `/bin/sh` on Unix-like systems and `cmd.exe` on
Windows. Shell syntax is therefore platform-dependent. These methods execute
arbitrary commands; never concatenate untrusted user input into a shell
command.

### URL Existence Check

```groovy
if (io.urlExists("https://example.com/api/health", 5000)) {
    println("API is reachable")
} else {
    println("API is not responding")
}
```

`urlExists` accepts HTTP and HTTPS URLs, follows redirects within the supplied overall timeout budget, and returns `false` unless the final response is 2xx. A negative timeout throws `IllegalArgumentException`; `0` disables the timeout entirely, so the call may block indefinitely.

### Content Type Detection

```groovy
def contentType = io.getContentType("/path/to/file")
println("File type: $contentType")  // e.g., "image/png", "text/plain"
```

### Resource Loading

```groovy
import se.alipsa.gi.FileUtils

// Load from classpath or file system
def url = FileUtils.getResourceUrl("templates/report.html")
if (url != null) {
    def content = url.text
}

// Missing paths return null; use File directly for a file that will be created later.
def output = new File("build/report.html")

// Extract filename from path
def filename = FileUtils.baseName("/path/to/data.csv")  // "data.csv"
// A # in a local filename is preserved; URL fragments are stripped.
assert FileUtils.baseName("/tmp/report#2.pdf") == "report#2.pdf"
```

## Gade Compatibility

The library is designed to be compatible with [Gade](https://github.com/Alipsa/gade). Scripts can detect the environment:

```groovy
// Check if running in Gade or standalone
if (!binding.hasVariable('io')) {
    @Grab(group:'se.alipsa.gi', module:'gi-swing', version:'0.4.0')
    import se.alipsa.gi.swing.InOut
    binding.setVariable('io', new InOut())
}

// Now 'io' works the same in both Gade and standalone
def file = io.chooseFile("Select", ".", "Choose file")
```

## Error Handling

```groovy
try {
    def contentType = io.getContentType("/nonexistent/file.txt")
} catch (FileNotFoundException e) {
    println("File not found: ${e.message}")
}

// Handling user cancellation
def file = io.chooseFile("Select File", ".", "Choose")
if (file == null) {
    println("User cancelled file selection")
    return
}
```

## Module-Specific Notes

### gi-fx (JavaFX)

- Requires a JVM with JavaFX support
- SVG rendering via matrix-charts JavaFX integration (a lighter subset than a browser renderer)
- Rich date pickers with calendar UI

### gi-swing

- Works with any JDK
- SVG rendering via matrix-charts integration
- Standard Swing look and feel

### gi-console

- Best for headless/CI environments
- `display()` and `display(Chart)` print messages instead of showing UI
- Password input is masked when `System.console()` is available; otherwise stdin input is visible and a warning is logged
- Tables displayed as text using Matrix.content()
