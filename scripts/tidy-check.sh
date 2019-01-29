#! /bin/bash
# scan the help files for HMTL errors, used in 'ant html'
# if an argument is provided, i.e. "help/en/*/*" scan there, otherwise scan all the help

if [[ "$@" == "" ]] ; then
    WHERE=help/*/*
else
    WHERE=$@
fi

# first, scan for whether there's an issue (omitting known fragment files)
find ${WHERE} -name \*html ! -path 'help/en/releasenotes/*' ! -name Sidebar.shtml -exec echo Filename: {} \;  ! -exec tidy -eq -access 0 {} \; 2>&1 | grep -v '<table> lacks "summary" attribute' | grep -v '<img> lacks "alt" attribute' | awk -f scripts/tidy.awk | grep Warning 1>&2

# swap return code from grep
if [ $? -eq 0 ]; then
    exit 1
fi
if [ $? -eq 1 ]; then
    exit 0
fi
# leave other error codes as-is

