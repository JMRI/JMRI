# Has one SignalHead follow the state of another/
# Use this is you have e.g. a "repeater" signal head
# on the fascia that shows the appearance of a head
# on the layout
#
# Author: Bob Jacobsen, copyright 2016
# Part of the JMRI distribution

import jmri
import java
import java.beans

# Define the listener. 
class SignalFollowerListener(java.beans.PropertyChangeListener):
  def set(self, inputName, outputName) :
    self.inSignal = signals.getSignalHead(inputName)
    self.outSignal = signals.getSignalHead(outputName)
    self.inSignal.addPropertyChangeListener(self)
    return
  def propertyChange(self, event):
    self.outSignal.setAppearance(self.inSignal.getAppearance())
    return

# Example of use - add a line like the following 
# that includes system or user names for the signal head to be repeated,
# and the signal head to repeat it onto. These must already exist.
# SignalFollowerListener().set("CH2001","CH2001R")
# SignalFollowerListener().set("CH2002","CH2002R")
 
