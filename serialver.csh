#! /bin/sh
#
# Script to run the serialver tool on a JMRI class
#
# First argument is fully=qualified class name.
#
# Made from the runtest.csh script, January 2010
#
# If you need to add any additional Java options or defines,
# include them in the JMRI_OPTIONS environment variable
#
# You can run separate instances of the program with their
# own preferences and setup if you either
# *) Provide the name of a configuration file as the 1st argument
# or
# *) Copy and rename this script.
#
# If you rename the script to e.g. JmriNew, it will use "JmriNewConfig2.xml"
# as it's configuration file.  You can then set and save the preferences for
# the next time you rerun the script.
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
CP=".:classes:target/classes"
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
OPTIONS=" ${JMRI_OPTIONS} "

# Format to add more options:
#OPTIONS="${OPTIONS} -Djava.library.path=.:lib:$SYSLIBPATH"

[ "${DEBUG}" ] && echo "OPTIONS: '${OPTIONS}'"

[ "${DEBUG}" ] && echo java ${OPTIONS} -cp "${CP}" "${CLASSNAME}" ${CONFIGFILE}
serialver ${OPTIONS} -classpath "${CP}" "${CLASSNAME}" $2 $3



