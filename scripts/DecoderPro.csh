#!/bin/csh -f
#
#  short csh script to start DecoderPro in java
#
#  Assumes that the program is being run from the distribution directory
#
#  In this version, the .jar file for the javax.comm package is not included.
#  See http://www.interstice.com/~kevinh/linuxcomm.html for information
#  on creating and installing a version based on RXMX

# Use the following to eliminate warnings about meta keys
# xprop -root -remove _MOTIF_DEFAULT_BINDINGS

java -cp .:jmri.jar:lib/log4j.jar:lib/collections.jar:lib/crimson.jar:lib/jdom-jdk11.jar apps.DecoderPro.DecoderPro


