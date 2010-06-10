#
# Use a USB device as a throttle
## Author: Lionel Jeanson, adapted from Bob Jacobsen, copyright 2008/2009
# Part of the JMRI distribution
#
# The list of available devices is accessible by right clicking on the Jysntrument icon once started.
#
# You want to have a look at the driver file this script will look for you hardware
# If required; copy default.py to XYZ.py where XYZ is from the line "Driver "XYZ" not found, loading default one" from the trace
# Then customize your XYZ.py to match your device layout 
# Use JMRI Debug->USB Input Control to check each device component name and possible values
#
# The customizable part bellow are the throttles calls
#

import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.beans.PropertyChangeListener as PropertyChangeListener
import jmri.jmrit.throttle.AddressListener as AddressListener
import javax.swing.Timer as Timer
import java.util.Calendar as Calendar
import thread
import sys
import net.java.games.input.Controller as Controller
import javax.swing.JCheckBoxMenuItem as JCheckBoxMenuItem
import javax.swing.JButton as JButton
import javax.swing.ImageIcon as ImageIcon

# Some default speeds that will be used in the program
speedMaxSpeed = 1
speedCruiseSpeed = 0.8   # this one is customizable
speedSlowSpeed = 0.3     # that one too
speedStopSpeed = 0
EStopSpeed = -1

# Some constants used by the program
valueSpeedTimerRepeat = 200 # repeat time in ms for speed set task
valueSpeedDivider = 10      # a divider for the value given by the pad, then used to increment speed
delay4doubleTap = 250       # max delay in ms for double tap => EStop (on 2xStop button) and MaxSpeed (on 2xCruise button)

