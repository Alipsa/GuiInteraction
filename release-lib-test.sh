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
awk() { return 42; }
if insert_release_section 0.6.0 2026-10-02 'def456 next' "$notes"; then
    echo 'Failed awk unexpectedly succeeded' >&2
    exit 1
fi
unset -f awk
[ "$(cat "$notes")" = "$before" ]

repo="$fixture_dir/repo"
mkdir "$repo"
(
    cd "$repo"
    git init -q
    git config user.name 'Release fixture'
    git config user.email 'release-fixture@example.invalid'
    printf 'README baseline\n' > README.md
    printf 'Release baseline\n' > release.md
    git add README.md release.md
    git commit -qm 'baseline'
    require_clean_release_file release.md
    printf 'Unrelated README work\n' >> README.md
    if require_clean_release_file README.md 2>/dev/null; then
        echo 'Dirty README passed preflight' >&2
        exit 1
    fi
    printf 'Release notes\n' >> release.md
    commit_changed_release_files 0.5.0 false >/dev/null
    [ "$(git show --pretty=format: --name-only HEAD | sed '/^$/d')" = release.md ]
    grep -q 'Unrelated README work' README.md
    [ "$(git status --short)" = ' M README.md' ]
)
echo 'release-lib fixtures passed'
