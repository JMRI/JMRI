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
# This works by calling run.sh, which is generated from the JMRI POSIX launcher
# by running 'ant run-sh'
#
# Note that this script may mangle arguments with unescaped spaces. This can be
# avoided by writing spaces in arguments as "\ ".
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

# assume ant is in the path, and silence output
ant run-sh 2>/dev/null
result=$?
if [ $result -ne 0 ] ; then
    echo "ant run-sh failed. please run independently to determine why."
    exit $result
fi

$( dirname $0 )/run.sh $@
