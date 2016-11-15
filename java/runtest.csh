#! /bin/sh
#
# Script to start a JMRI class directly, e.g during development
#
# Calls the script by the same name in the parent directory. See that script
# for further documentation.

cd ..
 ./runtest.csh $@
