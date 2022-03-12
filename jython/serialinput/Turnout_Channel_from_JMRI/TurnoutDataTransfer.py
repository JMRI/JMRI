# Transfer "TurnOut" Data from to an Arduino via Serial Transmission
# Author: Geoff Bunza 2018 based in part on a script by
# Bob Jacobsen as part of the JMRI distribution
# Version 1.1
# Connects JMRI Turnout "Watcher" to an Arduino Output Channel
# Note that JMRI must be set up to have a valid
# turnout table; if you're not using some other DCC connection, 
# configure JMRI to use LocoNet Simulator

import jarray
import jmri
import java
import purejavacomm

# find the port info and open the port
global extportin
portname = "COM5"
portID = purejavacomm.CommPortIdentifier.getPortIdentifier(portname)
try:
    port = portID.open("JMRI", 50)
except purejavacomm.PortInUseException:
    port = extportin
extportin = port
# set options on port
baudrate = 19200
port.setSerialPortParams(baudrate, 
    purejavacomm.SerialPort.DATABITS_8, 
    purejavacomm.SerialPort.STOPBITS_1, 
    purejavacomm.SerialPort.PARITY_NONE)
# Anticipate the Port Opening will restart the Arduino
self.delayMsec(2000)
# get I/O connections for later
inputStream = port.getInputStream()
outputStream = port.getOutputStream()

# define a turnout listener that will 
class Datatransfer(java.beans.PropertyChangeListener):
  # initialization 
  # registers to receive events
  def __init__(self, id, value) :
        self.name = "AT"+str(id)
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
        print "set CLOSED for", event.source.userName
        outputStream.write(event.source.userName)
        outputStream.write(",0")
      if (event.newValue == THROWN and event.oldValue != THROWN) :
        print "set THROWN for", event.source.userName
        outputStream.write(event.source.userName)
        outputStream.write(",1")        
    return

# The olloewing will set up 68 listeers for Turnouts AT2 though AT69 (by username)
for x in range(2,70) :
    Datatransfer(x,x+100)

