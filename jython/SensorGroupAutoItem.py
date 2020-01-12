# To make one selection of a SensorGroup a "none of the above" choice
#
# SensorGroups make sure that only one item is ACTIVE, but it's possible 
# for none to be.  If you have panel levers with physical sensors for LEFT
# and RIGHT, use one of these to get a NONE sensor.
#
# Ideally, this would get the list of sensors from the sensor group itself,
# but that's surprisingly hard.
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution

import jmri
import java
import java.beans

# Define the listener. 
class SensorGroupAutoItem(java.beans.PropertyChangeListener):
  def set(self, defaultName, groupList) :
    self.default = sensors.getSensor(defaultName)
    self.groupNames = groupList
    # process fake event to set the initial state
    self.propertyChange(java.beans.PropertyChangeEvent(sensors.getSensor(self.groupNames[0]), "KnownState", 0, 0))
    # set up listeners
    self.default.addPropertyChangeListener(self)
    for name in self.groupNames :
        sensors.getSensor(name).addPropertyChangeListener(self)
    return
  def propertyChange(self, event):
    # check for any active
    for name in self.groupNames : 
        if (sensors.getSensor(name).knownState == ACTIVE) : 
            self.default.state = INACTIVE
            return
    self.default.state = ACTIVE
    return

# Example of use - add lines like the following 
# that includes system or user names for the default sensor and 
# the rest of the SensorGroup. These must already exist.
# SensorGroupAutoItem().set("IS100",["IS101", "IS102"])
 
