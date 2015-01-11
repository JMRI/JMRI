# This is an example script for invoking an AppleScript from
# JMRI on Mac OS X.
#
# Author: Bob Jacobsen, Copyright 2008
# Part of the JMRI distribution
#
# Adapted from <http://www.oreilly.com/pub/a/mac/2003/02/25/apple_scripting.html>
#
# The next line is maintained by CVS, please don't change it
# $Revision$
#
#

# import the AppleScript utility classes
import com.apple.cocoa.application.NSApplication
import com.apple.cocoa.foundation.NSAppleScript
import com.apple.cocoa.foundation.NSMutableDictionary

# Our sample AppleScript tells the Finder to make a new folder called
# "untitled folder" on the Desktop.  Yes, that's not an important thing
# to do, but it's a good example.

script = \
"tell application \"Finder\"\n"+ \
"  make new folder at desktop\n"+ \
"end tell\n"

# Create an NSAppleScript object to execute our script
myScript = com.apple.cocoa.foundation.NSAppleScript(script);

# Create a dictionary to hold any errors that are
# encountered during script execution
errors = com.apple.cocoa.foundation.NSMutableDictionary()

# Execute the script!
myScript.execute(errors)

