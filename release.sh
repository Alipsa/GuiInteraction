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
#
# If the version has a -SNAPSHOT suffix, it will be removed to create the release version.
# The README.md and release.md will be updated automatically with the release version.
#
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Initialize SDKMAN and use JDK 21
if [ -f ~/.sdkman/bin/sdkman-init.sh ]; then
    source ~/.sdkman/bin/sdkman-init.sh
    sdk use java 21.0.9.fx-librca 2>/dev/null || sdk use java 21-librca 2>/dev/null || echo "Using default Java"
fi

PROJECT=$(basename "$PWD")
DRY_RUN=false
BUMP_TYPE=""
README_NEEDS_UPDATE=false

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
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            exit 1
            ;;
    esac
done

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
            major=$((major + 1))
            minor=0
            patch=0
            ;;
        minor)
            minor=$((minor + 1))
            patch=0
            ;;
        patch)
            patch=$((patch + 1))
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
    sed -i.bak "s/^version = '.*'/version = '${new_version}'/" build.gradle
    rm build.gradle.bak
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
    local date=$(date +%Y-%m-%d)
    local release_notes_file="release.md"

    if [ ! -f "$release_notes_file" ]; then
        echo "# Gui Interaction Release Notes" > "$release_notes_file"
        echo "" >> "$release_notes_file"
    fi

    # Get commits since last tag
    local last_tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
    local commits=""
    if [ -n "$last_tag" ]; then
        commits=$(git log --oneline "${last_tag}..HEAD" 2>/dev/null || echo "")
    else
        commits=$(git log --oneline -20 2>/dev/null || echo "")
    fi

    # Create release notes entry
    local entry="## ${version} - ${date}\n\n"
    if [ -n "$commits" ]; then
        entry+="### Changes\n\n"
        while IFS= read -r line; do
            if [ -n "$line" ]; then
                entry+="- ${line#* }\n"
            fi
        done <<< "$commits"
    fi
    entry+="\n"

    # Insert after the release notes header
    if [ -f "$release_notes_file" ]; then
        local temp_file=$(mktemp)
        head -2 "$release_notes_file" > "$temp_file"
        echo -e "$entry" >> "$temp_file"
        tail -n +3 "$release_notes_file" >> "$temp_file"
        mv "$temp_file" "$release_notes_file"
        echo -e "${GREEN}Updated ${release_notes_file}${NC}"
    fi
}

# Publish a subproject
publish() {
    local sub=$1
    echo -e "${YELLOW}Publishing $sub to Maven Central...${NC}"
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}[DRY RUN] Would execute: ./gradlew :${sub}:clean :${sub}:build :${sub}:release${NC}"
    else
        ./gradlew ":${sub}:clean" ":${sub}:build" ":${sub}:release" --no-configuration-cache
    fi
}

# Main script
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  GuiInteraction Release Script${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

CURRENT_VERSION=$(get_version)
echo -e "Current version: ${YELLOW}${CURRENT_VERSION}${NC}"

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
elif [ "$README_NEEDS_UPDATE" = true ]; then
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}[DRY RUN] Would update README.md to match version ${CURRENT_VERSION}${NC}"
    else
        read -p "Update README.md to version ${CURRENT_VERSION}? [Y/n]: " update_readme
        if [[ ! "$update_readme" =~ ^[Nn]$ ]]; then
            update_readme_version "$CURRENT_VERSION"
            if ! git diff --quiet HEAD -- README.md; then
                if ! git add README.md; then
                    echo -e "${RED}Error: Failed to add README.md to git. Please resolve the issue and try again.${NC}" >&2
                    exit 1
                fi
                if ! git commit -m "Update README version to ${CURRENT_VERSION}" -- README.md; then
                    echo -e "${RED}Error: Failed to commit README.md version change. Please resolve the issue and try again.${NC}" >&2
                    exit 1
                fi
            else
                echo -e "${YELLOW}README.md already matches version ${CURRENT_VERSION}; no commit needed.${NC}"
            fi
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
    echo "See https://central.sonatype.org/publish/release/ for more info"
fi
echo -e "${GREEN}========================================${NC}"
