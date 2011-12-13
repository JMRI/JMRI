#! /bin/csh -f
#
# Short script to check for files missing needed -kb cvs option

#alias doOne \(echo \!^ \; cd \!^\; cvs -q update -dA \)

alias doOne 'grep -r \.\!^/ . | grep Entries | grep -v /-kb/'

doOne wav
doOne gif
doOne jpg
doOne png
doOne jar
doOne dll
doOne so
doOne jnilib

