#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
./release-lib-test.sh
./gradlew spotlessApply check --console=plain
