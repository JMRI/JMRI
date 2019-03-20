# For each sensor, create a corresponding 
# internal turnout that tracks the sensor's state.
#
# Author: Bob Jacobsen, copyright 2013
# Part of the JMRI distribution
#

import jmri
import java
import java.beans

# Define routine to map status numbers to text
def stateMap(state) :
    if (state == ACTIVE) :
        return THROWN
    if (state == INACTIVE) :
        return CLOSED
    if (state == INCONSISTENT) :
        return INCONSISTENT
    if (state == UNKNOWN) :
        return UNKNOWN
    return "(invalid)"
    

# Define the sensor listener: 
# Makes a state change to corresponding Turnout
class SensorListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    if (event.propertyName == "KnownState") :
        name = "IT"+event.source.systemName
        # ensure exists
        turnout = turnouts.provideTurnout(name)
        # copy over the user name if present
        if ((event.source.userName != None) and (turnout.getUserName() == None)) :
            turnout.setUserName(event.source.userName)
        # copy over the state
        turnout.setState(stateMap(event.newValue))
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
for sensor in list:
    sensor.addPropertyChangeListener(listener)


 
