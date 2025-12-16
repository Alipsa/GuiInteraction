# TODO / Roadmap

## 1. Testing & documentation
- 1.1 [ ] Add missing tests for utility methods and headless environments.
- 1.2 [ ] Document public APIs with Javadoc/Groovydoc comments and usage examples in `docs/`.
- 1.3 [ ] Clarify module README files with setup instructions and feature overviews.

## 2. Functional fixes
- 2.1. [x] Console output is dropped: `gi-console/src/main/groovy/se/alipsa/gi/txt/InOut.groovy` parses HTML and builds matrices but never prints them; emit the sanitized text/table so console users see results.
- 2.2. [ ] Guard empty option lists: `promptSelect(String, Collection)` in `gi-common/.../AbstractInOut.groovy` calls `options.iterator().next()` and throws on empty collections; add validation or a clearer exception before delegating.
- 2.3. [ ] Add Swing SVG support: `gi-swing/.../InOut.groovy` currently bails with “no support”. Use Batik so bundled SVG fixtures can be previewed.
- 2.4. [ ] Console password fallback: `gi-console/.../InOut.groovy` returns `null` when `System.console()` is unavailable; provide a masked/non-echo stdin fallback for IDEs/CI.
- 2.5. [ ] Broaden `urlExists`: close the HTTP connection and treat 2xx/3xx as success to avoid resource leaks and false negatives.

## 3. Build & platform coverage
- 3.1 [ ] Add automated tests (JUnit/Gradle) for helpers like `FileUtils.baseName`/`getResourceUrl` and content-type detection using existing `src/test/resources` assets; add headless checks for table/clipboard adapters where feasible.
- 3.2. [ ] Set up CI (e.g., GitHub Actions) to run `./gradlew build` and `./gradlew dependencyUpdates` across modules to catch regressions and dependency drift.
- 3.3. [ ] Increase Gradle JVM memory: add `org.gradle.jvmargs=-Xmx1g -XX:MaxMetaspaceSize=512m` to `gradle.properties` to avoid metaspace warnings during builds.
- 3.4. [ ] Extract duplicated fatJar task logic to a shared Gradle convention plugin instead of repeating in each module.
- 3.5. [ ] Flesh out `release.sh` to automate version bumps, changelog generation, and GitHub releases.

## 4. Code quality & refactoring
- 4.1 [ ] Replace 12+ `System.err.println()` calls with a proper logging framework (SLF4J/Logback); locations include `gi-fx/InOut.groovy`, `gi-fx/Viewer.groovy`, `gi-swing/InOut.groovy`.
- 4.2 [ ] Fix `printStackTrace()` usage in `gi-swing/InOut.groovy:268`; use logger instead.
- 4.3 [ ] Remove code duplication: `AbstractInOut.getResourceUrl()` and `FileUtils.getResourceUrl()` have nearly identical logic; make one delegate to the other.
- 4.4 [ ] Add headless environment detection (`GraphicsEnvironment.isHeadless()`) in gi-fx and gi-swing to fail gracefully instead of crashing on headless systems.
- 4.5 [ ] Document and standardize null handling policy across all methods; some return null on failure, others throw exceptions.

## 5. Security & dependencies
- 5.1 [ ] Add OWASP Dependency Check plugin to scan for CVEs in transitive dependencies.
- 5.2 [ ] Document the Tika dependency impact (100+ transitive deps, ~50MB fat JAR) and consider providing a lighter "minimal" build profile.
- 5.3 [ ] Document required signing credentials setup (signing.keyId, sonatypeUsername, sonatypePassword) for publishing.

## 6. Feature enhancements
- 6.2 [ ] Consider adding async/streaming APIs (CompletableFuture) for long-running operations.
- 6.3 [ ] Cache Tika parser instances to avoid expensive re-initialization on each content-type detection call.
