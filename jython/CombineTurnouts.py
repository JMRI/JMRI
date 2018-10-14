# Make a set of turnouts follow a single one
# *) Changes to commanded state of the master are copied to the followers
# *) Changes to the known state of the followers map to the known state of the master,
#       following the rules (first applied wins)
#          Any UNKNOWN -> UNKNOWN
#          Any INCONSISTENT -> INCONSISTENT
#          All THROWN -> THROWN
#          All CLOSED -> CLOSED
#          Otherwise, INCONSISTENT
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
class CombineTurnouts(java.beans.PropertyChangeListener):
  def set(self, masterName, followerList) :
    self.master = turnouts.getTurnout(masterName)
    self.followers = followerList
    # force set the initial state
    for follower in self.followers :
        turnouts.getTurnout(follower).setCommandedState(self.master.getCommandedState())
    # set up listeners
    self.master.addPropertyChangeListener(self)
    for follower in self.followers :
        turnouts.getTurnout(follower).addPropertyChangeListener(self)
    return
  def propertyChange(self, event):
    # print "Receive change of", event.propertyName, "from", self.master.describeState(event.oldValue), "to", self.master.describeState(event.newValue), "in", event.source.userName
    # if a change to master commanded state
    if (event.source == self.master and event.propertyName == "CommandedState" ) :
        for follower in self.followers :
            if (self.master.getCommandedState() != turnouts.getTurnout(follower).getCommandedState()) : 
                turnouts.getTurnout(follower).setCommandedState(self.master.getCommandedState())
    elif (event.source != self.master and event.propertyName == "KnownState" ): 
        # otherwise, do the KnownState rules
        for follower in self.followers :
            if (turnouts.getTurnout(follower).getKnownState() == UNKNOWN) : 
                self.master.newKnownState(UNKNOWN)
                return
        for follower in self.followers :
            if (turnouts.getTurnout(follower).getKnownState() == INCONSISTENT) : 
                self.master.newKnownState(INCONSISTENT)
                return
        OK = True
        for follower in self.followers :
            if (turnouts.getTurnout(follower).getKnownState() != CLOSED) : 
                OK = False
        if (OK) :
            self.master.newKnownState(CLOSED)
            return
        OK = True
        for follower in self.followers :
            if (turnouts.getTurnout(follower).getKnownState() != THROWN) : 
                OK = False
        if (OK) :
            self.master.newKnownState(THROWN)
            return
        self.master.newKnownState(INCONSISTENT)
    return

# Example of use - add lines like the following 
# that includes system or user names for the master turnout and 
# the array of followers. These must already exist.
# CombineTurnouts().set("IT100",["IT101", "IT102"])
 
