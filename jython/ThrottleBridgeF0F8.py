# This is a proof of concept script showing how to bridge a throttle
# from one system to another.
#
# The test configuration for this scrip has a LocoNet system
# configured as the first connection and an XPressNet based system
# configured as the second connection.  This means the XpressNet based
# system's ThrottleManager is the system throttle manager.
#
# If you are using software throttles, this should work with any system
# as the controlling throttles, so long as that system is listed as the
# last connection in the connection preferences.
#
# If you want to control the trains with a hardware throttle, the system
# you use needs to be able to get updates about the throttle status from
# the layout.  This is not possible with all systems.
#
# In this version of the script, the controlled system MUST be a
# LocoNet based system.  If you have more than one loconet
# connection configured, the first loconet connection in the list of
# loconet connections will be used.
#
# This version of the script works with address 3.
#
# Author: Paul Bender, copyright 2004,2010
# Part of the JMRI distribution
# Updated: steambigboy, 2024
#

import jmri
import java
import java.beans

from time import sleep

lnMemo = jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0);
# define some global objects as placeholders

# First, define the property change listener.  for the
# throttle.   This one sends speed and direction information
# to the second loconet throttle and prints information about
# received events.
class tbListener(java.beans.PropertyChangeListener):
    oldValue = None
    newValue = None
    def propertyChange(self, event):
        print "change",event.propertyName
        print "from", event.oldValue, "to", event.newValue
        print "a slot check", a.lnSlot.getSlot()
        if(a.lnSlot == None):
            return
        if(event.propertyName == "IsForward"):
            print "Fw: ", event.newValue
            a.lnThrottle.setIsForward(event.newValue)
        if(event.propertyName == "SpeedSetting"):
            print "Speed: ", event.newValue
            a.lnThrottle.setSpeedSetting(event.newValue)
        if(event.propertyName == "F0"):
            print "F0: ", event.newValue
            a.lnThrottle.setF0(event.newValue)
        if(event.propertyName == "F1"):
            print "F1: ", event.newValue
            a.lnThrottle.setF0(event.newValue)
        if(event.propertyName == "F2"):
            print "F2: ", event.newValue
            a.lnThrottle.setF0(event.newValue)
        if(event.propertyName == "F3"):
            print "F3: ", event.newValue
            a.lnThrottle.setF0(event.newValue)
        if(event.propertyName == "F4"):
            print "F4: ", event.newValue
            a.lnThrottle.setF0(event.newValue)
        if(event.propertyName == "F5"):
            print "F5: ", event.newValue
            a.lnThrottle.setF0(event.newValue)
        if(event.propertyName == "F6"):
            print "F6: ", event.newValue
            a.lnThrottle.setF0(event.newValue)
        if(event.propertyName == "F7"):
            print "F7: ", event.newValue
            a.lnThrottle.setF0(event.newValue)
        if(event.propertyName == "F8"):
            print "F8: ", event.newValue
            a.lnThrottle.setF0(event.newValue)
            

# Second, we create a listener for the loconet slot
# this listener is necessary to create the loconet
# throttle.
class tbSlotListener(jmri.jmrix.loconet.SlotListener):

    def notifyChangedSlot(self,slot):
        print "slot changed"
        if(a.lnSlot == None):
            a.lnSlot = slot
            print "LocoAddress", a.lnSlot.locoAddr()
            print "Slot", a.lnSlot.getSlot()
            a.lnThrottle = jmri.jmrix.loconet.LocoNetThrottle(lnMemo, slot);
            print "lnThrottle:", a.lnThrottle

# class to keep this thread running
class ThrottleBridge(jmri.jmrit.automat.AbstractAutomaton) :

    # we need some class global data for items we can't initilize
    # until we get some information back from the layout
    lnThrottle = None
    lnSlot = None
    #init() is called exactly once at the begining to do any
    #necessary configuration
    def init(self):
        # we want to get a throttle for two systems assigned
        # to the same address.
        print "XpressNet"
        # One throttle is easy, all we need to do is
        # call the getThrottle routine
        self.xnThrottle = self.getThrottle(3,False);
        print "XNet: ", self.xnThrottle
        print "XNet details: ", self.xnThrottle.getDccAddress()
        print "XNet status: ", self.xnThrottle.getSpeedStepMode()
#        print "XNet discon: ", self.xnThrottle.notifyThrottleDisconnect()
        if (self.xnThrottle == None) :
            print "Couldn't assign throttle!"
        else:
            # If the throttle was available, then we need to add
            # a throttle listener to it
            self.tl = tbListener()
            self.xnThrottle.addPropertyChangeListener(self.tl)
            # and then request the second throttle.
            # The second throttle takes more work, because
            # it isn't the default manager.
            self.sl = tbSlotListener()
            lnMemo.getSlotManager().slotFromLocoAddress(3,self.sl)
 #           self.xnThrottle.throttleDispose()
        return
        #handle() is called repeated until it returns false.
    def handle(self):
        sleep(100.)
        return 1
#end of class definition
# create one of these
a = ThrottleBridge()
#set the name
a.setName("Throttle Bridge")
# adn start it running
a.start()