class USBThrottle(Jynstrument, PropertyChangeListener, AddressListener):
#Property listener part: USB value
    def propertyChange(self, event):
        # Customize bellow for throttles calls:
        if (event.propertyName == "Value") :  # USB
            if (event.oldValue.getController() == self.desiredController ) :
                component = event.oldValue.getComponent().toString()
                value = event.newValue

                try:
                    # Change current ThrottleFrame
                    if (component == self.driver.componentThrottleFrame) :
                        if (value == self.driver.valueNextThrottleFrame) : #NEXT
                            self.getContext().nextThrottleFrame()
                        if (value == self.driver.valuePreviousThrottleFrame) : #PREVIOUS
                            self.getContext().previousThrottleFrame()
                except AttributeError:
                    pass
                
                if (self.throttle == None) :
                    try:
                        # Browse through roster
                        if (component == self.driver.componentRosterBrowse) :
                            selectedIndex = self.addressPanel.getRosterSelectedIndex()
                            self.addressPanel.setVisible(True)
                            self.addressPanel.setIcon(False)
                            if (value == self.driver.valueNextRoster) : #NEXT
                                self.addressPanel.setRosterSelectedIndex(selectedIndex + 1)
                            if (value == self.driver.valuePreviousRoster) : #PREVIOUS
                                self.addressPanel.setRosterSelectedIndex(selectedIndex - 1)
                    except AttributeError:
                        pass
                    try:
                        # Request a throttle
                        if ((component == self.driver.componentRosterSelect) and (value == self.driver.valueRosterSelect)):
                            self.addressPanel.selectRosterEntry()
                    except AttributeError:
                        pass
                        
                # From there; current throttle control, hence require a throttle
                if (self.throttle != None) :
                    # Release current throttle
                    try:
                        if ((component == self.driver.componentThrottleRelease) and (value == self.driver.valueThrottleRelease)):
                            self.addressPanel.dispatchAddress()
                    except AttributeError:
                        pass
                    
                    try:
                        # Speed
                        if (component == self.driver.componentSpeed) :
                            try:
                                self.vsd = valueSpeedDivider * self.driver.componentSpeedMultiplier
                            except AttributeError:
                                self.vsd = valueSpeedDivider
                            self.speedAction.setSpeedIncrement(value / self.vsd)
                            if ( abs(value) > self.driver.valueSpeedTrigger ) :
                                self.speedTimer.start()
                            else :
                                self.speedTimer.stop()
                        else :
                            self.speedTimer.stop() # just in case, stop it
                    except AttributeError:
                        self.speedTimer.stop() # just in case, stop it, really should never get there
                        
                    # Direction
                    try:
                        if (component == self.driver.componentDirection) :
                            if (value == self.driver.valueDirectionForward) :
                                self.throttle.setIsForward(True)
                            if (value == self.driver.valueDirectionBackward) :
                                self.throttle.setIsForward(False)
                    except AttributeError:
                        pass                    
                    try:                    
                        if ((component == self.driver.componentDirectionSwitch) and (value == self.driver.valueDirectionSwitch)) :
                            self.throttle.setIsForward(not self.throttle.getIsForward())
                    except AttributeError:
                        pass
                    
                    # Speed presets
                    try:  # STOP
                        if ((component == self.driver.componentStopSpeed) and (value == self.driver.valueStopSpeed)) :
                            if ( Calendar.getInstance().getTimeInMillis() - self.lastTimeStopButton < delay4doubleTap ) : 
                                self.throttle.setSpeedSetting(EStopSpeed) # EStop on double tap
                            else:
                                self.throttle.setSpeedSetting(speedStopSpeed)
                            self.lastTimeStopButton = Calendar.getInstance().getTimeInMillis()
                    except AttributeError:
                        pass
                    try:   # EStop
                        if ((component == self.driver.componentEStopSpeed) and (value == self.driver.valueEStopSpeed)) :
                            self.throttle.setSpeedSetting(EStopSpeed)
                    except AttributeError:
                        pass
                    try:   # SLOW
                        if ((component == self.driver.componentSlowSpeed) and (value == self.driver.valueSlowSpeed)) :
                            self.throttle.setSpeedSetting(speedSlowSpeed)
                    except AttributeError:
                        pass
                    try:   # CRUISE
                        if ((component == self.driver.componentCruiseSpeed) and (value == self.driver.valueCruiseSpeed)) :
                            if ( Calendar.getInstance().getTimeInMillis() - self.lastTimeCruiseButton < delay4doubleTap ) : # EStop on double tap
                                self.throttle.setSpeedSetting(speedMaxSpeed) # Max speed on double tap
                            else:
                                self.throttle.setSpeedSetting(speedCruiseSpeed)
                            self.lastTimeCruiseButton = Calendar.getInstance().getTimeInMillis()
                    except AttributeError:
                        pass
                    try:   # MAX
                        if ((component == self.driver.componentMaxSpeed) and (value == self.driver.valueMaxSpeed)) :
                            self.throttle.setSpeedSetting(speedMaxSpeed)
                    except AttributeError:
                        pass
                        
                    # Functions
                    try:
                        if ((component == self.driver.componentF0) and (value == self.driver.valueF0)) :
                            self.throttle.setF0( not self.throttle.getF0() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF1) and (value == self.driver.valueF1)) :
                            self.throttle.setF1( not self.throttle.getF1() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF2) and (value == self.driver.valueF2)) :
                            self.throttle.setF2( not self.throttle.getF2() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF3) and (value == self.driver.valueF3)) :
                            self.throttle.setF3( not self.throttle.getF3() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF4) and (value == self.driver.valueF4)) :
                            self.throttle.setF4( not self.throttle.getF4() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF5) and (value == self.driver.valueF5)) :
                            self.throttle.setF5( not self.throttle.getF5() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF6) and (value == self.driver.valueF6)) :
                            self.throttle.setF6( not self.throttle.getF6() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF7) and (value == self.driver.valueF7)) :
                            self.throttle.setF7( not self.throttle.getF7() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF8) and (value == self.driver.valueF8)) :
                            self.throttle.setF8( not self.throttle.getF8() )
                    except AttributeError:
                        pass
                        
        # Nothing to customize bellow this point
        if (event.propertyName == "ThrottleFrame") :  # Curent throttle frame changed
            self.speedTimer.stop()
            event.oldValue.getAddressPanel().removeAddressListener(self)
            self.throttle = event.newValue.getAddressPanel().getThrottle()
            self.addressPanel = event.newValue.getAddressPanel()
            self.speedAction.setThrottle( self.throttle )
            event.newValue.getAddressPanel().addAddressListener(self)

