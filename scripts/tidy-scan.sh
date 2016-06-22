#! /bin/bash
# scan the English help files for HMTL errors, used in Jenkins

find help/en/*/ -name \*html -exec echo Filename: {} \; -exec tidy -e -access 0 {} \; 2>&1 | tr '<' '&lt;' | tr '>' '&gt;' | awk -f scripts/tidy.awk
