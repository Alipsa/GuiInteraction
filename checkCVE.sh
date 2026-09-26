#!/usr/bin/env bash
set -euo pipefail

if [ -z "${NVD_API_KEY:-}" ]; then
    echo 'NVD_API_KEY is required; request one at https://nvd.nist.gov/developers/request-an-api-key' >&2
    exit 1
fi

./gradlew dependencyCheckAggregate --rerun-tasks --no-configuration-cache --console=plain
