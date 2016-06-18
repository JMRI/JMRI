#! /bin/bash
#
find help/en/ -name \*html -exec echo Filename: {} \; -exec tidy -e {} \; 2>&1 | awk -f scripts/tidy.awk
