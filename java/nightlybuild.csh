#! /bin/tcsh -f
#
# Short script to run the nightly build
#
# You can run this directly from the command line in the 
# development (java) directory if you want to check in advance
# that your next commit won't "break the build"
#
# Tasks:
#   Update to HEAD from CVS
#   Compile all from scratch using the clean/init/tests targets
#   Run JUnit tests in jmri.HeadLessTest
#
# Errors, warnings and failures are reported via email if the -mail
# option is present as the 1st argument. Absent that, the
# errors are reported to stderr
#
# TODO: Get the log mail vs log display working; right now, the log
#       file is just left behind.
#

rm -f nightlybuildlog.txt

if (`( cvs -q update -d && ant clean && ant init tests && ./runtest.csh jmri.HeadLessTest) >& nightlybuildlog.txt`) then
# probably OK
echo OK
else
# did not terminate OK, mail the log
echo Did not run successfully
exit
endif

# check the log for error messages (searches cvs, build log too, but those shouldn't trip the comparison
if (`grep ERROR nightlybuildlog.txt >/dev/null || grep WARN nightlybuildlog.txt >/dev/null`) then
# errors found, mail the log
echo Errors found in log
endif
