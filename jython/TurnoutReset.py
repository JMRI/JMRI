# Reset a turnout to Closed every time it's clicked Thrown
#
# This might be used so that a turnout icon on a panel sits 
# in one position, ready to be clicked and fire a route
#
# The top of the file defines the needed code.  There are some
# lines near the bottom you should edit to adapt it to your 
# particular layout.
#
# Author: Bob Jacobsen, copyright 2005
# Part of the JMRI distribution
#

import jmri
import java
import java.beans

# First, define the listener.  
class MyListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    if ( event.propertyName == "KnownState" ) :
        if ( event.newValue == THROWN ) :
            turnouts.provideTurnout(event.source.systemName).setState(CLOSED)

# Define a routine to make it easy to attach listeners
def SetTurnoutToReset(name) :
    t = turnouts.provideTurnout(name)
    t.setState(CLOSED)
    t.addPropertyChangeListener(MyListener())

# Attach listeners to the desired turnouts
# (Edit the following to apply to your layout)
SetTurnoutToReset("14")
SetTurnoutToReset("15")

 
