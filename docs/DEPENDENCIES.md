# Dependencies Guide

This document describes the dependency structure of GuiInteraction and strategies for managing dependency footprint.

## Dependency Overview

### gi-common (Core Module)

The core module includes these key dependencies:

| Dependency | Purpose | Impact |
|------------|---------|--------|
| Apache Tika | Content-type detection | ~1MB (see below) |
| Matrix (BOM) | Table/chart support | ~2MB |
| CommonMark | Markdown rendering | ~200KB |
| SLF4J API | Logging facade | ~40KB |

### gi-fx, gi-swing, gi-console

UI modules add minimal additional dependencies:
- **gi-fx**: JavaFX (provided by JVM), YearMonthPicker
- **gi-swing**: Batik (SVG), LGoodDatePicker, Swing YearMonthPicker
- **gi-console**: JSoup (HTML parsing)

## Apache Tika Dependency Impact

### Why Tika?

Tika provides robust MIME type detection via `getContentType()`, which is essential for:
- Determining how to display files (image vs SVG vs document)
- Validating file uploads
- Content-aware processing

### Optimized Configuration

GuiInteraction uses **only `tika-core`** (~1MB) instead of the full `tika-parsers-standard-package`
(~50MB with 100+ transitive dependencies). This is sufficient because:

- The library only uses `Tika.detect(file)` for MIME type detection
- `tika-core` provides detection via file extension mapping and magic bytes
- Full parsers (POI, PDFBox, etc.) are not needed for content-type detection

**Result:** Fat JARs are **11-15MB** instead of ~60MB.

### Adding Full Tika Parsers (If Needed)

If your application needs full document parsing capabilities (e.g., extracting text from
PDFs or Office documents), add the full parser package:

```groovy
dependencies {
    implementation 'se.alipsa.gi:gi-common:0.2.0'
    // Add full Tika parsers for document content extraction
    implementation 'org.apache.tika:tika-parsers-standard-package:3.2.3'
}
```

This adds support for:
- Office documents (via Apache POI)
- PDFs (via Apache PDFBox)
- Audio/video metadata
- Archive formats
- And 100+ other formats

### Fat JAR for Standalone Scripts

The fat JARs (`gi-swing-fatjar`, `gi-fx-fatjar`, etc.) include all dependencies
and are designed for standalone Groovy scripts:

```groovy
@Grab(group:'se.alipsa.gi', module:'gi-swing', version:'0.2.0', classifier:'fatjar')
import se.alipsa.gi.swing.InOut
```

### Dependency Tree

To see the full dependency tree:

```bash
./gradlew :gi-common:dependencies --configuration runtimeClasspath
```

## Security Scanning

### OWASP Dependency Check

The project includes the OWASP Dependency Check plugin to scan for known vulnerabilities (CVEs):

```bash
# Run security scan
./gradlew dependencyCheckAnalyze

# View report
open build/reports/dependency-check-report.html
```

Configuration in `build.gradle`:
- Fails build on CVSS score >= 7.0 (HIGH/CRITICAL)
- Generates HTML and JSON reports
- Supports suppression file for false positives

### Suppressing False Positives

Create `config/owasp-suppressions.xml` to suppress known false positives:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
    <suppress>
        <notes>Example: False positive for internal package</notes>
        <packageUrl regex="true">^pkg:maven/com\.example/.*$</packageUrl>
        <cve>CVE-XXXX-XXXXX</cve>
    </suppress>
</suppressions>
```

### Keeping Dependencies Updated

Check for available updates:

```bash
./gradlew dependencyUpdates
```

## Version Compatibility

| GuiInteraction | Java | Groovy | JavaFX |
|----------------|------|--------|--------|
| 0.2.x | 21+ | 5.0.x | 21-23 |
| 0.1.x | 17+ | 4.0.x | 17-21 |

## CI/CD Integration

The GitHub Actions workflow includes a separate `dependency-check` job that runs on a schedule or manual workflow dispatch:

```yaml
dependency-check:
  runs-on: ubuntu-latest
  if: github.event_name == 'schedule' || github.event_name == 'workflow_dispatch'

  steps:
    - name: Check for dependency updates
      run: ./gradlew dependencyUpdates
```

This approach keeps regular CI builds fast while still providing periodic dependency monitoring.
