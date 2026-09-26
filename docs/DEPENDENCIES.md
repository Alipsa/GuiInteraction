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
    implementation 'se.alipsa.gi:gi-common:0.4.0'
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
@Grab(group:'se.alipsa.gi', module:'gi-swing', version:'0.4.0', classifier:'fatjar')
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
# Set an NVD API key for the current shell (request one at
# https://nvd.nist.gov/developers/request-an-api-key)
export NVD_API_KEY='your-nvd-api-key'

# Run the security scan across the root project and all four modules
./checkCVE.sh
```

Use `dependencyCheckAggregate`, not `dependencyCheckAnalyze`: the root project
declares no dependencies, so the single-project task scans nothing.
The script checks for `NVD_API_KEY` and forces a fresh scan; for a direct invocation,
run `./gradlew dependencyCheckAggregate --no-configuration-cache --console=plain`.

Reports are written below `build/reports/dependency-check/`. The build fails when a
dependency has a CVSS score of 7 or higher.

Keep `NVD_API_KEY` in your shell/CI secret store; do not commit it to the repository.

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
| 0.4.x | 21 | 5.1.x | 23 |
| 0.3.x | 21+ | 5.0.x | 21-23 |
| 0.2.x | 21+ | 5.0.x | 21-23 |
| 0.1.x | 17+ | 4.0.x | 17-21 |

Every module compiles with a Java 21 toolchain configured in the root build,
so published artifacts load on Java 21 and later runtimes.

## CI/CD Integration

The GitHub Actions workflow includes a separate `dependency-check` job that runs on a schedule or manual workflow dispatch:

```yaml
dependency-check:
  runs-on: ubuntu-latest
  if: github.event_name == 'schedule' || github.event_name == 'workflow_dispatch'

  steps:
    - name: Scan dependencies for known vulnerabilities
      env:
        NVD_API_KEY: ${{ secrets.NVD_API_KEY }}
      run: ./gradlew dependencyCheckAggregate --no-configuration-cache

    - name: Check for dependency updates
      if: always()
      run: ./gradlew dependencyUpdates
```

This keeps regular CI fast while scanning for CVEs and dependency updates on a
schedule. The scan needs the `NVD_API_KEY` repository secret to avoid NVD rate limits.

## Module dependency scopes

`gi-common` is an `api` dependency of `gi-swing`, `gi-fx`, and `gi-console`. Each
implementation's `InOut` extends `se.alipsa.gi.AbstractInOut` and returns
`se.alipsa.matrix.core.Matrix` and `se.alipsa.groovy.svg.Svg`, so those types are
part of the published API and must be on a consumer's compile classpath.
Declaring `gi-common` as `implementation` would publish it at `runtime` scope and
break compilation for consumers.

## Fat JARs

The `se.alipsa.gi.fatjar-conventions` convention plugin applies GradleUp Shadow and
configures `shadowJar` with `archiveClassifier = 'fatjar'`, so the published artifact
is `<module>-<version>-fatjar.jar`. `mergeServiceFiles()` is required: several
transitive `ph-*` (ph-css/gsvg) jars and `tika-core` declare the same
`META-INF/services/*` paths, and a plain `Jar` task with
`DuplicatesStrategy.EXCLUDE` keeps only the first and silently discards the rest.

Shadow merges bundled licence, notice and dependency metadata into single entries
and excludes source-JAR `buildinfo.xml`; no duplicate archive paths remain.
Check with `unzip -l <fatjar> | awk 'NF>=4{print $4}' | sort | uniq -d`.

## JavaFX platform natives

The `se.alipsa.gi.javafx-platform` convention plugin selects OpenJFX natives from
the Gradle JVM's OS and architecture: `linux`, `linux-aarch64`, `mac`,
`mac-aarch64`, or `win`. Unsupported architectures fail the build explicitly.
Windows Arm64 needs an x64 JDK under emulation: an Arm64 JVM cannot load the
published x64 Windows JavaFX binaries.
