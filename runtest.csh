#! /bin/sh
#
# Script to test a JMRI class directly, e.g during development
#
# First argument is fully-qualified class name of ether the test or the
# tested class.
#
# If you need to add any additional Java options or defines,
# include them on the command line after the classname
#
#  jmri.demo             Keep some test windows open after tests run
#  jmri.headlesstest     Tests won't attempt to use screen
#  jmri.skipschematests  Skip tests of XML schema if true
#  jmri.skipscripttests  Skip tests of Jython scripts if true
#
# E.g.: 
# ./runtest.csh jmri.util.FileUtil -Djmri.skipschematests=true

# define the class to be invoked
CLASSNAME=$1

shift
ARGS=$@

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

ant test-single -Dtest.includes=${CLASSNAME} "${@}"
