# Has a Virtual SignalHead for a JMRI Panel
# follow LocoNet messages for an SE8c signal
#
# Use this when you have something else
# controlling the SE8c, and you want to 
# display it on a panel.
#
# Note that this can't be perfect. It 
# doesn't know about Held, for example,
# nor does this implementation handle
# flashing appearances.
#
# See the example and test case in
# jython/test/FollowSE8cTest.py
#
# Author: Bob Jacobsen, copyright 2016
# Part of the JMRI distribution

import jmri
import java
import java.beans

class FollowSE8c(java.beans.PropertyChangeListener):
  def set(self, signalHeadName, lowTurnoutName, highTurnoutName) :
    self.signal = signals.getSignalHead(signalHeadName)
    self.lowTO = turnouts.provideTurnout(lowTurnoutName)
    self.lowTO.addPropertyChangeListener(self)
    self.highTO = turnouts.provideTurnout(highTurnoutName)
    self.highTO.addPropertyChangeListener(self)
    return
  def propertyChange(self, event):
    if (event.source == self.lowTO) : 
       if (event.newValue == CLOSED) :
          self.signal.setAppearance(GREEN)
       if (event.newValue == THROWN) :
          self.signal.setAppearance(RED)
    if (event.source == self.highTO) : 
       if (event.newValue == CLOSED) :
          self.signal.setAppearance(DARK)
       if (event.newValue == THROWN) :
          self.signal.setAppearance(YELLOW)
    return

# Example of use - add a line like the following 
# that includes system or user names for the signal head
# and the two turnouts for the SE8c addresses
# FollowSE8c().set("CH2001","LT301", "LT302")
 
