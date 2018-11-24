# Author: Lionel Jeanson copyright 2017
# Part of the JMRI distribution
#
# Use a Nintendo Wiimote device as a throttle
# You need to have Bluecove and WiiRemoteJ jars in your Java classpath, JMRI lib folder is a good place for that (copy both jars there)
# See: http://bluecove.org/ and https://github.com/micromu/WiiRemoteJ
#
# once Jynstrument started press 1+2 on the Wiimote you want to use, it should connect
# connection will be validated by Wiimote vibrating and one of the LED turning on
#
# See JMRI output or log in case of issue.
#
# Customize at will.
# Unfortunately, this is only a classic remote, nothing with movements
#
# Default control:
#   left / right : browse through throttles in instrumented window
#   home   : lights (function 0 or advanced function 0)
#    +/-   : direction
#     A    : brake
#     B    : accelerate
#    +&-   : EStop 
#     1    : function 1 (or advanced function 1)
#     2    : function 2 (or advanced function 2)
#

speedEStopSpeed = -1
valueSpeedTimerRepeat = 25 # repeat time in ms for speed set task
valueSpeedIncrement = 0.01

import java
import java.awt
import java.awt.event
import java.beans
import java.util
import java.beans.PropertyChangeListener as PropertyChangeListener
import java.awt.event.ActionListener as ActionListener
import java.util.Calendar as Calendar
import java.lang.Runnable as Runnable
import javax.swing.Timer as Timer
import javax.swing.JButton as JButton
import javax.swing.ImageIcon as ImageIcon
import javax.swing.SwingUtilities as SwingUtilities
import thread
import jmri.jmrit.throttle.AddressListener as AddressListener
import jmri.jmrit.jython.Jynstrument as Jynstrument
import wiiremotej.event.WiiRemoteListener as WiiRemoteListener
import wiiremotej.event.WiiDeviceDiscoveryListener as WiiDeviceDiscoveryListener
import wiiremotej.WiiRemoteJ as WiiRemoteJ
import wiiremotej.event.WRButtonEvent as WRButtonEvent


class WiimoteThrottle2(Jynstrument, PropertyChangeListener, AddressListener, WiiDeviceDiscoveryListener, WiiRemoteListener, Runnable):
    #Jynstrument main and mandatory methods
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleWindow"
    
    def init(self):
        self.getContext().addPropertyChangeListener(self) #ThrottleFrame change
        self.addressPanel=self.getContext().getCurrentThrottleFrame().getAddressPanel();
        self.addressPanel.addAddressListener(self) # change of throttle in Current frame
        self.throttle = self.getContext().getCurrentThrottleFrame().getAddressPanel().getThrottle() # the throttle
        self.speedAction =  SpeedAction()  #Speed increase thread
        self.speedAction.setThrottle( self.throttle )
        self.speedTimer = Timer(valueSpeedTimerRepeat, self.speedAction ) # Very important to use swing Timer object (see Swing and multithreading doc)
        self.speedTimer.setRepeats(True)
        self.label = JButton(ImageIcon(self.getFolder() + "/WiimoteThrottle2.png","WiiMote")) #label
        self.label.addMouseListener(self.getMouseListeners()[0]) # In order to get the popupmenu on the button too
        self.add(self.label)
        self.lastTimeButton1 = Calendar.getInstance().getTimeInMillis()
        self.lastTimeButton2 = Calendar.getInstance().getTimeInMillis()
        self.advFunctions = AdvFunctions()
        self.lastTimeEStop = Calendar.getInstance().getTimeInMillis()
        self.wiiDevice = None
        self.sync = thread.allocate_lock() # A lock protecting bellow self.evt
        self.evt = None
        java.lang.System.setProperty("bluecove.jsr82.psm_minimum_off", "true"); # Required for Bluecove + WiiRemoteJ
        WiiRemoteJ.findRemotes(self, 1) # Search for 1 Wiimote, and call back
       
    def quit(self):
        self.speedTimer.stop() 
        WiiRemoteJ.stopFind()
        if ((self.wiiDevice != None) and (self.wiiDevice.isConnected())):
            self.wiiDevice.removeWiiRemoteListener(self)
            self.wiiDevice.disconnect()
        self.wiiDevice = None
        self.speedAction = None
        self.speedTimer = None
        self.throttle = None
        self.advFunctions = None
        self.getContext().removePropertyChangeListener(self)
        self.addressPanel.removeAddressListener(self)
        self.addressPanel = None    
    #Wiimote discoverer events
        
    def findFinished(self, nb):
        print "Search finished, found ",nb ," wiimotes"

    def wiiDeviceDiscovered(self, evt):
        print "Found a Wiimote, number: ", evt.getNumber()
        self.wiiDevice = evt.getWiiDevice()
        ledLights = [False, False, False, False]
        ledLights[evt.getNumber()%4] = True
        self.wiiDevice.setLEDLights(ledLights)
        self.wiiDevice.addWiiRemoteListener(self)

    #Wiimote events        
    def buttonInputReceived(self, evt):
