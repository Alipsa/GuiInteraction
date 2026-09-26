#!/usr/bin/env bash
set -euo pipefail

"$(dirname "$0")/release-lib-test.sh"
./gradlew spotlessApply check --console=plain
