# Listen to all sensors, printing a line when they change state
#
# Author: Bob Jacobsen, copyright 2005
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

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
        print "Sensor", event.source.systemName, "(",event.source.userName,") from", stateName(event.oldValue), "to", stateName(event.newValue)

listener = SensorListener()

# Define a Manager listener.  When invoked, a new
# item has been added, so go through the list of items removing the 
# old listener and adding a new one (works for both already registered
# and new sensors)
class ManagerListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    list = event.source.getSystemNameList()
    for i in range(list.size()) :
        event.source.getSensor(list.get(i)).removePropertyChangeListener(listener)
        event.source.getSensor(list.get(i)).addPropertyChangeListener(listener)

# Attach the sensor manager listener
sensors.addPropertyChangeListener(ManagerListener())

# For the sensors that exist, attach a sensor listener
list = sensors.getSystemNameList()
for i in range(list.size()) :
    sensors.getSensor(list.get(i)).addPropertyChangeListener(listener)


 