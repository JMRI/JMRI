#! /bin/bash
# scan the English help files for HMTL errors, used in Jenkins

find help/en/ -name \*html -exec echo Filename: {} \; -exec tidy -e {} \; 2>&1 | awk -f scripts/tidy.awk
