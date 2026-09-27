#!/usr/bin/env bash
# Sourceable changelog and version helpers for release.sh.

# SDKMAN names start with numeric Java version components. Sort those components
# in descending order without relying on GNU sort -V (unavailable on macOS).
sort_java_versions() {
    LC_ALL=C sort -t. -k1,1nr -k2,2nr -k3,3nr -k4,4nr
}

release_section_exists() {
    local version=$1 file=${2:-release.md}
    [ -f "$file" ] && grep -qE "^## +${version//./[.]} - " "$file"
}

has_curated_unreleased_notes() {
    local file=${1:-release.md}
    [ -f "$file" ] || return 1
    awk '
        /^## +Unreleased[[:space:]]*$/ { inside = 1; next }
        inside && /^## / { exit }
        inside && /[^[:space:]]/ { content = 1 }
        END { exit content ? 0 : 1 }
    ' "$file"
}

# 0: promoted, 1: no curated Unreleased content, 2: version already recorded, 3: I/O error.
promote_unreleased_section() (
    local version=$1 date=$2 file=${3:-release.md} tmp
    [ -f "$file" ] || return 1
    release_section_exists "$version" "$file" && return 2
    has_curated_unreleased_notes "$file" || return 1
    tmp=$(mktemp "${file}.XXXXXX") || return 3
    trap 'rm -f "$tmp"' EXIT
    trap 'exit 130' INT TERM
    local ending=$'\n'
    grep -q $'\r' "$file" && ending=$'\r\n'
    awk -v heading="## ${version} - ${date}" -v ending="$ending" '
        BEGIN { ORS = ending }
        { sub(/\r$/, "") }
        !promoted && /^## +Unreleased[[:space:]]*$/ {
            print "## Unreleased"; print ""; print heading; promoted = 1; next
        }
        { print }
    ' "$file" > "$tmp" || { rm -f "$tmp"; return 3; }
    local mode
    mode=$(stat -c %a "$file" 2>/dev/null || stat -f %Lp "$file") || { rm -f "$tmp"; return 3; }
    chmod "$mode" "$tmp" || { rm -f "$tmp"; return 3; }
    mv "$tmp" "$file" || { rm -f "$tmp"; return 3; }
)

# Fallback when there are no curated notes. printf preserves backslashes in subjects.
insert_release_section() (
    local version=$1 date=$2 commits=$3 file=${4:-release.md} section tmp='' line
    release_section_exists "$version" "$file" && return 2
    section=$(mktemp) || return 3
    trap 'rm -f "$section" "$tmp"' EXIT
    trap 'exit 130' INT TERM
    local ending=$'\n'
    if [ -f "$file" ] && grep -q $'\r' "$file"; then ending=$'\r\n'; fi
    {
        printf '## %s - %s%s%s' "$version" "$date" "$ending" "$ending"
        if [ -n "$commits" ]; then
            printf '### Changes%s%s' "$ending" "$ending"
            while IFS= read -r line; do
                [[ "$line" == *' '* ]] || continue
                local subject=${line#* }
                [[ "$subject" =~ [^[:space:]] ]] && printf -- '- %s%s' "$subject" "$ending"
            done <<< "$commits"
            printf '%s' "$ending"
        fi
    } > "$section" || { rm -f "$section"; return 3; }
    if [ ! -f "$file" ]; then
        printf '# Gui Interaction Release Notes\n\n' > "$file" || { rm -f "$section"; return 3; }
    fi
    tmp=$(mktemp "${file}.XXXXXX") || { rm -f "$section"; return 3; }
    awk -v section="$section" -v ending="$ending" '
        BEGIN { ORS = ending }
        { sub(/\r$/, "") }
        !inserted && /^## +Unreleased[[:space:]]*$/ {
            print; next
        }
        !inserted && /^## / {
            while ((getline line < section) > 0) { sub(/\r$/, "", line); print line }
            close(section); inserted = 1
        }
        { print }
        END {
            if (!inserted) {
                while ((getline line < section) > 0) { sub(/\r$/, "", line); print line }
                close(section)
            }
        }
    ' "$file" > "$tmp" || { rm -f "$tmp" "$section"; return 3; }
    local mode
    mode=$(stat -c %a "$file" 2>/dev/null || stat -f %Lp "$file") || { rm -f "$tmp" "$section"; return 3; }
    chmod "$mode" "$tmp" || { rm -f "$tmp" "$section"; return 3; }
    mv "$tmp" "$file" || { rm -f "$tmp" "$section"; return 3; }
    rm -f "$section"
)

validate_version() {
    local version=${1%-SNAPSHOT}
    if [[ ! "$version" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
        echo "Unsupported version '${1}'. Expected MAJOR.MINOR.PATCH or MAJOR.MINOR.PATCH-SNAPSHOT, with no leading zeros." >&2
        return 1
    fi
}

# Refuse to overwrite a file carrying edits from before this release run.
require_clean_release_file() {
    local file=$1
    if ! git diff --quiet HEAD -- "$file"; then
        echo "${file} has pre-existing changes; commit or move them aside before releasing." >&2
        return 1
    fi
}

# Commit only files this run was allowed to change. A declined README update
# must never sweep unrelated README edits into the release commit.
commit_changed_release_files() {
    local version=$1 include_readme=$2
    local -a files=()
    if [ "$include_readme" = true ] && ! git diff --quiet HEAD -- README.md; then
        files+=(README.md)
    fi
    if ! git diff --quiet HEAD -- release.md; then
        files+=(release.md)
    fi
    [ "${#files[@]}" -gt 0 ] || return 0
    if ! git add "${files[@]}"; then
        echo "Failed to stage release files for ${version}" >&2
        return 1
    fi
    if ! git commit -m "Update release files for ${version}" -- "${files[@]}"; then
        echo "Failed to commit release files for ${version}; resolve the git error before retrying" >&2
        return 1
    fi
}
