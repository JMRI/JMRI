#! /bin/csh
###############################################################################
#
# Invoke the the PacketPro JMRI appliction, which provide a Jython
# command line.
#
# There must be a local jython interpreter and JMRI libraries available
#
# $Revision: 1.2 $ (CVS maintains this line, do not edit please)
#
setenv CLASSPATH .:classes:jmriplugins.jar:lib/jmriplugins.jar:jmri.jar:lib/log4j.jar:lib/collections.jar:lib/jh.jar:lib/crimson.jar:lib/jdom-jdk11.jar
     
/usr/bin/env jython -i $argv jython/PacketPro.py

