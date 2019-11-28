#! /bin/bash
# scan the help files for HMTL errors, used in Jenkins as "tidy-scan.sh help/en/*/*"
# formats output for Jenkins presentation
# if an argument is provided, i.e. "help/en/*/*" scan there, otherwise scan all the help

if [[ "$@" == "" ]] ; then
    WHERE=help/*/*
else
    WHERE=$@
fi

# first, scan for whether there's an issue (omitting known fragment files)
find ${WHERE}  -name \*html ! -path 'help/en/releasenotes/*' ! -name Sidebar.shtml -exec echo Filename: {} \; -exec tidy -e -access 0 {} \; 2>&1 | grep -v '<table> lacks "summary" attribute' | grep -v '<img> lacks "alt" attribute' | tr '<' '&lt;' | tr '>' '&gt;' | awk -f scripts/tidy.awk