#Jynstrument main and mandatory methods
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleWindow"
    
    def init(self):
        self.getContext().addPropertyChangeListener(self) #ThrottleFrame change
        self.getContext().getCurentThrottleFrame().getAddressPanel().addAddressListener(self) # change of throttle in curent frame
        self.throttle = self.getContext().getCurentThrottleFrame().getAddressPanel().getThrottle() # the throttle
        self.addressPanel = self.getContext().getCurentThrottleFrame().getAddressPanel()
        self.speedAction =  SpeedAction()  #Speed increase thread
        self.speedAction.setThrottle( self.throttle )
        self.speedTimer = Timer(valueSpeedTimerRepeat, self.speedAction ) # Very important to use swing Timer object (see Swing and multithreading doc)
        self.speedTimer.setRepeats(True)
        self.label = JButton(ImageIcon(self.getFolder() + "/USBControl.png","WiiMote")) #label
        self.label.addMouseListener(self.getMouseListeners()[0]) # In order to get the popupmenu on the button too
        self.add(self.label)
        self.model = jmri.jmrix.jinput.TreeModel.instance() # USB
        self.desiredController = None
        self.ctrlMenuItem = []
        self.USBDriver = None
        self.driver = None
        for ctrl in self.model.controllers(): # The selection bellow might have to be modified
            if ((ctrl.getType() == Controller.Type.GAMEPAD) or (ctrl.getType() == Controller.Type.STICK)) :
                mi =  JCheckBoxMenuItem (ctrl.getName())
                self.getPopUpMenu().add( mi )
                mi.addItemListener( ControllerItemListener(ctrl, self) )
                self.ctrlMenuItem.append(mi)
        if ( len(self.ctrlMenuItem) == 0 ):
            print "No matching USB device found"
        else:
            self.ctrlMenuItem[0].setSelected(True)  # by default connect to the first one
        self.model.addPropertyChangeListener(self)
        self.lastTimeStopButton = Calendar.getInstance().getTimeInMillis()
        self.lastTimeCruiseButton = Calendar.getInstance().getTimeInMillis()

# On quit clean up resources       
    def quit(self):
        self.speedTimer.stop()
        for mi in self.ctrlMenuItem :
            self.getPopUpMenu().remove( mi )
        self.ctrlMenuItem = None
        self.speedAction = None
        self.speedTimer = None
        self.throttle = None
        self.addressPanel = None
        self.driver = None
        self.USBDriver = None
        self.getContext().removePropertyChangeListener(self)
        self.model.removePropertyChangeListener(self)
        self.getContext().getCurentThrottleFrame().getAddressPanel().removeAddressListener(self)

# Menu entry changed for curent controller and update driver
    def setSelectedController(self, ctrl, item):
        for mi in self.ctrlMenuItem :
            if ( mi != item ):  # Force deselection of other ones
                mi.setSelected(False)
        self.desiredController = ctrl
        sys.path.append(self.getFolder()) # Load a driver
        try:
            del self.driver
            del self.USBDriver
            dd=ctrl.getName()
            dd=dd.replace(" ", "")
            dd=dd.replace(".", "")              
            self.USBDriver = __import__(dd)           
        except ImportError:  # On error load a default one
            print "Driver  \""+ dd +"\" not found, loading default one"
            self.USBDriver =  __import__("Default")
        reload(self.USBDriver)
        sys.path.remove(self.getFolder())
        self.driver = self.USBDriver.USBDriver()

#AddressListener part: to listen for address changes in address panel (release, acquired)
    def notifyAddressChosen(self, address, isLong):
        pass
        
    def notifyAddressThrottleFound(self, throttle):
        self.speedTimer.stop() 
        self.throttle = throttle
        self.speedAction.setThrottle( self.throttle )
            
    def notifyAddressReleased(self, address, isLong):
        self.speedTimer.stop()
        self.throttle = None
        self.speedAction.setThrottle( self.throttle )

# Item listeners for the PopUp menu
class ControllerItemListener( java.awt.event.ItemListener):
    def __init__(self, ctrl, jyns):
        self.ctrl = ctrl
        self.jyns = jyns

    def itemStateChanged(self, evt):
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED ):
            self.jyns.setSelectedController( self.ctrl, evt.getItem() )		
                
# Speed timer class, to increase speed regularly once button pushed, thread stopped on button release
class SpeedAction( java.awt.event.ActionListener):
    def __init__(self):
        self.sync = thread.allocate_lock() # Protects properties getters and setters
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
