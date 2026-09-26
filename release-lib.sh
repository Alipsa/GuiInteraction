#!/usr/bin/env bash
# Sourceable changelog and version helpers for release.sh.

release_section_exists() {
    local version=$1 file=${2:-release.md}
    [ -f "$file" ] && grep -qE "^## +${version//./[.]} - " "$file"
}

# 0: promoted, 1: no Unreleased section, 2: version already recorded.
promote_unreleased_section() {
    local version=$1 date=$2 file=${3:-release.md} tmp
    [ -f "$file" ] || return 1
    release_section_exists "$version" "$file" && return 2
    grep -qE '^## +Unreleased[[:space:]]*$' "$file" || return 1
    tmp=$(mktemp)
    awk -v heading="## ${version} - ${date}" '
        !promoted && /^## +Unreleased[[:space:]]*$/ {
            print "## Unreleased"; print ""; print heading; promoted = 1; next
        }
        { print }
    ' "$file" > "$tmp"
    mv "$tmp" "$file"
}

# Fallback when there are no curated notes. printf preserves backslashes in subjects.
insert_release_section() {
    local version=$1 date=$2 commits=$3 file=${4:-release.md} section tmp line
    release_section_exists "$version" "$file" && return 2
    section=$(mktemp)
    {
        printf '## %s - %s\n\n' "$version" "$date"
        if [ -n "$commits" ]; then
            printf '### Changes\n\n'
            while IFS= read -r line; do
                [ -n "$line" ] && printf -- '- %s\n' "${line#* }"
            done <<< "$commits"
            printf '\n'
        fi
    } > "$section"
    [ -f "$file" ] || printf '# Gui Interaction Release Notes\n\n' > "$file"
    tmp=$(mktemp)
    awk -v section="$section" '
        !inserted && /^## / {
            while ((getline line < section) > 0) print line
            close(section); inserted = 1
        }
        { print }
        END {
            if (!inserted) {
                while ((getline line < section) > 0) print line
                close(section)
            }
        }
    ' "$file" > "$tmp"
    mv "$tmp" "$file"
    rm -f "$section"
}

validate_version() {
    local version=${1%-SNAPSHOT}
    if [[ ! "$version" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
        echo "Unsupported version '${1}'. Expected MAJOR.MINOR.PATCH or MAJOR.MINOR.PATCH-SNAPSHOT, with no leading zeros." >&2
        return 1
    fi
}
