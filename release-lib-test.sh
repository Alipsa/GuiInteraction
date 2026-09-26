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
echo 'release-lib fixtures passed'