#        print("Wiimote Button event: ", evt)
        self.sync.acquire()
        self.evt = evt
        self.sync.release()
        SwingUtilities.invokeLater(self) # Delegate processing to Swing thread (when we are here, we're in the WiiRemoteJ driver thread)

    def run(self):
        self.sync.acquire()
        evt = self.evt
        self.sync.release()
        if (self.speedTimer != None):
            self.speedTimer.stop() # In any case
        # ThrottleFrames
        if ( evt.wasReleased(WRButtonEvent.RIGHT) ): # NEXT
             self.getContext().nextThrottleFrame()
        if ( evt.wasReleased(WRButtonEvent.LEFT) ):  # PREVIOUS
            self.getContext().previousThrottleFrame()
        if ( evt.wasReleased(WRButtonEvent.UP) ): # NEXT RUNNING
             self.getContext().nextRunningThrottleFrame()
        if ( evt.wasReleased(WRButtonEvent.DOWN) ):  # PREVIOUS RUNNING
            self.getContext().previousRunningThrottleFrame()  
        # No throttle assigned to current frame, browse through roster      
        if (self.throttle == None):
            if (evt.wasReleased(WRButtonEvent.HOME) ):  # Assign selected roster entry
                self.addressPanel.selectRosterEntry()
                return
            if ( evt.wasReleased(WRButtonEvent.PLUS) ):  # Next roster entry
                selectedIndex = self.addressPanel.getRosterSelectedIndex()
                self.addressPanel.setIcon(False)
                self.addressPanel.setVisible(True)
                self.addressPanel.setRosterSelectedIndex(selectedIndex + 1)
                return
            if ( evt.wasReleased(WRButtonEvent.MINUS) ):  # Previous roster entry
                selectedIndex = self.addressPanel.getRosterSelectedIndex()
                self.addressPanel.setIcon(False)
                self.addressPanel.setVisible(True)
                self.addressPanel.setRosterSelectedIndex(selectedIndex - 1)
                return
        # Throttle assigned to current frame, control it  
        if (self.throttle != None):
            # Speed control
            if ( evt.isPressed(WRButtonEvent.B) ): # SPEED - increment
                self.speedAction.setSpeedIncrement( valueSpeedIncrement )
                self.speedTimer.start()
            if ( evt.isPressed(WRButtonEvent.A) ): # SPEED - decrement
                self.speedAction.setSpeedIncrement( -valueSpeedIncrement )
                self.speedTimer.start()
            # EStop
            if ( evt.isPressed( WRButtonEvent.PLUS | WRButtonEvent.MINUS ) ): # estop = + & -
                self.throttle.setSpeedSetting( speedEStopSpeed )
                self.lastTimeEStop = Calendar.getInstance().getTimeInMillis() # To cancel next inputs
                self.wiiDevice.vibrateFor(750)
            # Directions
            if ( evt.wasReleased(WRButtonEvent.PLUS) ):  # FORWARD
                self.throttle.setIsForward(True)
            if ( evt.wasReleased(WRButtonEvent.MINUS) ):  # BACKWARD
                self.throttle.setIsForward(False)
            # Home : F0            
            if ( evt.wasReleased(WRButtonEvent.HOME) ):  # LIGHTS
                if not ((self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "0", False, self.throttle) != None)):
                   self.throttle.setF0( not self.throttle.getF0() )
            # Wiimote 1 & 2 buttons
            if (evt.isPressed(WRButtonEvent.ONE)):
                if not ((self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "1", True, self.throttle) != None)):
                    pass # default F1 not momentary (switch only on Release, do nothing here)
            if (evt.wasReleased(WRButtonEvent.ONE)):
                if not ((self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "1", False, self.throttle) != None)):
                    self.throttle.setF1( not self.throttle.getF1() )  # default F1 not momentary              
            if (evt.isPressed(WRButtonEvent.TWO)):
                if not ((self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "2", True, self.throttle) != None)):
                    self.throttle.setF2( True )  # default F2 momentary
            if (evt.wasReleased(WRButtonEvent.TWO)):
                if not ((self.addressPanel.getRosterEntry() != None) and (self.advFunctions.call(self.addressPanel.getRosterEntry(), "2", False, self.throttle) != None)):
                    self.throttle.setF2( False )

    def disconnected(self):
        self.wiiDevice = None
        print("Lost wiimote")
        
    def accelerationInputReceived(self, evt):
        pass
    def combinedInputReceived(self, evt):
        pass
    def extensionConnected(self, extension):
        pass          
    def extensionDisconnected(self, extension):
        pass          
    def extensionInputReceived(self, evt):
        pass
    def extensionPartiallyInserted(self):
        pass
    def extensionUnknown(self):
        pass
    def IRInputReceived(self, evt):
        pass
    def statusReported(self, evt):
        print("Wiimote status reported: ", evt)

