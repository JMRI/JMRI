# Transfer "Light" Data to an Arduino via Serial Transmission
# Author: Kevin Appleby 2023 based on a script by
# Bob Jacobsen and Geoff Bunza as part of the JMRI distribution
# Version 1.3
# Connects JMRI Light "Watcher" to an Arduino Output Channel
# Note that JMRI must be set up to have a valid
# light table; if you're not using some other DCC connection,
# configure JMRI to use LocoNet Simulator

import jarray
import jmri
import java
import purejavacomm
import java.beans

# find the port info and open the port
global extportin
portname = "/dev/cu.usbmodem14201"
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
# Anticipate the Port Opening will restart the Arduino. The following line was in the original script but does not work
self.delayMsec(2000)
# get I/O connections for later
inputStream = port.getInputStream()
outputStream = port.getOutputStream()

# define a turnout listener that will
class LightDatatransfer(java.beans.PropertyChangeListener):
  # initialization
  # registers to receive events
  def __init__(self, id, value) :
        self.name = "AL"+str(id)
        self.on = value     # write this value to set on
        self.off = value    # write this value to set off
        light = lights.provideLight(self.name)
        light.addPropertyChangeListener(self)
        light.setCommandedState(OFF)
        return

  # on a property change event, first see if
  # right type, and then write appropriate
  # value to port based on new state
  def propertyChange(self, event):
    #print "change",event.propertyName
    #print "from", event.oldValue, "to", event.newValue
    #print "source systemName", event.source.systemName
    if (event.propertyName == "TargetIntensity") :
      if (event.newValue == 0 and event.oldValue != 0) :
        print "set OFF for", event.source.userName
        outputStream.write(event.source.userName)
        outputStream.write(",0")
      if (event.newValue == 1 and event.oldValue != 1) :
        print "set ON for", event.source.userName
        outputStream.write(event.source.userName)
        outputStream.write(",1")
    return

# The following will set up 68 listeners for Light AL2 though AL69 (by username)
for x in range(2,70) :
    LightDatatransfer(x,x+100)

