#! /bin/bash
# scan the help files for HMTL errors, used in 'ant html'
# if an argument is provided, i.e. "help/en/*/*" scan there, otherwise scan all the help

if [[ "$@" == "" ]] ; then
    WHERE=help/*/*
else
    WHERE=$@
fi

# first, scan for whether there's an issue (omitting known fragment files)
# THree files are excluded since they have excess html (empty <ul></ul> pairs) due to the XSLT transformation.
find ${WHERE}  -name \*html -exec grep -q DOCTYPE {} \; -exec echo Filename: {} \; -exec tidy -e -access 0 {} \; 2>&1 | awk -f scripts/tidy.awk | egrep -v "local/index.html|webindex.shtml|webtoc.shtml"

# swap return code from grep
if [ $? -eq 0 ]; then
    exit 1
fi
if [ $? -eq 1 ]; then
    exit 0
fi
# leave other error codes as-is
