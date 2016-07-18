#! /bin/sh
#
# Script to start a JMRI class directly, e.g during development
#
# First argument is fully=qualified class name.
#
# If the class has a main() method, that's launched.  This works
# for running e.g. early-JUnit test classes, and applications
# like apps.DecoderPro.DecoderPro
#
# If there is no main() method found in the named class, an
# org.junit.runner.JUnitCore runner is asked to try to run the
# class as JUnit4 tests. 
# 
# All this is done by using a script similar to (originally
# identical to) the JMRI POSIX launcher, which invokes a
# bespoke jmri.util.junit.TestClassMainMethod class.
#
# Note that, unlike running JUnit tests from Ant, 
# this currently doesn't set the jmri.prefsdir system property
# to "temp". Running tests in Ant does that, which causes
# them to use a custom preferences directory.  You can
# do that manually via (or similar)
#  setenv JMRI_OPTIONS "-Djmri.prefsdir=${PWD}/temp ${JMRI_OPTIONS}"
#
# Made from the DecoderPro script, January 2010
#
# If you need to add any additional Java options or defines,
# include them in the JMRI_OPTIONS environment variable
#
#  jmri.demo             Keep some test windows open after tests run
#  jmri.headlesstest     Tests won't attempt to use screen
#  jmri.skipschematests  Skip tests of XML schema if true
#  jmri.skipscripttests  Skip tests of Jython scripts if true
#
# E.g.: 
# setenv JMRI_OPTIONS -Djmri.skipschematests=true
#
# If your serial ports are not shown in the initial list, you 
# can include them in the environment variable JMRI_SERIAL_PORTS
# separated by commas:
#    export JMRI_SERIAL_PORTS="/dev/locobuffer,/dev/cmri"
#
# You can run separate instances of the program with their
# own preferences and setup if you provide the name of a configuration file 
# as the 1st argument
#
# If you are getting X11 warnings about meta keys, uncomment the next line
# xprop -root -remove _MOTIF_DEFAULT_BINDINGS
#
# For more information, please see
# http://jmri.org/install/ShellScripts.shtml
#

SYSLIBPATH=

if [ -z "$OS" ]
then
    # start finding the architecture specific library directories
    OS=`uname -s`

    # normalize to match our standard names
    if [ "$OS" = "Linux" ]
    then
      OS="linux"
    fi

    if [ "$OS" = "Darwin" ]
    then
      OS="macosx"
    fi
fi

if [ -d "lib/$OS" ]
then
  SYSLIBPATH="lib/$OS"
fi

# one or another of these commands should return a useful value, except that sometimes
# it is spelled funny (e,g, amd64, not x86_64).  

if [ -z "$ARCH" ]
then
    for cmd in "arch" "uname -i" "uname -p"
    do
      ARCH=`$cmd 2>/dev/null`
      if [ -n "$ARCH" ]
      then
	    if [ "$ARCH" = "amd64" ]
	    then
	      ARCH="x86_64"
	    fi

	    if [ "$ARCH" = "i686" ]
	    then
	      ARCH="i386"
	    fi

	    if [ -d "lib/$OS/$ARCH" ]
	    then
	       SYSLIBPATH="lib/$OS/$ARCH:$SYSLIBPATH"

	       # we're only interested in ONE of these values, so as soon as we find a supported
	       # architecture directory, continue processing and start up the program
	       break
	    fi
      fi
    done
fi


# define the class to be invoked
CLASSNAME=$1

# set DEBUG to anything to see debugging output
# DEBUG=yes

# if JMRI_HOME is defined, go there, else
# change directory to where the script is located
if [ "${JMRI_HOME}" ]
then
    cd "${JMRI_HOME}"
else 
    cd `dirname $0`
fi
[ "${DEBUG}" ] && echo "PWD: '${PWD}'"

# build classpath dynamically
CP=".:classes:java/classes"
# list of jar files in home, not counting jmri.jar
LOCALJARFILES=`ls *.jar | grep -v jmri.jar | tr "\n" ":"`
if [ ${LOCALJARFILES} ]
then 
  CP="${CP}:${LOCALJARFILES}"
fi
# add jmri.jar
CP="${CP}:jmri.jar"
# and contents of lib
CP="${CP}:`ls -m lib/*.jar | tr -d ' \n' | tr ',' ':'`"
# add a stand-in for ${ant.home}/lib/ant.jar
CP="${CP}:/usr/share/ant/lib/ant.jar"

[ "${DEBUG}" ] && echo "CLASSPATH: '${CP}'"

# create the option string
#
# Add JVM and RMI options to user options, if any
OPTIONS="${JMRI_OPTIONS} -noverify"
OPTIONS="${OPTIONS} -Djava.security.policy=lib/security.policy"
OPTIONS="${OPTIONS} -Djava.rmi.server.codebase=file:java/classes/"
OPTIONS="${OPTIONS} -Djava.library.path=.:lib:$SYSLIBPATH"
# ddraw is disabled to get around Swing performance problems in Java 1.5.0
OPTIONS="${OPTIONS} -Dsun.java2d.noddraw"
# memory start and max limits
OPTIONS="${OPTIONS} -Xms30m"
OPTIONS="${OPTIONS} -Xmx640m"

# RXTX options (only works in some versions)
OPTIONS="${OPTIONS} -Dgnu.io.rxtx.NoVersionOutput=true"
[ "${DEBUG}" ] && echo "OPTIONS: '${OPTIONS}'"

if [ -n "$JMRI_SERIAL_PORTS" ]
then
  JMRI_SERIAL_PORTS="$JMRI_SERIAL_PORTS,"
fi

# locate alternate serial ports
ALTPORTS=`(echo $JMRI_SERIAL_PORTS; ls -fm /dev/ttyUSB* /dev/ttyACM* 2>/dev/null ) | tr -d " \n" | tr "," ":"`
if [ "${ALTPORTS}" ]
then
  ALTPORTS=-Dgnu.io.rxtx.SerialPorts=${ALTPORTS}
fi
[ "${DEBUG}" ] && echo "ALTPORTS: '${ALTPORTS}'"

[ "${DEBUG}" ] && echo java ${OPTIONS} "${ALTPORTS}" -cp "${CP}" "${CLASSNAME}" ${CONFIGFILE}
java ${OPTIONS} ${ALTPORTS} -cp "${CP}" jmri.util.junit.TestClassMainMethod "${CLASSNAME}" $2 $3



