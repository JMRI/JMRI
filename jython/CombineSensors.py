# Make a master sensor follow a set of follower sensor
# *) Changes to the known state of the followers map to the known state of the master,
#       following the rules (first applied wins)
#          Any UNKNOWN -> UNKNOWN
#          Any INCONSISTENT -> INCONSISTENT
#          Any ACTIVE -> ACTIVE
#          All INACTIVE -> INACTIVE
#          Otherwise, INCONSISTENT
# *) changes to the known state of the master does nothing
#
# This is hard to do properly in Logix due to limited access to states
# and events (can't tell which change happened due to what)
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution

import jmri
import java
import java.beans

# Define the listener. 
class CombineSensors(java.beans.PropertyChangeListener):
  def set(self, masterName, followerList) :
    self.master = sensors.getSensor(masterName)
    self.followers = followerList
    # force event to set the initial state
    self.propertyChange(java.beans.PropertyChangeEvent(sensors.getSensor(followerList[0]), "KnownState", 0, 0))
    # set up listeners
    self.master.addPropertyChangeListener(self)
    for follower in self.followers :
        sensors.getSensor(follower).addPropertyChangeListener(self)
    return
  def propertyChange(self, event):
    # print "Receive change of", event.propertyName, "from", self.master.describeState(event.oldValue), "to", self.master.describeState(event.newValue), "in", event.source.userName
    if (event.source != self.master and event.propertyName == "KnownState" ): 
        # do the KnownState rules
        for follower in self.followers :
            if (sensors.getSensor(follower).getKnownState() == UNKNOWN) : 
                self.master.setKnownState(UNKNOWN)
                return
        for follower in self.followers :
            if (sensors.getSensor(follower).getKnownState() == INCONSISTENT) : 
                self.master.setKnownState(INCONSISTENT)
                return
        for follower in self.followers :
            if (sensors.getSensor(follower).getKnownState() == ACTIVE) : 
                self.master.setKnownState(ACTIVE)
                return
        inactive = True
        for follower in self.followers :
            if (sensors.getSensor(follower).getKnownState() != INACTIVE) : 
                inactive = False
        if (inactive) :
            self.master.setKnownState(INACTIVE)
            return
        self.master.setKnownState(INCONSISTENT)
    return

# Example of use - add lines like the following 
# that includes system or user names for the master sensor and 
# the array of followers. These must already exist.
# CombineSensors().set("IS100",["IS101", "IS102"])
 
