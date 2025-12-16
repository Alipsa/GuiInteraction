# Repository Guidelines

## Project Structure & Modules
- Multi-module Gradle build targeting Java 21 via the shared toolchain in `build.gradle`.
- Core Groovy code lives in `gi-common/src/main/groovy`, consumed by UI variants `gi-swing`, `gi-fx`, and `gi-console` (each under their own `src/main/groovy`).
- Test assets (charts, svg) sit under each module’s `src/test/resources`; add new fixtures beside the module they belong to.
- Gradle wrappers (`./gradlew`, per-module wrappers) and `gradle.properties` are the expected entry points; avoid invoking system Gradle directly.

## Build, Test, and Development Commands
- `./gradlew build` — compile all modules, run tests where present.
- `./gradlew test` or `./gradlew :gi-fx:test` — run the full suite or a single module; tests execute on JUnit Platform.
- `./gradlew :gi-swing:fatJar` (likewise `:gi-fx:fatJar`, `:gi-console:fatJar`) — produce self-contained artifacts.
- `./gradlew dependencyUpdates` — check for newer dependencies with the versions plugin.
- Use a JDK with JavaFX when building/running `gi-fx` (e.g., Bellsoft Full JDK); `gi-swing` and `gi-console` work on any Java 21 JDK.

## Coding Style & Naming Conventions
- Groovy first, with `@CompileStatic` where practical; keep indentation at 2 spaces and UTF-8 encoding.
- Classes: `PascalCase`; methods/fields: `camelCase`; constants: `UPPER_SNAKE_CASE`.
- Prefer reusable helpers in `gi-common`; UI-specific behavior stays inside the corresponding module.
- Fat-jar tasks already exclude duplicate META-INF entries—reuse them instead of custom packaging scripts.

## Testing Guidelines
- JUnit Jupiter is configured; place specs under `module/src/test/groovy` with `*Test.groovy` naming.
- Keep UI-heavy code factored so logic can be unit-tested without displays; use resource fixtures already present for rendering assertions.
- Run `./gradlew test` before pushing; add focused tests when altering IO, clipboard, or rendering behaviors.
- Always run `./gradlew test` after a task is done.

## Commit & Pull Request Guidelines
- Follow the existing history: short, imperative messages (e.g., “fix publishing by …”, “improve display methods”).
- PRs should list the touched modules (`gi-common`, `gi-fx`, etc.), describe behavior changes, and link issues/tickets.
- Include screenshots or gifs for visual tweaks in `gi-fx`/`gi-swing`; note platform specifics if a change is OS-dependent.
- Keep release/publishing secrets (signing keys, Sonatype creds) out of the repo; supply them via local `gradle.properties` when needed.

## Implementation Guidelines
- A task has 3 parts, implementation, tests, and documentation. A task is not done until all 3 parts are completed.
- Always run `./gradlew test` when a task is finished to validate changes.
