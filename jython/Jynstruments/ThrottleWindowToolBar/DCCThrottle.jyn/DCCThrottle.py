# Author: Lionel Jeanson copyright 2017
#
# Inspired from ThrottleBridge.py
# Author: Paul Bender, copyright 2004,2010
#
# Part of the JMRI distribution
#
# Listen for a given (3 as a default) DCC throttle commands 
# and forward them to the curently selected one in throttle window
# or call advanced function if any defined for curently selected roster entry
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
import time

class DCCThrottle(Jynstrument, PropertyChangeListener, AddressListener, jmri.ThrottleListener):
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
        # Advanced functions
        self.advFunctions = AdvFunctions()

    def quit(self):
        if (self.masterThrottle != None):
            self.masterThrottle.removePropertyChangeListener(self)
            self.masterThrottle = None
        self.panelThrottle = None
        self.advFunctions = None
        if (self.addressPanel != None):
            self.addressPanel.removeAddressListener(self)
            self.addressPanel = None            
        self.getContext().removePropertyChangeListener(self)               

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
           return
        if(event.propertyName == "SpeedSetting"):
            self.panelThrottle.setSpeedSetting(event.newValue)
            return
        if(event.propertyName == "F0"):
            if (self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "0", event.newValue, self.panelThrottle) != None):
                return
            self.panelThrottle.setF0(event.newValue)
            return
        if(event.propertyName == "F1"):
            if (self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "1", event.newValue, self.panelThrottle) != None):
                return
            self.panelThrottle.setF1(event.newValue)
            return
        if(event.propertyName == "F2"):
            if (self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "2", event.newValue, self.panelThrottle) != None):
                return
            self.panelThrottle.setF2(event.newValue)
            return
        if(event.propertyName == "F3"):
            if (self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "3", event.newValue, self.panelThrottle) != None):
                return
            self.panelThrottle.setF3(event.newValue)
            return
        if(event.propertyName == "F4"):
            if (self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "4", event.newValue, self.panelThrottle) != None):
                return
            self.panelThrottle.setF4(event.newValue)
            return
        if(event.propertyName == "F5"):
            if (self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "5", event.newValue, self.panelThrottle) != None):
                return
            self.panelThrottle.setF5(event.newValue)
            return
        if(event.propertyName == "F6"):
            if (self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "6", event.newValue, self.panelThrottle) != None):
                return
            self.panelThrottle.setF6(event.newValue)
            return
        if(event.propertyName == "F7"):
            if (self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "7", event.newValue, self.panelThrottle) != None):
                return
            self.panelThrottle.setF7(event.newValue)
            return
        if(event.propertyName == "F8"):
            if (self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "8", event.newValue, self.panelThrottle) != None):
                return
            self.panelThrottle.setF8(event.newValue)
            return
        if(event.propertyName == "F9"):
            if (self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "9", event.newValue, self.panelThrottle) != None):
                return
            self.panelThrottle.setF9(event.newValue)
            return
            
    #ThrottleListener part (real dccThrottle)
    def notifyThrottleFound(self, dccThrottle):        
        self.masterThrottle = dccThrottle
        self.masterThrottle.addPropertyChangeListener(self)
    
    def notifyFailedThrottleRequest(self, locoAddress, reason):
        self.masterThrottle = None
        # Sleep a bit and try again
        try:
            time.sleep(1)
        except BaseException:
            pass
        if ( jmri.InstanceManager.throttleManagerInstance().requestThrottle(listenToDCCThrottle, self) == False):
            print "Couldn't request a throttle for "+locoAddress
            
    def notifyStealThrottleRequired(LocoAddress):
        pass # Throttle steal decisions are delegated to the Hardware
        
    def notifyDecisionRequired(LocoAddress, decision):
        pass # Throttle steal / share decisions are delegated to the ThrottleManager
    
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
    

class AdvFunctions():
    def call(self, rosterEntry, advFn, status, throttle):
        assert (rosterEntry!=None), "rosterEntry is null"
        assert (advFn!=None), "advFn is null"
        assert (status!=None), "status is null"        
        assert (throttle!=None), "throttle is null"
        todoStr = rosterEntry.getAttribute("advF"+advFn)
        if (todoStr == None):
            return None
       # poor man parser, should unserialize a json object instead
        todo = todoStr.split(";")
        for task in todo:
            task = task.lstrip()
            # Actual function call 
            if (task.startswith("F")):                 
                task = task.rstrip()
                setter = None
                getter = None
                ok = False
                for fct in throttle.getClass().getMethods():
                    fctName = fct.getName()
                    if (fctName == "set"+task):
                        setter=fct
                    if (fctName == "get"+task):
                        getter=fct
                    if (setter != None and getter != None):
                        ok = True
                        break
                if (ok):
                    if (not rosterEntry.getFunctionLockable(int(task[1:]))):
                        setter.invoke(throttle, status)
                    else:
                        state = getter.invoke(throttle)
                        setter.invoke(throttle, not state)
                continue
            # Play sound
            if (task.startswith("P") and status):
                path = task[1:]
                self.play(path, throttle)
                continue
        return True
                
    def play(self, sndPath, throttle):
        assert (sndPath!=None), "sndPath is null"
        sourceName="IAS"+sndPath+"-"+str(throttle.getLocoAddress())
        bufferName="IAB"+sndPath
        source = audio.getAudio(sourceName)
        if (source == None):
            buffer = audio.getAudio(bufferName)
            if (buffer == None):
                buffer = audio.provideAudio(bufferName)
                buffer.setURL(sndPath)
            source = audio.provideAudio(sourceName)
            source.setAssignedBuffer(bufferName)
        # would need to update location here
        source.play()

