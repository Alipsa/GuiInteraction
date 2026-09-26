#!/usr/bin/env bash
# Sourceable changelog and version helpers for release.sh.

release_section_exists() {
    local version=$1 file=${2:-release.md}
    [ -f "$file" ] && grep -qE "^## +${version//./[.]} - " "$file"
}

# 0: promoted, 1: no curated Unreleased content, 2: version already recorded, 3: I/O error.
promote_unreleased_section() {
    local version=$1 date=$2 file=${3:-release.md} tmp
    [ -f "$file" ] || return 1
    release_section_exists "$version" "$file" && return 2
    awk '
        /^## +Unreleased[[:space:]]*$/ { inside = 1; next }
        inside && /^## / { exit }
        inside && /[^[:space:]]/ { content = 1 }
        END { exit content ? 0 : 1 }
    ' "$file" || return 1
    tmp=$(mktemp) || return 3
    awk -v heading="## ${version} - ${date}" '
        !promoted && /^## +Unreleased[[:space:]]*$/ {
            print "## Unreleased"; print ""; print heading; promoted = 1; next
        }
        { print }
    ' "$file" > "$tmp" || { rm -f "$tmp"; return 3; }
    cat "$tmp" > "$file" || { rm -f "$tmp"; return 3; }
    rm -f "$tmp"
}

# Fallback when there are no curated notes. printf preserves backslashes in subjects.
insert_release_section() {
    local version=$1 date=$2 commits=$3 file=${4:-release.md} section tmp line
    release_section_exists "$version" "$file" && return 2
    section=$(mktemp) || return 3
    {
        printf '## %s - %s\n\n' "$version" "$date"
        if [ -n "$commits" ]; then
            printf '### Changes\n\n'
            while IFS= read -r line; do
                [ -n "$line" ] && printf -- '- %s\n' "${line#* }"
            done <<< "$commits"
            printf '\n'
        fi
    } > "$section" || { rm -f "$section"; return 3; }
    if [ ! -f "$file" ]; then
        printf '# Gui Interaction Release Notes\n\n' > "$file" || { rm -f "$section"; return 3; }
    fi
    tmp=$(mktemp) || { rm -f "$section"; return 3; }
    awk -v section="$section" '
        !inserted && /^## +Unreleased[[:space:]]*$/ {
            print; next
        }
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
    ' "$file" > "$tmp" || { rm -f "$tmp" "$section"; return 3; }
    cat "$tmp" > "$file" || { rm -f "$tmp" "$section"; return 3; }
    rm -f "$tmp" "$section"
}

validate_version() {
    local version=${1%-SNAPSHOT}
    if [[ ! "$version" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
        echo "Unsupported version '${1}'. Expected MAJOR.MINOR.PATCH or MAJOR.MINOR.PATCH-SNAPSHOT, with no leading zeros." >&2
        return 1
    fi
}
