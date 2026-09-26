#!/usr/bin/env bash
#
# Release script for GuiInteraction
#
# Prerequisites:
#   - SDKMAN installed with JDK 21
#   - Signing credentials in gradle.properties (signing.keyId, signing.password, signing.secretKeyRingFile)
#   - Sonatype credentials in gradle.properties (sonatypeUsername, sonatypePassword)
#   - GitHub CLI (gh) installed and authenticated for creating releases
#
# Usage:
#   ./release.sh              - Release current version (strips -SNAPSHOT if present)
#   ./release.sh --bump minor - Bump version and release (major, minor, patch)
#   ./release.sh --dry-run    - Show what would be released without publishing
#   ./release.sh --skip gi-common,gi-console
#                              - Skip modules already published in a prior partial
#                                run; use with the SAME version and explicitly
#                                confirm that every listed module was published
#
# If the version has a -SNAPSHOT suffix, it will be removed to create the release version.
# The README.md and release.md will be updated automatically with the release version.
#
set -e

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
# shellcheck source=release-lib.sh
source "${SCRIPT_DIR}/release-lib.sh"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Initialize SDKMAN and select an installed JavaFX-capable JDK 21
if [ -f ~/.sdkman/bin/sdkman-init.sh ]; then
    source ~/.sdkman/bin/sdkman-init.sh

    sdk_java_dir="${SDKMAN_CANDIDATES_DIR:-$HOME/.sdkman/candidates}/java"
    java21_fx_candidates=()
    if [ -d "$sdk_java_dir" ]; then
        while IFS= read -r candidate; do
            java21_fx_candidates+=("$candidate")
        done < <(
            find "$sdk_java_dir" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' |
                awk 'tolower($0) ~ /^21([.-]|$)/ && tolower($0) ~ /fx/' |
                sort -V -r
        )
    fi

    if [ "${#java21_fx_candidates[@]}" -gt 0 ]; then
        echo "Installed JavaFX-capable JDK 21 candidates:"
        printf '  %s\n' "${java21_fx_candidates[@]}"
        echo "Using Java ${java21_fx_candidates[0]}"
        sdk use java "${java21_fx_candidates[0]}" 2>/dev/null || {
            echo -e "${RED}Could not activate Java ${java21_fx_candidates[0]}.${NC}" >&2
            exit 1
        }
    else
        echo -e "${RED}No installed JavaFX-capable JDK 21 was found in ${sdk_java_dir}.${NC}" >&2
        echo "Install one with SDKMAN, then rerun the release script." >&2
        exit 1
    fi
else
    echo "SDKMAN is not installed; using the default Java."
fi

PROJECT=$(basename "$PWD")
DRY_RUN=false
BUMP_TYPE=""
README_NEEDS_UPDATE=false
PUBLISHED_MODULES=""
SKIP_MODULES=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --bump)
            BUMP_TYPE="$2"
            shift 2
            ;;
        --skip)
            if [ -z "${2:-}" ]; then
                echo -e "${RED}--skip requires a comma-separated module list.${NC}" >&2
                exit 1
            fi
            SKIP_MODULES="${2// /}"
            shift 2
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            exit 1
            ;;
    esac
done

if [ -n "$BUMP_TYPE" ] && [ -n "$SKIP_MODULES" ]; then
    echo -e "${RED}--skip cannot be combined with --bump; resume a partial release with its original version.${NC}" >&2
    exit 1
fi

# Whether a module name appears in the comma-separated --skip list
is_skipped() {
    local sub=$1
    [[ ",${SKIP_MODULES}," == *",${sub},"* ]]
}

