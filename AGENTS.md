# Repository Guidelines

## Project Structure & Modules
- Multi-module Gradle build targeting Java 21 via the shared toolchain in `build.gradle`.
- Core Groovy code lives in `gi-common/src/main/groovy`, consumed by UI variants `gi-swing`, `gi-fx`, and `gi-console` (each under their own `src/main/groovy`).
- Test assets (charts, svg) sit under each module’s `src/test/resources`; add new fixtures beside the module they belong to.
- Gradle wrappers (`./gradlew`, per-module wrappers) and `gradle.properties` are the expected entry points; avoid invoking system Gradle directly.

## Build, Test, and Development Commands
- `./gradlew build` — compile all modules, run tests, Spotless, and SpotBugs (both static checks are part of `check`).
- `./check.sh` — reformat with Spotless and then run the full `check`; use this before pushing.
- `./gradlew test` or `./gradlew :gi-fx:test` — run the full suite or a single module; tests execute on JUnit Platform.
- `./gradlew :gi-swing:shadowJar` (likewise `:gi-fx:shadowJar`, `:gi-console:shadowJar`) — produce self-contained artifacts (`*-fatjar.jar`). Note these bundle the runtime classpath but not Groovy, which is a `compileOnly` dependency the consumer supplies.
- `./gradlew dependencyUpdates` — check for newer dependencies with the versions plugin.
- `./checkCVE.sh` — run a fresh aggregate CVE scan after exporting `NVD_API_KEY`.
- Use a JDK with JavaFX when building/running `gi-fx` (e.g., Bellsoft Full JDK); `gi-swing` and `gi-console` work on any Java 21 JDK.

## Coding Style & Naming Conventions
- Groovy first, with `@CompileStatic` where practical; keep indentation at 2 spaces and UTF-8 encoding.
- Classes: `PascalCase`; methods/fields: `camelCase`; constants: `UPPER_SNAKE_CASE`.
- Prefer reusable helpers in `gi-common`; UI-specific behavior stays inside the corresponding module.
- Fat-jar packaging goes through the `se.alipsa.gi.fatjar-conventions` plugin, which uses Shadow so duplicate `META-INF/services` entries are merged rather than dropped. Reuse it instead of custom packaging scripts.

## Testing Guidelines
- JUnit Jupiter is configured; place specs under `module/src/test/groovy` with `*Test.groovy` naming.
- Keep UI-heavy code factored so logic can be unit-tested without displays; use resource fixtures already present for rendering assertions.
- Run `./gradlew test` before pushing; add focused tests when altering IO, clipboard, or rendering behaviors.
- Always run `./gradlew test` after completing a task to validate changes.

## Commit & Pull Request Guidelines
- **Never commit code unless explicitly instructed to do so.**
- Follow the existing history: short, imperative messages (e.g., "fix publishing by …", "improve display methods").
- PRs should list the touched modules (`gi-common`, `gi-fx`, etc.), describe behavior changes, and link issues/tickets.
- Include screenshots or gifs for visual tweaks in `gi-fx`/`gi-swing`; note platform specifics if a change is OS-dependent.
- Keep release/publishing secrets (signing keys, Sonatype creds) out of the repo; supply them via local `gradle.properties` when needed.
- The `test.gsh` sample scripts in each module pin the latest **released** version for Grape. Bump them as part of a release, not as part of a SNAPSHOT bump.

## Implementation Guidelines
- A task has 3 parts, implementation, tests, and documentation. A task is not done until all 3 parts are completed.
