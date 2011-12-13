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
# Errors, warnings and failures are reported via email 
# to the jmri-builds@lists.sourceforge.net list in production use, 
# but for debugging they are left in the log with a message to the console.
#
#

# Make sure env variables are defined
setenv normal_destination
setenv error_destination

# Place to send email. If null, errors are not sent as email.
# Uncomment next line for production use
# setenv error_destination jmri-builds@lists.sourceforge.net
# setenv normal_destination jmri-builds@lists.sourceforge.net

# remove leftovers from last time
rm -f nightlybuildlog.txt
rm -f decoders.zip
# remove and replace Version.java so previous "ant mark" won't cause CVS conflicts
rm -f src/jmri/Version.java
cvs -q update -d src/jmri/Version.java

# start log
date > nightlybuildlog.txt

setenv CVS_RSH ssh

# Update to current CVS
if ( { (cd ..; cvs -q update -d >>& java/nightlybuildlog.txt) } ) then
# probably OK
else
  # CVS did not terminate OK
  if ( { (test "$error_destination") } ) then
    cat nightlybuildlog.txt | mail -s "Error in CVS checkout" ${error_destination}
  else
    echo Error in CVS checkout, see log in nightlybuildlog.txt
  endif
  exit 1
endif

# Do clean build
if ( { ((ant init clean && ant init tests) >>& nightlybuildlog.txt) } ) then
# Probably OK
else
  # Ant did not terminate OK
  if ( { (test "$error_destination") } ) then
    cat nightlybuildlog.txt | mail -s "Did not build successfully" ${error_destination}
  else
    echo Did not build successfully, see log in nightlybuildlog.txt
  endif
  exit 2
endif

# Run tests
if ( { (./runtest.csh jmri.HeadLessTest >>& nightlybuildlog.txt) } ) then
# Probably OK
else
  # Tests did not run and terminate OK
  if ( { (test "$error_destination") } ) then
    cat nightlybuildlog.txt | mail -s "Tests did not run successfully" ${error_destination}
  else
    echo Did not run successfully, see log in nightlybuildlog.txt
  endif
  exit 3
endif

# Check the log for error messages (searches cvs, build log too, but those shouldn't trip the comparison
if ( { grep ERROR nightlybuildlog.txt >/dev/null || grep 'WARN ' nightlybuildlog.txt >/dev/null } ) then
  # Errors found, mail the log
  if ( { (test "$error_destination") } ) then
    cat nightlybuildlog.txt | mail -s "Errors found in test log" ${error_destination}
  else
    echo Errors found in test log, see nightlybuildlog.txt
  endif
  exit 4
endif

# Test javadoc build while at it
if ( { (((ant javadoc) |& grep -v "Loading source files for package") >>& nightlybuildlog.txt) } ) then
# Probably OK
else
  # Ant did not terminate OK
  if ( { (test "$error_destination") } ) then
    cat nightlybuildlog.txt | mail -s "Javadoc did not build successfully" ${error_destination}
  else
    echo Javadoc did not build successfully, see log in nightlyjavadoclog.txt
  endif
  exit 5
endif
# Check the log for javadoc error messages (searches cvs, build log too, but those shouldn't trip the comparison
if ( { grep '\[javadoc\].*: warning -' nightlybuildlog.txt >/dev/null } ) then
  # Errors found, mail the log
  if ( { (test "$error_destination") } ) then
    cat nightlybuildlog.txt | mail -s "Javadoc errors found" ${error_destination}
  else
    echo Javadoc errors found, see nightlybuildlog.txt
  endif
  exit 6
endif

# Success!

# put warnings in the log
ant warnings >>& nightlybuildlog.txt

# build and upload the jar file and decoder zip file
echo Start jar build and upload >>& nightlybuildlog.txt
rm -f ../jmri.jar
ant mark >>& nightlybuildlog.txt
ant jar >>& nightlybuildlog.txt
scp ../jmri.jar jacobsen,jmri@web.sourceforge.net:htdocs/ >>& nightlybuildlog.txt
ant zip >>& nightlybuildlog.txt
scp decoders.zip jacobsen,jmri@web.sourceforge.net:htdocs/ >>& nightlybuildlog.txt
echo "ls -l htdocs/decoders.zip" | sftp jacobsen,jmri@web.sourceforge.net >>& nightlybuildlog.txt

# and notify of success
if ( { (test "$normal_destination") } ) then
  cat nightlybuildlog.txt | mail -s "Build completed OK" ${normal_destination}
else
  echo Build completed OK
endif

exit 0
