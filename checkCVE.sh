#!/usr/bin/env zsh
./gradlew dependencyCheckAggregate --rerun-tasks --no-configuration-cache --console=plain -Denable-native-access=ALL-UNNAMED
