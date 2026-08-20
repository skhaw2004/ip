#!/usr/bin/env bash

# Compiles Stuart and runs it against input.txt, diffing the output
# against EXPECTED.TXT. Run from anywhere; paths are resolved relative
# to this script's location.

cd "$(dirname "$0")"

cd ..
mkdir -p out/production/ip
javac -d out/production/ip src/main/java/*.java
if [ $? -ne 0 ]; then
    echo "********** BUILD FAILURE **********"
    exit 1
fi
cd text-ui-test

java -classpath ../out/production/ip Stuart < input.txt > ACTUAL.TXT

# Normalize line endings (in case EXPECTED.TXT has Windows line endings).
cp EXPECTED.TXT EXPECTED-UNIX.TXT
if command -v dos2unix >/dev/null 2>&1; then
    dos2unix ACTUAL.TXT EXPECTED-UNIX.TXT >/dev/null 2>&1
fi

diff ACTUAL.TXT EXPECTED-UNIX.TXT
if [ $? -eq 0 ]; then
    echo "Test passed!"
    exit 0
else
    echo "Test failed!"
    exit 1
fi
