Short Detector
==============

Demonstration web applet that displays a notification if a sensor attached to a
short detector goes active.

# Configuration
1. Create a Memory object in JMRI with the system name _IMSHORTDETECTION_ and
   set its value to a regular expression that is used to scan the user names of
   sensors for a sensor used for short detection. The simplest regular
   expression is a literal match, such as "Short Detection".
2. Create sensors associated with the short detection hardware.
   a. Set the user name of each sensor to match the regular expression set in
      step 1.
   b. Set the comment of each sensor to the text to display in the notification.
      __Note__ that each sensor must have a unique comment.
