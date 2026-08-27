#!/usr/bin/env sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
build_dir="$project_dir/out/test"

rm -rf "$build_dir"
mkdir -p "$build_dir"

find "$project_dir/src" "$project_dir/test" -name '*.java' -print \
  | sort \
  | xargs javac -cp "$project_dir/libs/jl1.0.1.jar" -d "$build_dir"

java -ea -cp "$build_dir:$project_dir/libs/jl1.0.1.jar" battle.BattleAITest
