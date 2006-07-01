#!/bin/csh -f
#
#  short csh script to start DecoderPro in java ($Revision: 1.7 $)
#
#  Assumes that the program is being run from the distribution directory
#

# Use the following to eliminate warnings about meta keys
# xprop -root -remove _MOTIF_DEFAULT_BINDINGS

java -noverify -Djava.security.policy=lib/security.policy -Djava.rmi.server.codebase=file:java/classes/ -cp .:classes:jmri.jar:lib/log4j.jar:lib/collections.jar:lib/crimson.jar:lib/jdom-jdk11.jar:lib/jython.jar apps.DecoderPro.DecoderPro