# Reject typos and require an explicit acknowledgement before a release omits
# modules. Maven Central cannot be used to repair a release missing an artifact.
validate_skipped_modules() {
    if [ -z "$SKIP_MODULES" ]; then
        return
    fi
    if [[ "$SKIP_MODULES" == ,* || "$SKIP_MODULES" == *, || "$SKIP_MODULES" == *,,* ]]; then
        echo -e "${RED}--skip must contain comma-separated module names without empty entries.${NC}" >&2
        exit 1
    fi

    local sub seen="," confirmation
    local skipped_modules=()
    IFS=',' read -r -a skipped_modules <<< "$SKIP_MODULES"
    for sub in "${skipped_modules[@]}"; do
        case "$sub" in
            gi-common|gi-console|gi-fx|gi-swing) ;;
            *)
                echo -e "${RED}Unknown module in --skip: ${sub}.${NC}" >&2
                exit 1
                ;;
        esac
        if [[ "$seen" == *",${sub},"* ]]; then
            echo -e "${RED}Module appears more than once in --skip: ${sub}.${NC}" >&2
            exit 1
        fi
        seen="${seen}${sub},"
    done

    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}[DRY RUN] Would require confirmation that skipped modules are already published: ${SKIP_MODULES}${NC}"
        return
    fi
    echo -e "${YELLOW}Skipping ${SKIP_MODULES} means their artifacts will not be published by this release.${NC}"
    if ! read -r -p "Type 'already published' to confirm they were published with this version: " confirmation || [ "$confirmation" != "already published" ]; then
        echo -e "${RED}Release aborted: skipped modules were not confirmed as already published.${NC}" >&2
        exit 1
    fi
}

validate_skipped_modules

# Get current version from build.gradle
get_version() {
    grep -E '^\s*version\s*=\s*["'\'']' build.gradle | sed -E 's/^\s*version\s*=\s*["'\'']([^"'\''"]+)["'\''].*/\1/'
}

# Bump version based on type (major, minor, patch)
bump_version() {
    local version=$1
    local type=$2
    local major minor patch

    IFS='.' read -r major minor patch <<< "${version%-SNAPSHOT}"

    case $type in
        major)
            major=$((10#$major + 1))
            minor=0
            patch=0
            ;;
        minor)
            minor=$((10#$minor + 1))
            patch=0
            ;;
        patch)
            patch=$((10#$patch + 1))
            ;;
        *)
            echo -e "${RED}Invalid bump type: $type. Use major, minor, or patch${NC}"
            exit 1
            ;;
    esac

    echo "${major}.${minor}.${patch}"
}

# Update version in build.gradle
update_version() {
    local new_version=$1
    sed -i.bak -E "s/^([[:space:]]*)version[[:space:]]*=[[:space:]]*'[^']*'/\1version = '${new_version}'/" build.gradle
    rm build.gradle.bak
    local written
    written=$(get_version)
    if [ "$written" != "$new_version" ]; then
        echo -e "${RED}Error: build.gradle still reports version '${written}' after updating to '${new_version}'.${NC}" >&2
        exit 1
    fi
    echo -e "${GREEN}Updated build.gradle version to ${new_version}${NC}"
}

# Update version in README.md
update_readme_version() {
    local new_version=$1
    # Update all version references in README.md (Gradle, Maven, Grape examples)
    sed -i.bak -E "s/(gi-(swing|fx|console):)[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?/\1${new_version}/g" README.md
    sed -i.bak -E "s/(<version>)[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?(<\/version>)/\1${new_version}\3/g" README.md
    rm -f README.md.bak
    echo -e "${GREEN}Updated README.md version to ${new_version}${NC}"
}

# Check if README.md has the correct version
check_readme_version() {
    local expected_version=$1
    local readme_versions=$(grep -oE '(gi-(swing|fx|console):)[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?' README.md | head -1 | sed -E 's/gi-(swing|fx|console)://')

    if [ "$readme_versions" != "$expected_version" ]; then
        echo -e "${RED}Warning: README.md contains version '${readme_versions}' but releasing '${expected_version}'${NC}"
        return 1
    fi
    return 0
}

# Generate release notes entry
generate_release_notes() {
    local version=$1
    local date
    date=$(date +%Y-%m-%d)
    local release_notes_file="release.md"
    local rc=0

    if [ ! -f "$release_notes_file" ]; then
        printf '# Gui Interaction Release Notes\n\n' > "$release_notes_file"
    fi
    promote_unreleased_section "$version" "$date" "$release_notes_file" || rc=$?
    case $rc in
        0) echo -e "${GREEN}Promoted Unreleased notes for ${version}${NC}"; return ;;
        2) echo -e "${YELLOW}Notes for ${version} already exist${NC}"; return ;;
    esac

    local last_tag commits
    last_tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
    if [ -n "$last_tag" ]; then
        commits=$(git log --oneline "${last_tag}..HEAD" 2>/dev/null || echo "")
    else
        commits=$(git log --oneline -20 2>/dev/null || echo "")
    fi
    rc=0
    insert_release_section "$version" "$date" "$commits" "$release_notes_file" || rc=$?
    if [ "$rc" -eq 0 ]; then
        echo -e "${GREEN}Updated ${release_notes_file} from commit log${NC}"
    else
        echo -e "${YELLOW}${release_notes_file} already has notes for ${version}${NC}"
    fi
}

