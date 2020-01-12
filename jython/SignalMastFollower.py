# Has one SignalMast follow the state of another/
# Use this is you have e.g. a "repeater" signal mast
# on the fascia that shows the appearance of a mast
# on the layout
#
# Author: Bob Jacobsen, copyright 2016
# Part of the JMRI distribution

import jmri
import java
import java.beans

# Define the listener. 
class SignalMastFollowerListener(java.beans.PropertyChangeListener):
  def set(self, inputName, outputName) :
    self.inSignal = masts.getSignalMast(inputName)
    self.outSignal = masts.getSignalMast(outputName)
    self.inSignal.addPropertyChangeListener(self)
    return
  def propertyChange(self, event):
    self.outSignal.setAspect(self.inSignal.getAspect())
    return

# Example of use - add a line like the following 
# that includes system or user names for the signal mast to be repeated,
# and the signal mast to repeat it onto. These must already exist.
# SignalMastFollowerListener().set("Mast 1","Mast 1 R")
 
