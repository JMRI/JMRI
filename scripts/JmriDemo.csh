#! /bin/csh -f
#
#  short csh script to start JMRIdemo in java ($Revision: 1.8 $)
#
#  Assumes that the program is being run from the distribution directory
#
#  In this version, the .jar file for the javax.comm package is not included.
#  See http://www.interstice.com/~kevinh/linuxcomm.html for information
#  on creating and installing a version based on RXMX

# Use the following to eliminate warnings about meta keys
# xprop -root -remove _MOTIF_DEFAULT_BINDINGS

#
# Change the following to match the JMRI install directly if you would
# like to run this script without cd'ing into the install directory.
cd /usr/local/JMRI

java -noverify -Djava.security.policy=lib/security.policy -Djava.rmi.server.codebase=file:java/classes/ -cp .:classes:jmri.jar:lib/log4j.jar:lib/collections.jar:lib/crimson.jar:lib/jdom-jdk11.jar:lib/jython.jar apps.JmriDemo.JMRIdemo

