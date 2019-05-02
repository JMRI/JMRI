# Drive a turnouts from two CMRI connections.
#
# Some CMRI users connect slow-motion switch machines between
# two CMRI outputs, and then set the outputs to complementary 
# states (one active, one inactive) to drive the machine in
# the two directions.
#
# This script slaves a 2nd turnout to a first one, so that when the
# first one is commanded to a particular state, the second one
# goes to the other state
#
# To use this, edit the script to include your turnouts at the bottom
# (see the existing example; you should remove that)
# Then select it as a script to be run at startup from the "Advanced
# Preferences" on the preferences window
#
# Author: Bob Jacobsen, copyright 2005
# Part of the JMRI distribution
#

import jmri
import java
import java.beans

# First, define the listener that does everything
class MainTurnoutListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    if (self.master.state == CLOSED) :
        self.slave.state = THROWN
    elif (self.master.state == THROWN) :
        self.slave.state = CLOSED

# Define a routine to make it easy to attach listeners
def SetTwoOutputTurnout(master, slave) :
    # get turnout objects
    t1 = turnouts.provideTurnout(master)
    t2 = turnouts.provideTurnout(slave)
    # initialize state
    if (t1.state == CLOSED) :
        t2.state = THROWN
    elif (t1.state == THROWN) :
        t2.state = CLOSED
    # connect up listener for future changes
    listener = MainTurnoutListener()
    listener.master = t1
    listener.slave = t2
    t1.addPropertyChangeListener(listener)

# Attach listeners to the desired the turnouts
# In this sample, there are two pairs; replace with your own lines
SetTwoOutputTurnout("CT13", "CT14")
SetTwoOutputTurnout("CT15", "CT16")

 
