# Listen to all sensors, printing a line when they change state.
#
# Optionally, this can also speak the information through
# your computer's speakers if you have the "speak" command 
# installed.
#
# For an example of this in action:
#  http://www.youtube.com/user/2sk21#p/a/u/0/0rDqBob3Vpk
#  (visited February 2010)
#
# Author: Bob Jacobsen, copyright 2005, 2008
# Part of the JMRI distribution

import jmri
import java
import java.beans

# Define routine to map status numbers to text
def stateName(state) :
    if (state == ACTIVE) :
        return "ACTIVE"
    if (state == INACTIVE) :
        return "INACTIVE"
    if (state == INCONSISTENT) :
        return "INCONSISTENT"
    if (state == UNKNOWN) :
        return "UNKNOWN"
    return "(invalid)"
    

# Define the sensor listener: Print some
# information on the status change.
class SensorListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    if (event.propertyName == "KnownState") :
        mesg = "Sensor "+event.source.systemName
        if (event.source.userName != None) :
            mesg += " ("+event.source.userName+")"
        mesg += " from "+stateName(event.oldValue)
        mesg += " to "+stateName(event.newValue)
        print mesg
        # You can also speak the message by un-commenting the next line
        #java.lang.Runtime.getRuntime().exec(["speak", mesg])
        # For more info on the speak command, see http://espeak.sf.net/
    return
    
listener = SensorListener()

# Define a Manager listener.  When invoked, a new
# item has been added, so go through the list of items removing the 
# old listener and adding a new one (works for both already registered
# and new sensors)
class ManagerListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    list = event.source.getNamedBeanSet()
    for sensor in list :
        sensor.removePropertyChangeListener(listener)
        sensor.addPropertyChangeListener(listener)

# Attach the sensor manager listener
sensors.addPropertyChangeListener(ManagerListener())

# For the sensors that exist, attach a sensor listener
list = sensors.getNamedBeanSet()
for sensor in list :
    sensor.addPropertyChangeListener(listener)


