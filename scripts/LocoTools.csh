#! /bin/csh -f
#
# Short csh script to start LocoTools in java ($Revision: 1.6 $)
#
# Assumes that the program is being run from the distribution directory.
# Uncomment and change the following to match the JMRI
# install directory if you would like to run this script 
# without cd'ing into the install directory.
#   cd /usr/local/JMRI
#
# Use the following to eliminate warnings about meta keys
# xprop -root -remove _MOTIF_DEFAULT_BINDINGS

java -noverify -Djava.security.policy=lib/security.policy -Djava.rmi.server.codebase=file:java/classes/ \
     -cp .:classes:jmriplugins.jar:lib/jmriplugins.jar:jmri.jar:lib/log4j.jar:lib/collections.jar:lib/jh.jar:lib/crimson.jar:lib/jdom-jdk11.jar:lib/jython.jar \
     apps.LocoTools.LocoTools

