#!/bin/zsh
set -euo pipefail

cd "$(dirname "$0")"
mvn -q -Dmaven.repo.local=.m2 -pl maxbots-examples -am package
exec java -jar maxbots-examples/target/maxbots-examples-0.1.0-SNAPSHOT.jar
