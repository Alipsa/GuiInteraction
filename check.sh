#!/usr/bin/env bash
set -euo pipefail

./gradlew spotlessApply check --no-configuration-cache --console=plain
