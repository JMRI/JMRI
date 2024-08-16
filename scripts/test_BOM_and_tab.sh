#!/bin/bash

# Test if there are any files with with multiple UTF-8 Byte-Order-Marks (BOM)
# or TABs

# Check all files for multiple BOMs
grep_output=$(grep -rlIP --exclude-dir=.git '^\xEF\xBB\xBF\xEF\xBB\xBF' .)
result=$?
if [[ $result == 2 ]]; then
    echo "grep failed."
    exit 1
fi

if [[ $result == 0 ]]; then
    echo "grep found multiple UTF-8 Byte-Order-Marks in these files:"
    echo $grep_output
    exit 1
fi


# Check for any scripts with tabs.
grep_output=$(grep -lrP '\t' jython/ | grep '\.py')
result=$?
if [[ $result == 2 ]]; then
    echo "grep failed."
    exit 1
fi

if [[ $result == 0 ]]; then
    echo "grep found TABs in these files:"
    echo $grep_output
    exit 1
else
    # If here, everything is OK
    echo ""
    echo "No UTF-8 Byte-Order-Marks or TABs found"
    echo ""
    exit 0
fi
