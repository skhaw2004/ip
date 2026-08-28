#!/usr/bin/env bash

# Compiles Stuart and runs it (from the project root, so its "./data/..."
# save path resolves correctly) against input.txt, diffing both the console
# output (against EXPECTED.TXT) and the resulting save file at
# ../data/stuart.txt (against EXPECTED_DATA.TXT). Run from anywhere; paths
# are resolved relative to this script's location.

cd "$(dirname "$0")"

cd ..
mkdir -p out/production/ip
javac -d out/production/ip src/main/java/*.java
if [ $? -ne 0 ]; then
    echo "********** BUILD FAILURE **********"
    exit 1
fi

# Start each run with no save file, so it doesn't carry over between runs.
rm -rf data

# Run from the project root, matching how a normal user (or IntelliJ) would
# launch Stuart, so its hard-coded "./data/stuart.txt" path resolves here.
java -classpath out/production/ip Stuart < text-ui-test/input.txt > text-ui-test/ACTUAL.TXT

cd text-ui-test

# Normalize line endings (in case EXPECTED.TXT has Windows line endings).
cp EXPECTED.TXT EXPECTED-UNIX.TXT
if command -v dos2unix >/dev/null 2>&1; then
    dos2unix ACTUAL.TXT EXPECTED-UNIX.TXT >/dev/null 2>&1
fi

CONSOLE_OK=0
DATA_OK=0

diff ACTUAL.TXT EXPECTED-UNIX.TXT
if [ $? -eq 0 ]; then
    CONSOLE_OK=1
else
    echo "********** Console output does not match EXPECTED.TXT **********"
fi

diff ../data/stuart.txt EXPECTED_DATA.TXT
if [ $? -eq 0 ]; then
    DATA_OK=1
else
    echo "********** Saved data/stuart.txt does not match EXPECTED_DATA.TXT **********"
fi

if [ $CONSOLE_OK -eq 1 ] && [ $DATA_OK -eq 1 ]; then
    echo "Test passed!"
    exit 0
else
    echo "Test failed!"
    exit 1
fi
