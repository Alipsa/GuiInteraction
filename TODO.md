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