# Publish a subproject
publish() {
    local sub=$1
    if is_skipped "$sub"; then
        echo -e "${YELLOW}Skipping $sub (already published; --skip was given)${NC}"
        PUBLISHED_MODULES="${PUBLISHED_MODULES}${PUBLISHED_MODULES:+, }${sub}"
        return 0
    fi
    echo -e "${YELLOW}Publishing $sub to Maven Central...${NC}"
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}[DRY RUN] Would execute: ./gradlew :${sub}:clean :${sub}:build :${sub}:release${NC}"
        return 0
    fi
    if ! ./gradlew ":${sub}:clean" ":${sub}:build" ":${sub}:release" --no-configuration-cache; then
        echo -e "${RED}Error: publishing ${sub} failed.${NC}" >&2
        if [ -n "$PUBLISHED_MODULES" ]; then
            echo -e "${RED}These modules were ALREADY published to Maven Central and cannot be unpublished:${NC}" >&2
            echo -e "${RED}  ${PUBLISHED_MODULES}${NC}" >&2
            echo -e "${YELLOW}The release commit is local and unpushed, and no tag was created.${NC}" >&2
            echo -e "${YELLOW}Resolve the failure, then re-run with the SAME version and:${NC}" >&2
            echo -e "${YELLOW}  --skip ${PUBLISHED_MODULES// /}${NC}" >&2
        fi
        exit 1
    fi
    PUBLISHED_MODULES="${PUBLISHED_MODULES}${PUBLISHED_MODULES:+, }${sub}"
}

# Main script
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  GuiInteraction Release Script${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

CURRENT_VERSION=$(get_version)
echo -e "Current version: ${YELLOW}${CURRENT_VERSION}${NC}"
if ! validate_version "$CURRENT_VERSION"; then
    echo -e "${RED}Aborting: build.gradle version '${CURRENT_VERSION}' is not MAJOR.MINOR.PATCH[-SNAPSHOT].${NC}" >&2
    exit 1
fi

commit_release_version() {
    local release_version=$1
    update_version "$release_version"
    update_readme_version "$release_version"
    generate_release_notes "$release_version"

    if ! git add build.gradle README.md release.md; then
        echo -e "${RED}Error: Failed to add files to git. Please resolve the issue and try again.${NC}" >&2
        exit 1
    fi
    if ! git commit -m "Release version ${release_version}" -- build.gradle README.md release.md; then
        echo -e "${RED}Error: Failed to commit version change. Please resolve the issue and try again.${NC}" >&2
        exit 1
    fi
}

# A requested bump determines the release version, including when the current
# version is a snapshot. This avoids first committing the unbumped version and
# then publishing a different version.
if [ -n "$BUMP_TYPE" ]; then
    RELEASE_VERSION=$(bump_version "$CURRENT_VERSION" "$BUMP_TYPE")
    if ! validate_version "$RELEASE_VERSION"; then
        echo -e "${RED}Aborting: bumped version '${RELEASE_VERSION}' is malformed.${NC}" >&2
        exit 1
    fi
    echo -e "Bumping version to: ${GREEN}${RELEASE_VERSION}${NC}"
elif echo "$CURRENT_VERSION" | grep -q '\-SNAPSHOT'; then
    RELEASE_VERSION="${CURRENT_VERSION%-SNAPSHOT}"
    echo -e "Stripping SNAPSHOT suffix: ${YELLOW}${CURRENT_VERSION}${NC} -> ${GREEN}${RELEASE_VERSION}${NC}"
else
    RELEASE_VERSION="$CURRENT_VERSION"
    if ! check_readme_version "$CURRENT_VERSION"; then
        README_NEEDS_UPDATE=true
    fi
fi

CURRENT_VERSION="$RELEASE_VERSION"

# Check if version has already been released (git tag exists)
TAG="v${CURRENT_VERSION}"
if git rev-parse "$TAG" >/dev/null 2>&1 || git ls-remote --tags origin | grep -q "refs/tags/$TAG$"; then
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}Warning: Tag $TAG already exists. This version may have already been released.${NC}"
    else
        echo -e "${RED}Warning: Version ${CURRENT_VERSION} appears to have already been released (tag $TAG exists).${NC}"
        read -p "Continue anyway? [y/N]: " confirm
        if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
            echo -e "${RED}Aborting release.${NC}"
            exit 1
        fi
    fi
