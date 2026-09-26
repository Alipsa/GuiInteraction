#!/usr/bin/env bash
set -euo pipefail

source "$(dirname "$0")/release-lib.sh"
fixture_dir=$(mktemp -d)
trap 'rm -rf "$fixture_dir"' EXIT
notes="$fixture_dir/release.md"

printf '# Notes\n\n## Unreleased\n\n## 0.4.1 - 2026-09-26\n- old\n' > "$notes"
chmod 664 "$notes"
if promote_unreleased_section 0.5.0 2026-10-01 "$notes"; then
    echo 'Empty Unreleased was promoted' >&2
    exit 1
fi
insert_release_section 0.5.0 2026-10-01 'abc123 new change' "$notes"
awk '/^## Unreleased$/ { unreleased = NR } /^## 0.5.0 / { current = NR } /^## 0.4.1 / { old = NR } END { exit !(unreleased < current && current < old) }' "$notes"
[ "$(ls -ld "$notes" | cut -c1-10)" = '-rw-rw-r--' ]
grep -q -- '- new change' "$notes"

printf '# Notes\n\n## Unreleased\n- curated\n' > "$notes"
chmod 664 "$notes"
promote_unreleased_section 0.5.0 2026-10-01 "$notes"
[ "$(ls -ld "$notes" | cut -c1-10)" = '-rw-rw-r--' ]
grep -q -- '- curated' "$notes"

before=$(cat "$notes")
awk() { printf '# partial output\n'; return 42; }
if insert_release_section 0.6.0 2026-10-02 'def456 next' "$notes"; then
    echo 'Failed awk unexpectedly succeeded' >&2
    exit 1
fi
unset -f awk
[ "$(cat "$notes")" = "$before" ]
mv() { return 42; }
if promote_unreleased_section 0.6.0 2026-10-02 "$notes"; then
    echo 'Failed rename unexpectedly promoted notes' >&2
    exit 1
fi
if insert_release_section 0.6.0 2026-10-02 'def456 next' "$notes"; then
    echo 'Failed rename unexpectedly inserted notes' >&2
    exit 1
fi
unset -f mv
[ "$(cat "$notes")" = "$before" ]
if find "$fixture_dir" -maxdepth 1 -name 'release.md.*' | grep -q .; then
    echo 'Failed update left a temporary changelog behind' >&2
    exit 1
fi

repo="$fixture_dir/repo"
mkdir "$repo"
(
    cd "$repo"
    export GIT_CONFIG_GLOBAL=/dev/null GIT_CONFIG_NOSYSTEM=1
    git init -q
    git config user.name 'Release fixture'
    git config user.email 'release-fixture@example.invalid'
    git config commit.gpgsign false
    git config core.hooksPath /dev/null
    printf 'README baseline\n' > README.md
    printf 'Release baseline\n' > release.md
    printf 'Build baseline\n' > build.gradle
    git add README.md release.md build.gradle
    git commit -qm 'baseline'
    require_clean_release_file release.md
    printf 'Unrelated README work\n' >> README.md
    if require_clean_release_file README.md 2>/dev/null; then
        echo 'Dirty README passed preflight' >&2
        exit 1
    fi
    printf 'Release notes\n' >> release.md
    if require_clean_release_file release.md 2>/dev/null; then
        echo 'Dirty release notes passed preflight' >&2
        exit 1
    fi
    commit_changed_release_files 0.5.0 false >/dev/null
    [ "$(git show --pretty=format: --name-only HEAD | sed '/^$/d')" = release.md ]
    grep -q 'Unrelated README work' README.md
    [ "$(git status --short)" = ' M README.md' ]
    printf 'Unrelated build work\n' >> build.gradle
    if require_clean_release_file build.gradle 2>/dev/null; then
        echo 'Dirty build script passed preflight' >&2
        exit 1
    fi
)
echo 'release-lib fixtures passed'
