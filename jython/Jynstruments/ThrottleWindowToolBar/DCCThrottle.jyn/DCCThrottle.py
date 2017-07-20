# Author: Lionel Jeanson copyright 2017
#
# Inspired from ThrottleBridge.py
# Author: Paul Bender, copyright 2004,2010
#
# Part of the JMRI distribution
#
# Listen for a given (3 as a default) DCC throttle commands 
# and forward them to the curently selected one in throttle window
#
# See JMRI output or log in case of issue.
#
# Customize at will.

listenToDCCThrottle = 3

import jmri
import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.beans.PropertyChangeListener as PropertyChangeListener
import jmri.jmrit.throttle.AddressListener as AddressListener
import javax.swing.JButton as JButton
import javax.swing.ImageIcon as ImageIcon

class DCCThrottle(Jynstrument, PropertyChangeListener, AddressListener, jmri.ThrottleListener):

    #Property listener part
    def propertyChange(self, event):
        if (event.propertyName == "ThrottleFrame") :  # Current throttle frame changed
            event.oldValue.getAddressPanel().removeAddressListener(self)
            self.addressPanel = event.newValue.getAddressPanel()
            self.panelThrottle = self.addressPanel.getThrottle()
            self.addressPanel.addAddressListener(self)
            return
        if(self.panelThrottle == None):
            return
        if(event.propertyName == "IsForward"):
           self.panelThrottle.setIsForward(event.newValue)
        if(event.propertyName == "SpeedSetting"):
            self.panelThrottle.setSpeedSetting(event.newValue)
        if(event.propertyName == "F0"):
            self.panelThrottle.setF0(event.newValue)
        if(event.propertyName == "F1"):
            self.panelThrottle.setF1(event.newValue)
        if(event.propertyName == "F2"):
            self.panelThrottle.setF2(event.newValue)
        if(event.propertyName == "F3"):
            self.panelThrottle.setF3(event.newValue)
        if(event.propertyName == "F4"):
            self.panelThrottle.setF4(event.newValue)
        if(event.propertyName == "F5"):
            self.panelThrottle.setF5(event.newValue)
        if(event.propertyName == "F6"):
            self.panelThrottle.setF6(event.newValue)
        if(event.propertyName == "F7"):
            self.panelThrottle.setF7(event.newValue)
        if(event.propertyName == "F8"):
            self.panelThrottle.setF8(event.newValue)
        if(event.propertyName == "F9"):
            self.panelThrottle.setF9(event.newValue)
            
    #ThrottleListener part (real dccThrottle)
    def notifyThrottleFound(self, dccThrottle):        
        self.masterThrottle = dccThrottle
        self.masterThrottle.addPropertyChangeListener(self)
    
    def notifyFailedThrottleRequest(self, locoAddress, reason):
        print "Couldn't get throttle for "+locoAddress+" : "+reason
        self.masterThrottle = None
    
    #AddressListener part: to listen for address changes in address panel (release, acquired)
    def notifyAddressChosen(self, address):
        pass

    def notifyAddressThrottleFound(self, throt):
        self.panelThrottle = throt

    def notifyAddressReleased(self, address):
        self.panelThrottle = None

    def notifyConsistAddressChosen(self, address, isLong):
        self.notifyAddressChosen(address)

    def notifyConsistAddressThrottleFound(self, throttle):
        self.notifyAddressThrottleFound(throttle)

    def notifyConsistAddressReleased(self, address, isLong):
        self.notifyAddressReleased(address)
    
    #Jynstrument main and mandatory methods
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleWindow"
    
    def init(self):
        self.getContext().addPropertyChangeListener(self) #ThrottleFrame change
        self.addressPanel = self.getContext().getCurrentThrottleFrame().getAddressPanel()
        self.addressPanel.addAddressListener(self) # change of throttle in Current frame
        self.panelThrottle = self.getContext().getCurrentThrottleFrame().getAddressPanel().getThrottle() # the throttle
        self.label = JButton(ImageIcon(self.getFolder() + "/DCCThrottle.png","DCCThrottle")) #label
        self.label.addMouseListener(self.getMouseListeners()[0]) # In order to get the popupmenu on the button too
        self.add(self.label)
        # create a dcc throttle and request one from the ThrottleManager
        self.masterThrottle = None
        if ( jmri.InstanceManager.throttleManagerInstance().requestThrottle(listenToDCCThrottle, self) == False):
            print "Couldn't request a throttle for "+locoAddress        

    def quit(self):
        self.masterThrottle.removePropertyChangeListener(self)
        self.masterThrottle = None
	self.panelThrottle = None
        self.addressPanel.removeAddressListener(self)
        self.addressPanel = None
        self.getContext().removePropertyChangeListener(self)               


