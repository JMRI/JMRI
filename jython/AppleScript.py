# Demonstration of invoking osascript (using AppleScript) from JMRI on Mac OS X.
#
# Author: Bob Jacobsen, Copyright 2008, 2016
# Part of the JMRI distribution

import jmri

# osascript is an external command, so we need to use Popen to call it and PIPE
# to get its input and output
from subprocess import Popen, PIPE

# define a method for running osascript
# takes two arguments:
#   a script (required)
#   an array of arguments to pass to the script (optional)
def osascript(scpt, args=[]):
    # create an osascript process
    p = Popen(['osascript', '-'] + args, stdin=PIPE, stdout=PIPE, stderr=PIPE)
    # execute the script
    stdout, stderr = p.communicate(scpt)
    # return its output
    return stdout

# sample - note extensive use of quoting and \ characters to get lines right
script = \
"tell application \"Finder\"\n" + \
"  make new folder at desktop\n" + \
"end tell\n"

# Execute the sample
osascript(script)

