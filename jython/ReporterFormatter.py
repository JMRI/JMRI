# Provides an example of listening to a Reporter, and
# putting the changes to a Memory in a nicer format
#
# Author: Bob Jacobsen, copyright 2006
# Part of the JMRI distribution
#
# The Reporter and Memory names are hardcoded in the example
# near the bottom.  Change those to something that makes
# sense for your layout

import jmri
import java

# First, define the listener class.  This gets messages
# from the reporter, uses them to keep track of the decoders
# in a block, and writes that list to a memory for display.
#
class ReporterFormatter(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    if (event.propertyName == "currentReport") :
      # new report, format and store
      self.report = event.newValue
      self.value = self.format(self.report)
      self.memory.setValue(self.value)
    return 

  def start(self, reporterName, memoryName) :
    # connect the object to the reporter, and start to work
    self.memory = jmri.InstanceManager.memoryManagerInstance().provideMemory(memoryName)
    jmri.InstanceManager.reporterManagerInstance().provideReporter(reporterName).addPropertyChangeListener(self)
    self.content = []
    self.memory.setValue("")
    return

  def stop(self) :
    # Cease operation.  
    # You can call start() again if desired.
    jmri.InstanceManager.reporterManagerInstance().getReporter(reporterName).removePropertyChangeListener(self)
    return
    
  def format(self, inputString) :
    # Return a formated version of the input string.
    # This is where the real work of the class takes place.
    #
    # In this simple version, we just keep a list that contains
    # all the names, and return that
    #
    # First check if this is an arrival
    if (self.isEnter(inputString)) :
        # yes, if name not there, append
        if (not (self.number(inputString) in self.content)) :
          self.content.append(self.number(inputString))  
    # Then check if this is a departure
    if (self.isExit(inputString)) :
        # yes, if name there, remove
        if (self.number(inputString) in self.content) :
          del self.content[self.content.index(self.number(inputString))]
    # and return updated value
    result = ""
    for item in self.content :
      result = result+item+" "
    return (result)

  def number(self, message) :
    # return the number of the locomotive from a message
    return message[:-6]

  def isEnter(self, message) :
    # return True if the message is an "entry"
    return (message[-5:]=="enter")
    
  def isExit(self, message) :
    # return True if the message is an "exits"
    return (message[-5:]=="exits")
    
# End of the definition of the FormattedReporter class
    
#########################################################

# Below here is an example of the use of this class.
# Modify if to use names appropriate for your layout.
m = ReporterFormatter()
m.start("LR145", "IM145")
# At this point, messages from LocoNet reporter LR145 are being sent to memory IM145
# To stop this, you can later say
# m.stop()

# Do a couple more as examples
m = ReporterFormatter()
m.start("LR146", "IM146")

m = ReporterFormatter()
m.start("LR147", "IM147")


#########################################################

# Define some test routines. 
# These don't do anything until invoked by hand to test the routine.
# 
# Example msg: D0 20 0B 7D 03 FF - lower byte 1 and byte 2 are reporter 12, 
# 3,4 are loco address (3=7D short)
# 20 vs 00 in 2nd byte shows enter/exit
def test3enter() :  # 3 enters
    packet = jmri.jmrix.loconet.LocoNetMessage(6)
    packet.setElement(0, 0xD0)
    packet.setElement(1, 0x21)
    packet.setElement(2, 0x11)
    packet.setElement(3, 0x7D)
    packet.setElement(4, 0x03)
    jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(packet)
    return
    
def test257enter() :  # 257 enters
    packet = jmri.jmrix.loconet.LocoNetMessage(6)
    packet.setElement(0, 0xD0)
    packet.setElement(1, 0x21)
    packet.setElement(2, 0x11)
    packet.setElement(3, 0x02)
    packet.setElement(4, 0x01)
    jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(packet)
    return
    
def test257exit() :  # 257 exits
    packet = jmri.jmrix.loconet.LocoNetMessage(6)
    packet.setElement(0, 0xD0)
    packet.setElement(1, 0x01)
    packet.setElement(2, 0x11)
    packet.setElement(3, 0x02)
    packet.setElement(4, 0x01)
    jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(packet)
    return
    
def test3exit() :  # 3 exits
    packet = jmri.jmrix.loconet.LocoNetMessage(6)
    packet.setElement(0, 0xD0)
    packet.setElement(1, 0x01)
    packet.setElement(2, 0x11)
    packet.setElement(3, 0x7D)
    packet.setElement(4, 0x03)
    jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(packet)
    return
