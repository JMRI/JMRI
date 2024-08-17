#!/bin/bash

# Test if there are any files with with multiple UTF-8 Byte-Order-Marks (BOM)
# or TABs

# Check all files for multiple BOMs



IFS=$'\n'

found=0

for x in `find -P -type f `; do

    if [[ "$x" == "./.git/"* ]]; then
        continue
    fi

#    echo "the next file is $x"

    xxd_output=$(xxd -g 0 -l 6 -p "$x")
    if [[ "$xxd_output" == "efbbbfefbbef" ]]; then
        echo "File $x has two UTF-8 Byte-Order-Marks"
        found=1
    fi

done

# If $found, we have at least on file with two BOMs
if [[ $found == 1 ]]; then
    echo "One or more files with two UTF-8 Byte-Order-Marks found"
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
fi



# If here, everything is OK
echo ""
echo "No UTF-8 Byte-Order-Marks or TABs found"
echo ""
exit 0
