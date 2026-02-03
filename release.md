# Gui Interaction Release Notes

## [0.3.0-SNAPSHOT] - 2026-02-03
- Build: introduced Gradle version catalog `gradle/libs.versions.toml` to centralize dependency versions across modules.
- Build: updated Gradle wrapper to 9.3.1 and OWASP dependency check plugin to 12.2.0.
- Build: bumped `se.alipsa.nexus-release-plugin` to 2.0.1 across modules.
- Console: moved implementation to `se.alipsa.gi.console.InOut` and left `se.alipsa.gi.txt.InOut` as a deprecated shim.
- Console: clipboard operations now log and no-op/return null when running headless.
- Logging: switched from direct SLF4J usage to `se.alipsa.matrix.core.util.Logger` for better script-friendly output.
- Swing: SVG display now uses matrix-charts `ChartToSwing`/`SvgPanel`, removing the Batik dependency.
- Swing: fixed IntelliJ type inference for extension filters and added empty-matrix handling.
- JavaFX: avoid deadlocks by running blocking dialogs/clipboard access directly on the FX thread and validate initial directories.
- JavaFX: guard empty grids in `Viewer` and return null for empty clipboard file lists.
- Common: content type detection now handles non-file classpath resources and added a test for classpath names.
- Docs: added FX threading and chooser behavior notes in `docs/API-Guide.md`.

## [0.2.0] - 2025-12-17
- Compile static where possible for improved performance and reliability.
- Testing & documentation
  - Added missing tests for utility methods and headless environments.
  - Documented public APIs with Javadoc/Groovydoc comments and usage examples in `docs/`.
  - Clarified module README files with setup instructions and feature overviews.
- Functional fixes
  - Console output was dropped: `gi-console/src/main/groovy/se/alipsa/gi/txt/InOut.groovy` parses HTML and builds matrices but never prints them; emit the sanitized text/table so console users see results.
  - Guarded empty option lists: `promptSelect(String, Collection)` in `gi-common/.../AbstractInOut.groovy` calls `options.iterator().next()` and throws on empty collections; add validation or a clearer exception before delegating.
  - Added Swing SVG support: `gi-swing/.../InOut.groovy` currently bails with "no support". Use Batik so bundled SVG fixtures can be previewed.
  - Console password fallback: `gi-console/.../InOut.groovy` returns `null` when `System.console()` is unavailable; provide a masked/non-echo stdin fallback for IDEs/CI.
  - Broadened `urlExists`: close the HTTP connection and treat 2xx/3xx as success to avoid resource leaks and false negatives.
- Build & platform coverage
  - Added automated tests (JUnit/Gradle) for helpers like `FileUtils.baseName`/`getResourceUrl` and content-type detection using existing `src/test/resources` assets; add headless checks for table/clipboard adapters where feasible.
  - Set up CI (e.g., GitHub Actions) to run `./gradlew build` and `./gradlew dependencyUpdates` across modules to catch regressions and dependency drift.
  - Increased Gradle JVM memory: add `org.gradle.jvmargs=-Xmx1g -XX:MaxMetaspaceSize=512m` to `gradle.properties` to avoid metaspace warnings during builds.
  - Extracted duplicated fatJar task logic to a shared Gradle convention plugin instead of repeating in each module.
  - Flushed out `release.sh` to automate version bumps, changelog generation, and GitHub releases.
- Code quality & refactoring
  - Replaced 12+ `System.err.println()` calls with a proper logging framework (SLF4J/Logback); locations include `gi-fx/InOut.groovy`, `gi-fx/Viewer.groovy`, `gi-swing/InOut.groovy`.
  - Fixed `printStackTrace()` usage in `gi-swing/InOut.groovy:268`; use logger instead.
  - Removed code duplication: `AbstractInOut.getResourceUrl()` and `FileUtils.getResourceUrl()` have nearly identical logic; make one delegate to the other.
  - Added headless environment detection (`GraphicsEnvironment.isHeadless()`) in gi-fx and gi-swing to fail gracefully instead of crashing on headless systems.
  - Documented and standardized null handling policy across all methods to favour exceptions; some return null on failure, others throw exceptions.
- Security & dependencies
  - Added OWASP Dependency Check plugin to scan for CVEs in transitive dependencies.
  - Documented the Tika dependency impact (100+ transitive deps, ~50MB fat JAR) and consider providing a lighter "minimal" build profile.
  - Document required signing credentials setup (signing.keyId, sonatypeUsername, sonatypePassword) for publishing.

## [0.1.0] - 2024-04-19
- Initial release of se.alipsa.gi modules:
  - gi-fx
  - gi-swing
  - gi-console
