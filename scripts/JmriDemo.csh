#! /bin/csh -f
#
#  short csh script to start JMRIdemo in java
#
#  Assumes that the program is being run from the distribution directory
#
#  In this version, the .jar file for the javax.comm package is not included.
#  See http://www.interstice.com/~kevinh/linuxcomm.html for information
#  on creating and installing a version based on RXMX

# add the local jar files to the CLASSPATH
# In case Swing appears in the existing CLASSPATH, we add our 
# local Swing version afterwards

if ( "${?CLASSPATH}" == "0" ) then
  setenv CLASSPATH
endif

setenv CLASSPATH .:jmri.jar:lib/log4j.jar:lib/collections.jar:lib/crimson.jar:lib/jdom-jdk11.jar:${CLASSPATH}:lib/swingall.jar

# some installations have multiple java levels installed.  If that's the case, they
# use JAVAVER to specify one.  

setenv JAVAVER 1.3.1

# start the program

java JMRIdemo