#Property listener part
    def propertyChange(self, event):
        self.speedTimer.stop()                     
        if (event.propertyName == "ThrottleFrame") :  # Current throttle frame changed
            event.oldValue.getAddressPanel().removeAddressListener(self)
            self.addressPanel = event.newValue.getAddressPanel()
            self.throttle = self.addressPanel.getThrottle()
            self.speedAction.setThrottle( self.throttle )
            self.addressPanel.addAddressListener(self)

#AddressListener part: to listen for address changes in address panel (release, acquired)
    def notifyAddressChosen(self, address):
        pass
        
    def notifyAddressThrottleFound(self, throttle):
        self.speedTimer.stop() 
        self.throttle = throttle
        self.speedAction.setThrottle( self.throttle )
            
    def notifyAddressReleased(self, address):
        self.speedTimer.stop()
        self.throttle = None
        self.speedAction.setThrottle( self.throttle )

    def notifyConsistAddressChosen(self, address, isLong):
        self.notifyAddressChosen(address)

    def notifyConsistAddressThrottleFound(self, throttle):
        self.notifyAddressThrottleFound(throttle)

    def notifyConsistAddressReleased(self, address, isLong):
        self.notifyAddressReleased(address)
                
# Speed timer class, to increase speed regularly once button pushed, thread stopped on button release
class SpeedAction(ActionListener):
    def __init__(self):
        self.sync = thread.allocate_lock() # Protects properties getter and setter
        self.speedIncrement = 0
        self.throttle = None

    def setSpeedIncrement(self, si):
        self.sync.acquire()
        self.speedIncrement = si
        self.sync.release()

    def getSpeedIncrement(self):
        self.sync.acquire()
        si = self.speedIncrement
        self.sync.release()
        return si

    def setThrottle(self, throt):
        self.sync.acquire()
        self.throttle = throt
        self.sync.release()
    
    def getThrottle(self):
        self.sync.acquire()
        throt = self.throttle
        self.sync.release()
        return throt

    def actionPerformed(self, e):
        throttle = self.getThrottle()
        spi = self.getSpeedIncrement()
        if (throttle != None) :
            ns =  throttle.getSpeedSetting() + spi
            if (ns < 0 ) :
                ns = 0
            if (ns > 1 ) :
                ns = 1
            throttle.setSpeedSetting( ns )

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
