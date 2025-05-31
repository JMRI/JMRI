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
# Author: Bob Jacobsen, copyright 2016, 2025
# Part of the JMRI distribution

import jmri
import java

class FollowSE8c(jmri.jmrix.loconet.LocoNetListener):
  def set(self, signalHeadName, lowTurnoutNumber, highTurnoutNumber) :
    self.signal = signals.getSignalHead(signalHeadName)
    self.lowTO = lowTurnoutNumber
    self.highTO = highTurnoutNumber
    
    # get the LocoNet connection (the first of potentially several LocoNet connections)
    myLocoNetConnection = jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0);
    myLocoNetConnection.getLnTrafficController().addLocoNetListener(0xFF,self)
    return
  
  def address(self, message) :
    a1 = message.getElement(1)
    a2 = message.getElement(2)
    return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1)
    
  def closed(self, message) :
    a2 = message.getElement(2)
    return ((a2 & 0x20) != 0)
    
  def message(self, message):
    if (message.getOpCode() == 0xB0) :
        if (self.address(message) == self.lowTO) : 
           if (self.closed(message)) :
              self.signal.setAppearance(GREEN)
           else :
              self.signal.setAppearance(RED)
        if (self.address(message) == self.highTO) : 
           if (self.closed(message)) :
              self.signal.setAppearance(DARK)
           else :
              self.signal.setAppearance(YELLOW)
    return

# Example of use - add a line like the following 
# that includes system or user names for the signal head
# and the two turnout numbers for the SE8c addresses
# FollowSE8c().set("CH2001", 301, 302)
 
