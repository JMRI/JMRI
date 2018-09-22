# example of an event listener for a JMRI class, in this case
# a Turnout.
#
# Author: Bob Jacobsen, copyright 2004
# Part of the JMRI distribution

import java
import java.beans
import jmri

# First, define the listener.  This one just prints some
# information on the change, but more complicated code is
# of course possible.
class MyListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    print "change",event.propertyName
    print "from", event.oldValue, "to", event.newValue
    print "source systemName", event.source.systemName
    print "source userName", event.source.userName

# Second, attach that listener to a particular turnout. The variable m
# is used to remember the listener so we can remove it later
t = turnouts.provideTurnout("12")
m = MyListener()
t.addPropertyChangeListener(m)

# This could have been done on one line if we weren't going
# to remove the listener later:
# turnouts.provideTurnout("12").addPropertyChangeListener(MyListener())

# Exercise it to show what happens. Note that
# the call-backs can be asynchronous, depending on 
# the layout hardware, so might appear later
print "Set the turnout to THROWN"
t.commandedState = THROWN

print "Set the turnout to CLOSED"
t.commandedState = CLOSED

print "Finished setting the turnout"

# remove the listener
t.removePropertyChangeListener(m)

 
