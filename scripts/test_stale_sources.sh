#!/bin/bash

# Test if there are any stale java source files

output=$(mvn -X test-compile | grep "Stale source detected:")
if [[ $? != 0 ]]; then
    echo "mvn command failed."
elif [[ $output ]]; then
    // If here, we have stale java sources.
    // Print the output and return with exit code 1.
    echo $output
    exit 1
else
    // If here, no stale java sources was found. Exit with 0.
    exit 0
fi
