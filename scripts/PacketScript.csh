#!/bin/csh
###############################################################################
#
# Invoke the the PacketScript JMRI appliction, which provide a Jython
# command line.
#
# There must be alocal jython interpreter and JMRI libraries available
#
# $Revision: 1.1 $ (CVS maintains this line, do not edit please)
#
setenv CLASSPATH .:jmri.jar:lib/log4j.jar:lib/collections.jar:lib/crimson.jar:lib/jdom-jdk11.jar

/usr/bin/env jython -i $argv packetscript/PacketScript.py

