#! /bin/bash
# scan the English help files for HMTL errors, used in Jenkins

find help/*/*/ -name \*html -exec echo Filename: {} \; -exec tidy -e -access 0 {} \; 2>&1 | grep -v '<table> lacks "summary" attribute' | grep -v '<img> lacks "alt" attribute' | tr '<' '&lt;' | tr '>' '&gt;' | awk -f scripts/tidy.awk
