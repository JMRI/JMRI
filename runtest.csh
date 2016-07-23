#! /bin/sh
#
# Script to start a JMRI class directly, e.g during development
#
# First argument is a fully-qualified class name. Any standard JMRI POSIX
# launcher options may precede the first argument. Run this script with the
# --help option for details.
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
# By default this script sets the JMRI settings: dir to the directory "temp" in
# the directory its run from. Pass the option --settingsdir="" to use the JMRI
# default location. local.conf in the directory this script is run from is
# copied to jmri.conf in the settings: dir if the settings: dir is not "" and
# local.conf is newer than jmri.conf.
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
ant run-sh 1>/dev/null
result=$?
if [ $result -ne 0 ] ; then
    echo "ant run-sh failed."
    exit $result
fi

# set the default settings dir to $( dirname 0 )/temp, but allow it to be
# overridden
settingsdir="$( dirname 0 )/temp"
found_settingsdir="no"
for opt in "$@"; do
    if [ "${found_settingsdir}" = "yes" ]; then
        # --settingsdir /path/to/... part 2
        settingsdir="$opt"
        break
    elif [ "$opt" = "--settingsdir" ]; then
        # --settingsdir /path/to/... part 1
        found_settingsdir="yes"
    elif [[ "$opt" =~ "--settingsdir=" ]]; then
        # --settingsdir=/path/to/...
        settingsdir="${opt#*=}"
        break;
    fi
done

# if settingsdir is not empty (using JMRI default), and local.conf is newer than
# jmri.conf, copy local.conf to jmri.conf
if [ ! -z "${settingsdir}" -a "$( dirname $0 )/local.conf" -nt "${settingsdir}/jmri.conf" ] ; then
    cp "$( dirname $0 )/local.conf" "${settingsdir}/jmri.conf"
fi

# if --settingsdir="" was passed, allow run.sh to use JMRI default, otherwise
# prepend the option token to ensure run.sh sets the settings dir correctly
if [ -n "$settingsdir" ] ; then
    settingsdir="--settingsdir=${settingsdir}"
fi

$( dirname $0 )/.run.sh "$settingsdir" $@
