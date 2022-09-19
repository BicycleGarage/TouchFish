#!/bin/sh
TouchFish="$(dirname "$0")"/TouchFish.jar
if [ -n "$JAVA_HOME" ]; then
  $JAVA_HOME/bin/java -jar "$TouchFish" "$@"
else
  java -jar "$TouchFish" "$@"
fi
