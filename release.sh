#!/usr/bin/env bash
set -e
source ~/.sdkman/bin/sdkman-init.sh
source jdk21
PROJECT=$(basename "$PWD")

if grep "version '" build.gradle | grep -q 'SNAPSHOT'; then
  echo "$PROJECT snapshot cannot be published to maven central"
  exit 1
fi
publish() {
  sub=$1
  echo "Publishing $sub to maven central"
  ./gradlew :sub:clean :sub:build :sub:release
}
publish 'gi-common'
publish 'gi-console'
publish 'gi-fx'
publish 'gi-swing'

echo "$PROJECT uploaded and released"
echo "see https://central.sonatype.org/publish/release/ for more info"
#browse https://oss.sonatype.org &
