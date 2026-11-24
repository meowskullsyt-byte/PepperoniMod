#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
command -v java >/dev/null 2>&1 || { echo 'Install a Java 17 JDK first.'; exit 1; }
command -v gradle >/dev/null 2>&1 || { echo 'Install Gradle 8.1.1 or newer first.'; exit 1; }
gradle build
printf '\nIf successful, the mod JAR is in %s/build/libs/\n' "$PWD"
