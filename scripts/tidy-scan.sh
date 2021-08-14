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
# THree files are excluded since they have excess html (empty <ul></ul> pairs) due to the XSLT transformation.
find ${WHERE}  -name \*html -exec grep -q DOCTYPE {} \; -exec echo Filename: {} \; -exec tidy -e -access 0 {} \; 2>&1 | awk -f scripts/tidy.awk | egrep -v "local/index.html|webindex.shtml|webtoc.shtml"
