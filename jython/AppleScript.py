# Invoke an AppleScript from
# JMRI on Mac OS X.
#
# Author: Bob Jacobsen, Copyright 2008, 2016
# Part of the JMRI distribution
#
# Note: Modern JMRI versions can directly run Applescript, without
# having to start with the Python interpreter
#

import jmri

from subprocess import Popen, PIPE

# define a method for running an Applescript 
def run_this_scpt(scpt, args=[]):
     p = Popen(['osascript', '-'] + args, stdin=PIPE, stdout=PIPE, stderr=PIPE)
     stdout, stderr = p.communicate(scpt)
     return stdout

# a sample - note that a lot of quoting \ characters are needed to get lines right
script = \
"tell application \"Finder\"\n"+ \
"  make new folder at desktop\n"+ \
"end tell\n"

# Execute the sample
run_this_scpt(script)

