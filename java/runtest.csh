#! /bin/csh -f
#
# Short csh script to setup and run a JUnit test ($Revision: 1.4 $)
#
# Assumes that the program is being run from the "java" build directory.
# Do "ant tests" first to build the necessary classes
#

# first, move up to distribution directory to find xml, resources, etc
cd ..

# run the command
java -noverify -Djava.security.policy=lib/security.policy -Djava.rmi.server.codebase=file:java/classes/ -Dsun.java2d.noddraw \
     -cp .:java/classes:jmriplugins.jar:java/lib/junit.jar:lib/jmriplugins.jar:jmri.jar:lib/log4j.jar:lib/jhall.jar:lib/crimson.jar:lib/jdom.jar:lib/jython.jar:lib/javacsv.jar:lib/MRJAdapter.jar:lib/jakarta-regexp-1.5.jar:lib/vecmath.jar \
     $1

