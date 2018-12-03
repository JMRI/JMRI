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
#

import jmri
import java
import java.beans

from time import sleep

# define some global objects as placeholders

# First, define the property change listener.  for the
# throttle.   This one sends speed and direction information
# to the second loconet throttle and prints information about
# received events.
class tbListener(java.beans.PropertyChangeListener):
 
    def propertyChange(self, event):
        print "change",event.propertyName
        print "from", event.oldValue, "to", event.newValue
        if(a.lnSlot == None):
            return
        if(event.propertyName == "isForward"):
            a.lnThrottle.setIsForward(event.newValue)
        if(event.propertyName == "SpeedSetting"):
            a.lnThrottle.setSpeedSetting(event.newValue)

# Second, we create a listener for the loconet slot
# this listener is necessary to create the loconet
# throttle.
class tbSlotListener(jmri.jmrix.loconet.SlotListener):

    def notifyChangedSlot(self,slot):
        print "slot changed"
        if(a.lnSlot == None):
            a.lnSlot = slot
            a.lnThrottle = jmri.jmrix.loconet.LocoNetThrottle(slot);

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
     
        # One throttle is easy, all we need to do is
        # call the getThrottle routine
        self.xnThrottle = self.getThrottle(3,False);
     
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

            lnMemo=jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0);
            lnMemo.getSlotManager().slotFromLocoAddress(3,self.sl)
         
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