fi

# Only mutate release files after the tag check has passed.
if [ "$DRY_RUN" = false ] && [ "$RELEASE_VERSION" != "$(get_version)" ]; then
    commit_release_version "$RELEASE_VERSION"
elif [ "$DRY_RUN" = true ] && [ "$RELEASE_VERSION" != "$(get_version)" ]; then
    echo -e "${YELLOW}[DRY RUN] Would update build.gradle and README.md to ${RELEASE_VERSION}${NC}"
else
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}[DRY RUN] Would update release.md for ${CURRENT_VERSION}${NC}"
        if [ "$README_NEEDS_UPDATE" = true ]; then
            echo -e "${YELLOW}[DRY RUN] Would update README.md to match version ${CURRENT_VERSION}${NC}"
        fi
    else
        generate_release_notes "$CURRENT_VERSION"
        if [ "$README_NEEDS_UPDATE" = true ]; then
            read -p "Update README.md to version ${CURRENT_VERSION}? [Y/n]: " update_readme
            if [[ ! "$update_readme" =~ ^[Nn]$ ]]; then
                update_readme_version "$CURRENT_VERSION"
            fi
        fi
        if ! git diff --quiet HEAD -- README.md release.md; then
            git add README.md release.md
            git commit -m "Update release notes for ${CURRENT_VERSION}" -- README.md release.md
        fi
    fi
fi
echo ""
echo -e "Releasing version: ${GREEN}${CURRENT_VERSION}${NC}"
echo ""

# Run tests first
echo -e "${YELLOW}Running tests...${NC}"
if [ "$DRY_RUN" = true ]; then
    echo -e "${YELLOW}[DRY RUN] Would run: ./gradlew test${NC}"
else
    ./gradlew test
fi

# Publish each module
publish 'gi-common'
publish 'gi-console'
publish 'gi-fx'
publish 'gi-swing'

echo ""

# Create GitHub release
TAG="v${CURRENT_VERSION}"
RELEASE_TITLE="Ver ${CURRENT_VERSION}"

if [ "$DRY_RUN" = true ]; then
    echo -e "${YELLOW}[DRY RUN] Would push commits to origin${NC}"
    echo -e "${YELLOW}[DRY RUN] Would create GitHub release:${NC}"
    echo -e "${YELLOW}  Tag: ${TAG}${NC}"
    echo -e "${YELLOW}  Title: ${RELEASE_TITLE}${NC}"
    echo -e "${YELLOW}  Command: gh release create ${TAG} --title \"${RELEASE_TITLE}\" --generate-notes${NC}"
else
    echo -e "${YELLOW}Pushing commits to origin...${NC}"
    if ! git push origin; then
        echo -e "${RED}Error: Failed to push commits. Please resolve the issue and try again.${NC}" >&2
        exit 1
    fi

    echo -e "${YELLOW}Creating GitHub release...${NC}"
    if ! gh release create "${TAG}" --title "${RELEASE_TITLE}" --generate-notes; then
        echo -e "${RED}Error: Failed to create GitHub release. Please create it manually.${NC}" >&2
        echo -e "${YELLOW}You can create it at: https://github.com/Alipsa/GuiInteraction/releases/new${NC}"
    else
        echo -e "${GREEN}GitHub release created successfully!${NC}"
    fi
fi

echo ""
echo -e "${GREEN}========================================${NC}"
if [ "$DRY_RUN" = true ]; then
    echo -e "${YELLOW}[DRY RUN] Release simulation complete${NC}"
else
    echo -e "${GREEN}$PROJECT v${CURRENT_VERSION} released to Maven Central and GitHub!${NC}"
    echo ""
    echo "See https://central.sonatype.com/publishing/deployments for more info"
fi
echo -e "${GREEN}========================================${NC}"
