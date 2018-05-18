#! /bin/bash
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
# This works by calling .run.sh, which is generated from the JMRI POSIX launcher
# by running 'ant run-sh'
#
# By default this script sets the JMRI settings: dir to the directory "temp" in
# the directory its run from. Pass the option --settingsdir="" to use the JMRI
# default location. test/jmri.conf in the directory this script is run from is
# used to pull any persistent settings unless --settingsdir=... is specified.
#
# Note that this script may mangle arguments with unescaped spaces. This can be
# avoided by writing spaces in arguments as "arg\ with\ spaces" or by wrapping
# arguments with spaces in extra, escaped quotes like "\"arg with spaces\"".
#
# If you need to add any additional Java options or defines,
# include them in the JMRI_OPTIONS environment variable
#
#  jmri.demo                Keep some test windows open after tests run
#  java.awt.headless        Tests won't attempt to use screen
#  jmri.skipschematests     Skip tests of XML schema if true
#  jmri.skipscripttests     Skip tests of Jython scripts if true
#  jmri.log4jconfigfilename Specify replacement for details tests.lcf file (tests only)
#
# E.g.: 
# setenv JMRI_OPTIONS -Djmri.skipschematests=true
#
# You can run separate instances of the program with their
# own preferences and setup if you provide the name of a configuration file 
# as the 1st argument
#
# If you are getting X11 warnings about meta keys, uncomment the next line
# xprop -root -remove _MOTIF_DEFAULT_BINDINGS
#
# For more information, please see
# http://jmri.org/help/en/html/doc/Technical/StartUpScripts.shtml

dirname="$( dirname $0 )"
# assume ant is in the path, and silence output
if [ "${dirname}/scripts/AppScriptTemplate" -nt "${dirname}/.run.sh" ] ; then
    ant run-sh
    result=$?
    if [ $result -ne 0 ] ; then
        exit $result
    fi
fi

# set the default settings dir to ${dirname}/temp, but allow it to be
# overridden
settingsdir="${dirname}/test"
prefsdir="${dirname}/temp"
found_settingsdir="no"
for opt in "$@"; do
    if [ "${found_settingsdir}" = "yes" ]; then
        # --settingsdir /path/to/... part 2
        settingsdir="${opt}"
        prefsdir="${settingsdir}"
        break
    elif [ "${opt}" = "--settingsdir" ]; then
        # --settingsdir /path/to/... part 1
        found_settingsdir="yes"
    elif [[ "${opt}" =~ "--settingsdir=" ]]; then
        # --settingsdir=/path/to/...
        settingsdir="${opt#*=}"
        prefsdir="${settingsdir}"
        break;
    fi
done

# if --settingsdir="" was passed, allow run.sh to use JMRI default, otherwise
# prepend the option token to ensure run.sh sets the settings dir correctly
if [ -n "${settingsdir}" ] ; then
    settingsdir="--settingsdir=${settingsdir}"
fi
if [ -n "${prefsdir}" ] ; then
    prefsdir="-Djmri.prefsdir=${prefsdir}"
fi

# tests are no longer with production classes, so append the directory containing
# tests to the classpaths
testclasspath="--cp:a=${dirname}/target/test-classes"

"${dirname}/.run.sh" "${settingsdir}" "${prefsdir}" "${testclasspath}" $@
