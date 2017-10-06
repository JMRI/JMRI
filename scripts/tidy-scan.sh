#! /bin/bash
# scan the help files for HMTL errors, used in Jenkins
# if an argument is provided, i.e. "help/en/*/" scan there, otherwise scan all the help

if [[ "$@" == "" ]] ; then
    WHERE=help/*/*/
else
    WHERE=$@
fi

find ${WHERE} -name \*html -exec echo Filename: {} \; -exec tidy -e -access 0 {} \; 2>&1 | grep -v '<table> lacks "summary" attribute' | grep -v '<img> lacks "alt" attribute' | tr '<' '&lt;' | tr '>' '&gt;' | awk -f scripts/tidy.awk
