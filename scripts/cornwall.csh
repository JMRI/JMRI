#! /bin/csh -f
#
# Short csh script to start CornwallRR in java ($Revision$)
#
# Assumes that the program is being run from the distribution directory.
# Uncomment and change the following to match the JMRI
# install directory if you would like to run this script 
# without cd'ing into the install directory.
#   cd /usr/local/JMRI
#
# If you need to add any additional Java options or defines,
# include them in the JMRI_OPTIONS environment variable
#
# Use the following to eliminate warnings about meta keys
# xprop -root -remove _MOTIF_DEFAULT_BINDINGS

if ( ! $?JMRI_OPTIONS ) setenv JMRI_OPTIONS ""

java -noverify -Djava.security.policy=lib/security.policy -Djava.rmi.server.codebase=file:java/classes/ -Dsun.java2d.noddraw \
     -cp .:java/classes:jmriplugins.jar:lib/jmriplugins.jar:jmri.jar:lib/MRJAdapter.jar:lib/RXTXcomm.jar:lib/Serialio.jar:lib/ch.ntb.usb.jar:lib/comm-rxtx.jar:lib/comm.jar:lib/crimson.jar:lib/javacsv.jar:lib/jdom.jar:lib/jhall.jar:lib/jinput.jar:java/lib/junit.jar:lib/jython.jar:lib/log4j.jar:lib/servlet.jar:lib/vecmath.jar:lib/activation.jar:lib/mailapi.jar:lib/smtp.jar:lib/ExternalLinkContentViewerUI.jar:/System/Library/Java \
     $JMRI_OPTIONS \
     apps.cornwall.CornwallRR


