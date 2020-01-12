# If you have an external signal system, not run
# by JMRI, and you want to put SignalHeads on a 
# JMRI panel, this script will listen to Sensors
# that are connected to that external signal system
# and drive the appearance of a SignalHead within JMRI.
#
# Note that the SignalHead objects need to have been
# previously defined, e.g. by a panel file. If you
# just want to put them on a panel, they can be Virtual SignalHeads,
# or they can be of a type that'll drive real hardware, whatever
# you need; the script doesn't care.
#
# Author: Bob Jacobsen, copyright 2013
# Part of the JMRI distribution

import jmri
import java
import java.beans

# Define the listener. 
class SignalSensorListener(java.beans.PropertyChangeListener):
  def set(self, signal, sensor, appearance) :
    if (sensor == None) : return
    self.signal = signals.getSignalHead(signal)
    self.appearance = appearance
    sensors.provideSensor(sensor).addPropertyChangeListener(self)
    return
  def propertyChange(self, event):
    print "change",event.propertyName
    print "from", event.oldValue, "to", event.newValue
    print "source systemName", event.source.systemName
    print "source userName", event.source.userName
    if (event.newValue == ACTIVE) :
        self.signal.setAppearance(self.appearance)
    return
   
# Define a service routine to create multiple listeners 
def connect(signal, red, yellow, green) :
    SignalSensorListener().set(signal,red,RED)
    SignalSensorListener().set(signal,yellow,YELLOW)
    SignalSensorListener().set(signal,green,GREEN)
    return

# Example of use - add a line like this for 
# each signal head and red, yellow, green input 
# you'd like. Use None if a color input doesn't exist.
connect("IH1","LS1001","LS1002","LS1003")
 
