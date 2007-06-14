#! /bin/tcsh -f
#
# Short script to run the nightly build
#
# Tasks:
#   Update to HEAD from CVS
#   Compile all from scratch using the clean/init/tests targets
#   Run JUnit tests in jmri.HeadLessTest
#
# Errors, warnings and failures are reported via email
#

rm -f nightlybuildlog.txt

if (`( cvs -q update -d && ant clean && ant init tests && ./runtest.csh jmri.HeadLessTest) >& nightlybuildlog.txt`) then
# probably OK
echo OK
else
# not OK, mail the log
echo mail log build
exit
endif

# check the log for error messages (searches cvs, build log too, but those shouldn't trip this
if (`grep ERROR nightlybuildlog.txt >/dev/null || grep WARN nightlybuildlog.txt >/dev/null`) then
#errors found, mail the log
echo mail log search
endif
