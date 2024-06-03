#!/bin/bash

# Test if there are any stale java source files

# Compile first once to ensure we have compiled everything
mvn_output=$(mvn -X test-compile)

# Compile again to find stale sources
mvn_output=$(mvn -X test-compile)
if [[ $? != 0 ]]; then
    echo "mvn command failed."
    exit 1
fi

output=$(grep "Stale source detected:" <<< {$mvn_output})
if [[ $? == 0 ]]; then
    # If here, we have stale java sources.
    # Print the output and return with exit code 1.
    echo "Error: Stale Java sources found."
    echo "A stale Java source is a java file that doesn't compiles into a class file."
    echo ""
    echo "For Java files that doesn't have any code, for example package-info.java, add these two lines:"
    echo "// include empty DefaultAnnotation to avoid excessive recompilation"
    echo "@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})"
    echo ""
    echo $output
    echo ""
    exit 1
else
    # If here, no stale java sources was found. Exit with 0.
    echo ""
    echo "No stale sources found"
    echo ""
    exit 0
fi
