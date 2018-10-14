# Connect JMRI turnouts to parallel port logic
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#
# Maps internal turnouts to specific values to be send to the
# parallel port.  Note that JMRI must be set up to have a valid
# turnout table; if you're not using some other DCC connection, 
# configure JMRI to use LocoNet Simulator

import jmri
import java
import java.beans

name = "LPT1" 
#name = "/dev/cu.usbmodem3d11" # debug

# find the port info and open the port
import purejavacomm
port = purejavacomm.CommPortIdentifier.getPortIdentifier(name)
parallelPort = port.open("JMRI", 50)
outputStream = parallelPort.getOutputStream()
print "Port opened OK"

# define a turnout listener that will 
# activate the parallel port as needed
class ParallelDriver(java.beans.PropertyChangeListener):
  # initialization stores some values for later,
  # registers to receive events
  def __init__(self, id, value) :
    self.name = "IT"+str(id)
    self.closed = value     # write this value to close
    self.thrown = value    # write this value to throw
    turnout = turnouts.provideTurnout(self.name)
    turnout.addPropertyChangeListener(self)
    turnout.setCommandedState(CLOSED)
    return
  # on a property change event, first see if 
  # right type, and then write appropriate
  # value to port based on new state
  def propertyChange(self, event):
    #print "change",event.propertyName
    #print "from", event.oldValue, "to", event.newValue
    #print "source systemName", event.source.systemName
    if (event.propertyName == "CommandedState") :
      if (event.newValue == CLOSED and event.oldValue != CLOSED) :
        print "set CLOSED for ", event.source.systemName
        outputStream.write(self.closed)
      if (event.newValue == THROWN and event.oldValue != THROWN) :
        print "set THROWN for ", event.source.systemName
        outputStream.write(self.thrown)
    return

ParallelDriver(1,232)
ParallelDriver(2,233)
ParallelDriver(3,234)
ParallelDriver(4,235)
ParallelDriver(5,236)
ParallelDriver(6,237)
ParallelDriver(7,238)
ParallelDriver(8,239)
ParallelDriver(9,216)
ParallelDriver(10,217)
ParallelDriver(11,218)
ParallelDriver(12,219)
ParallelDriver(13,220)
ParallelDriver(14,221)
ParallelDriver(15,222)
ParallelDriver(16,223)
ParallelDriver(17,184)
ParallelDriver(18,185)
ParallelDriver(19,186)
ParallelDriver(20,187)
ParallelDriver(21,188)
ParallelDriver(22,189)
ParallelDriver(23,190)
ParallelDriver(24,192)

print "Initialization Complete"
