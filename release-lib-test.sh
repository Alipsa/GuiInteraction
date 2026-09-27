#!/usr/bin/env bash
set -euo pipefail

source "$(dirname "$0")/release-lib.sh"
sorted_jdks=$(printf '%s\n' 21.0.9.fx-librca 21.0.11.fx-librca 21.0.12.fx-librca 21.0.12.1.fx-librca | sort_java_versions)
[ "$sorted_jdks" = $'21.0.12.1.fx-librca\n21.0.12.fx-librca\n21.0.11.fx-librca\n21.0.9.fx-librca' ]
fixture_dir=$(mktemp -d)
trap 'rm -rf "$fixture_dir"' EXIT
notes="$fixture_dir/release.md"
awk() {
    if [ "${FAIL_AWK:-false}" = true ]; then
        printf '# partial output\n'
        return 42
    fi
    command awk "$@"
}

printf '# Notes\n\n## Unreleased\n\n## 0.4.1 - 2026-09-26\n- old\n' > "$notes"
chmod 664 "$notes"
if promote_unreleased_section 0.5.0 2026-10-01 "$notes"; then
    echo 'Empty Unreleased was promoted' >&2
    exit 1
fi
TMPDIR="$fixture_dir" insert_release_section 0.5.0 2026-10-01 'abc123 new change' "$notes"
awk '/^## Unreleased$/ { unreleased = NR } /^## 0.5.0 / { current = NR } /^## 0.4.1 / { old = NR } END { exit !(unreleased < current && current < old) }' "$notes"
[ "$(ls -ld "$notes" | cut -c1-10)" = '-rw-rw-r--' ]
grep -q -- '- new change' "$notes"

printf '# Notes\n\n## Unreleased\n- curated\n' > "$notes"
chmod 664 "$notes"
promote_unreleased_section 0.5.0 2026-10-01 "$notes"
[ "$(ls -ld "$notes" | cut -c1-10)" = '-rw-rw-r--' ]
grep -q -- '- curated' "$notes"
if has_curated_unreleased_notes "$notes"; then
    echo 'Empty Unreleased was reported as curated' >&2
    exit 1
fi
printf '# Notes\n\n## Unreleased\n- later note\n\n## 0.5.0 - 2026-10-01\n- curated\n' > "$notes"
has_curated_unreleased_notes "$notes"
rc=0
promote_unreleased_section 0.5.0 2026-10-01 "$notes" || rc=$?
[ "$rc" -eq 2 ]

before=$(cat "$notes")
FAIL_AWK=true
if TMPDIR="$fixture_dir" insert_release_section 0.6.0 2026-10-02 'def456 next' "$notes"; then
    echo 'Failed awk unexpectedly succeeded' >&2
    exit 1
fi
FAIL_AWK=false
[ "$(cat "$notes")" = "$before" ]
printf '# Notes\n\n## Unreleased\n- curated\n' > "$notes"
before=$(cat "$notes")
mv() { return 42; }
rc=0
promote_unreleased_section 0.6.0 2026-10-02 "$notes" || rc=$?
if [ "$rc" -ne 3 ]; then
    echo "Failed rename returned $rc instead of 3 for promotion" >&2
    exit 1
fi
[ "$(cat "$notes")" = "$before" ]
rc=0
TMPDIR="$fixture_dir" insert_release_section 0.6.0 2026-10-02 'def456 next' "$notes" || rc=$?
if [ "$rc" -ne 3 ]; then
    echo "Failed rename returned $rc instead of 3 for insertion" >&2
    exit 1
fi
unset -f mv
[ "$(cat "$notes")" = "$before" ]
if find "$fixture_dir" -maxdepth 1 -name 'release.md.*' | grep -q .; then
    echo 'Failed update left a temporary changelog behind' >&2
    exit 1
fi

printf '# Notes\r\n\r\n## Unreleased\r\n- curated\r\n' > "$notes"
promote_unreleased_section 0.7.0 2026-10-03 "$notes"
if perl -ne 'exit 1 if /(?<!\r)\n/' "$notes"; then :; else
    echo 'Promotion mixed line endings' >&2
    exit 1
fi
printf '# Notes\r\n\r\n## Unreleased\r\n' > "$notes"
TMPDIR="$fixture_dir" insert_release_section 0.7.0 2026-10-03 $'abc123 valid\nabc123\nabc123   ' "$notes"
grep -q -- '- valid' "$notes"
if grep -q -- '- abc123' "$notes"; then exit 1; fi
if perl -ne 'exit 1 if /(?<!\r)\n/' "$notes"; then :; else
    echo 'Insertion mixed line endings' >&2
    exit 1
fi
if find "$fixture_dir" -maxdepth 1 -type f ! -name release.md | grep -q .; then
    echo 'Release helper left a temporary file behind' >&2
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
