#
# Use a USB device as a throttle
#
# Author: Lionel Jeanson, adapted from Bob Jacobsen, copyright 2008/2009
#
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

import sys
import thread

import java
import java.awt
import java.awt.event
import java.beans
import java.util
import jmri
import java.beans.PropertyChangeListener as PropertyChangeListener
import java.util.Calendar as Calendar
import javax.swing.ImageIcon as ImageIcon
import javax.swing.JButton as JButton
import javax.swing.JCheckBoxMenuItem as JCheckBoxMenuItem
import javax.swing.Timer as Timer
import jmri.jmrit.jython.Jynstrument as Jynstrument
import jmri.jmrit.throttle.AddressListener as AddressListener
import org.jdom.Element as Element

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
                # Uncomment bellow line to see component name and its value
                # print "Component",component,"value changed to",value
                try:
                    # Change current ThrottleFrame
                    if ((component == self.driver.componentNextThrottleFrame) and (value == self.driver.valueNextThrottleFrame)) : #NEXT
                        self.getContext().nextThrottleFrame()
                    if  ((component == self.driver.componentPreviousThrottleFrame) and (value == self.driver.valuePreviousThrottleFrame)) : #PREVIOUS
                        self.getContext().previousThrottleFrame()
                    if ((component == self.driver.componentNextRunningThrottleFrame) and (value == self.driver.valueNextRunningThrottleFrame)) : #NEXT RUNNING
                        self.getContext().nextRunningThrottleFrame()
                    if  ((component == self.driver.componentPreviousRunningThrottleFrame) and (value == self.driver.valuePreviousRunningThrottleFrame)) : #PREVIOUS RUNNING
                        self.getContext().previousRunningThrottleFrame()  
                except AttributeError:
                    pass
                
                if (self.throttle == None) :
                    try:
                        # Browse through roster
                        if ((component == self.driver.componentNextRosterBrowse) and (value == self.driver.valueNextRoster)): #NEXT
                            selectedIndex = self.addressPanel.getRosterSelectedIndex()
                            self.addressPanel.setVisible(True)
                            self.addressPanel.setIcon(False)
                            self.addressPanel.setRosterSelectedIndex(selectedIndex + 1)
                        if ((component == self.driver.componentPreviousRosterBrowse) and (value == self.driver.valuePreviousRoster)) : #PREVIOUS
                            selectedIndex = self.addressPanel.getRosterSelectedIndex()
                            self.addressPanel.setVisible(True)
                            self.addressPanel.setIcon(False)                            
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
                        # Speed - dynamic controler (joystick going back to neutral position)
                        if ((component == self.driver.componentSpeedIncrease) or (component == self.driver.componentSpeedDecrease) or (component == self.driver.componentSpeed)):
                            if ((component == self.driver.componentSpeedIncrease) and (value == self.driver.valueSpeedIncrease)) :
                                self.speedAction.setSpeedIncrement( 0.03 )
                            if ((component == self.driver.componentSpeedDecrease) and (value == self.driver.valueSpeedDecrease)) :
                                self.speedAction.setSpeedIncrement( -0.03 )
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
                    except AttributeError:
                        self.speedTimer.stop() # just in case, stop it, really should never get there
                    
                    try:
                        # Speed v2 - static controler (lever on RailDriver or AAR105)
                        if (component == self.driver.componentSpeedSet):
                            # negative is lever front, positive is lever back
                            # limit range to only positive side of lever
                            if (value < self.driver.valueSpeedSetMinValue) : value = self.driver.valueSpeedSetMinValue
                            if (value > self.driver.valueSpeedSetMaxValue) : value = self.driver.valueSpeedSetMaxValue
                            # convert fraction of input to speed step
                            self.throttle.setSpeedSetting((value-self.driver.valueSpeedSetMinValue)/(self.driver.valueSpeedSetMaxValue-self.driver.valueSpeedSetMinValue))
                            print "Slider Speed:", self.controlPanel.getDisplaySlider()
                    except AttributeError:
                        pass
                    # Direction
                    try:
                        if ((component == self.driver.componentDirectionForward) and (value == self.driver.valueDirectionForward)) :
                            self.throttle.setIsForward(True)
                        if ((component == self.driver.componentDirectionBackward) and (value == self.driver.valueDirectionBackward)) :
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
                        if ((component == self.driver.componentEStopSpeed) and (value == self.driver.valueEStopSpeed)):
                            self.throttle.setSpeedSetting(EStopSpeed)
                    except AttributeError:
                        pass
                    try:   # EStop
                        if ((component == self.driver.componentEStopSpeedBis) and (value == self.driver.valueEStopSpeedBis)):
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
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(0)) and (component == self.driver.componentF0) and (value == self.driver.valueF0Off)) :
                            self.throttle.setF0( not self.throttle.getF0() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF1) and (value == self.driver.valueF1)) :
                            self.throttle.setF1( not self.throttle.getF1() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(1)) and (component == self.driver.componentF1) and (value == self.driver.valueF1Off)) :
                            self.throttle.setF1( not self.throttle.getF1() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF2) and (value == self.driver.valueF2)) :
                            self.throttle.setF2( not self.throttle.getF2() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(2)) and (component == self.driver.componentF2) and (value == self.driver.valueF2Off)) :
                            self.throttle.setF2( not self.throttle.getF2() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF3) and (value == self.driver.valueF3)) :
                            self.throttle.setF3( not self.throttle.getF3() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(3)) and (component == self.driver.componentF3) and (value == self.driver.valueF3Off)) :
                            self.throttle.setF3( not self.throttle.getF3() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF4) and (value == self.driver.valueF4)) :
                            self.throttle.setF4( not self.throttle.getF4() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(4)) and (component == self.driver.componentF4) and (value == self.driver.valueF4Off)) :
                            self.throttle.setF4( not self.throttle.getF4() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF5) and (value == self.driver.valueF5)) :
                            self.throttle.setF5( not self.throttle.getF5() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(5)) and (component == self.driver.componentF5) and (value == self.driver.valueF5Off)) :
                            self.throttle.setF5( not self.throttle.getF5() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF6) and (value == self.driver.valueF6)) :
                            self.throttle.setF6( not self.throttle.getF6() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(6)) and (component == self.driver.componentF6) and (value == self.driver.valueF6Off)) :
                            self.throttle.setF6( not self.throttle.getF6() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF7) and (value == self.driver.valueF7)) :
                            self.throttle.setF7( not self.throttle.getF7() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(7)) and (component == self.driver.componentF7) and (value == self.driver.valueF7Off)) :
                            self.throttle.setF7( not self.throttle.getF7() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF8) and (value == self.driver.valueF8)) :
                            self.throttle.setF8( not self.throttle.getF8() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(8)) and (component == self.driver.componentF8) and (value == self.driver.valueF8Off)) :
                            self.throttle.setF8( not self.throttle.getF8() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF9) and (value == self.driver.valueF9)) :
                            self.throttle.setF9( not self.throttle.getF9() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(9)) and (component == self.driver.componentF9) and (value == self.driver.valueF9Off)) :
                            self.throttle.setF9( not self.throttle.getF9() )
                    except AttributeError:
                        pass  
                    try:
                        if ((component == self.driver.componentF10) and (value == self.driver.valueF10)) :
                            self.throttle.setF10( not self.throttle.getF10() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(10)) and (component == self.driver.componentF10) and (value == self.driver.valueF10Off)) :
                            self.throttle.setF10( not self.throttle.getF10() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF11) and (value == self.driver.valueF11)) :
                            self.throttle.setF11( not self.throttle.getF11() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(11)) and (component == self.driver.componentF11) and (value == self.driver.valueF11Off)) :
                            self.throttle.setF11( not self.throttle.getF11() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF12) and (value == self.driver.valueF12)) :
                            self.throttle.setF12( not self.throttle.getF12() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(12)) and (component == self.driver.componentF12) and (value == self.driver.valueF12Off)) :
                            self.throttle.setF12( not self.throttle.getF12() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF13) and (value == self.driver.valueF13)) :
                            self.throttle.setF13( not self.throttle.getF13() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(13)) and (component == self.driver.componentF13) and (value == self.driver.valueF13Off)) :
                            self.throttle.setF13( not self.throttle.getF13() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF14) and (value == self.driver.valueF14)) :
                            self.throttle.setF14( not self.throttle.getF14() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(14)) and (component == self.driver.componentF14) and (value == self.driver.valueF14Off)) :
                            self.throttle.setF14( not self.throttle.getF14() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF15) and (value == self.driver.valueF15)) :
                            self.throttle.setF15( not self.throttle.getF15() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(15)) and (component == self.driver.componentF15) and (value == self.driver.valueF15Off)) :
                            self.throttle.setF15( not self.throttle.getF15() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF16) and (value == self.driver.valueF16)) :
                            self.throttle.setF16( not self.throttle.getF16() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(16)) and (component == self.driver.componentF16) and (value == self.driver.valueF16Off)) :
                            self.throttle.setF16( not self.throttle.getF16() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF17) and (value == self.driver.valueF17)) :
                            self.throttle.setF17( not self.throttle.getF17() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(17)) and (component == self.driver.componentF17) and (value == self.driver.valueF17Off)) :
                            self.throttle.setF17( not self.throttle.getF17() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF18) and (value == self.driver.valueF18)) :
                            self.throttle.setF18( not self.throttle.getF18() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(18)) and (component == self.driver.componentF18) and (value == self.driver.valueF18Off)) :
                            self.throttle.setF18( not self.throttle.getF18() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF19) and (value == self.driver.valueF19)) :
                            self.throttle.setF19( not self.throttle.getF19() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(19)) and (component == self.driver.componentF19) and (value == self.driver.valueF19Off)) :
                            self.throttle.setF19( not self.throttle.getF19() )
                    except AttributeError:
                        pass   
                    
                    try:
                        if ((component == self.driver.componentF20) and (value == self.driver.valueF20)) :
                            self.throttle.setF20( not self.throttle.getF20() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(20)) and (component == self.driver.componentF20) and (value == self.driver.valueF20Off)) :
                            self.throttle.setF20( not self.throttle.getF20() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF21) and (value == self.driver.valueF21)) :
                            self.throttle.setF21( not self.throttle.getF21() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(21)) and (component == self.driver.componentF21) and (value == self.driver.valueF21Off)) :
                            self.throttle.setF21( not self.throttle.getF21() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF22) and (value == self.driver.valueF22)) :
                            self.throttle.setF22( not self.throttle.getF22() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(22)) and (component == self.driver.componentF22) and (value == self.driver.valueF22Off)) :
                            self.throttle.setF22( not self.throttle.getF22() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF23) and (value == self.driver.valueF23)) :
                            self.throttle.setF23( not self.throttle.getF23() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(23)) and (component == self.driver.componentF23) and (value == self.driver.valueF23Off)) :
                            self.throttle.setF23( not self.throttle.getF23() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF24) and (value == self.driver.valueF24)) :
                            self.throttle.setF24( not self.throttle.getF24() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(24)) and (component == self.driver.componentF24) and (value == self.driver.valueF24Off)) :
                            self.throttle.setF24( not self.throttle.getF24() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF25) and (value == self.driver.valueF25)) :
                            self.throttle.setF25( not self.throttle.getF25() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(25)) and (component == self.driver.componentF25) and (value == self.driver.valueF25Off)) :
                            self.throttle.setF25( not self.throttle.getF25() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF26) and (value == self.driver.valueF26)) :
                            self.throttle.setF26( not self.throttle.getF26() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(26)) and (component == self.driver.componentF26) and (value == self.driver.valueF26Off)) :
                            self.throttle.setF26( not self.throttle.getF26() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF27) and (value == self.driver.valueF27)) :
                            self.throttle.setF27( not self.throttle.getF27() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(27)) and (component == self.driver.componentF27) and (value == self.driver.valueF27Off)) :
                            self.throttle.setF27( not self.throttle.getF27() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF28) and (value == self.driver.valueF28)) :
                            self.throttle.setF28( not self.throttle.getF28() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(28)) and (component == self.driver.componentF28) and (value == self.driver.valueF28Off)) :
                            self.throttle.setF28( not self.throttle.getF28() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF29) and (value == self.driver.valueF29)) :
                            self.throttle.setF29( not self.throttle.getF29() )
                        if ((self.roster != None) and (not self.roster.getFunctionLockable(29)) and (component == self.driver.componentF29) and (value == self.driver.valueF29Off)) :
                            self.throttle.setF29( not self.throttle.getF29() )
                    except AttributeError:
                        pass
                        
        # Nothing to customize bellow this point
        if (event.propertyName == "ThrottleFrame") :  # Current throttle frame changed
            self.speedTimer.stop()
            event.oldValue.getAddressPanel().removeAddressListener(self)
            self.addressPanel = event.newValue.getAddressPanel()
            self.throttle = self.addressPanel.getThrottle()
            self.roster = self.addressPanel.getRosterEntry()
            self.speedAction.setThrottle( self.throttle )
            event.newValue.getAddressPanel().addAddressListener(self)

#Jynstrument main and mandatory methods
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleWindow"
    
    def init(self):
        self.getContext().addPropertyChangeListener(self) #ThrottleFrame change
        self.getContext().getCurrentThrottleFrame().getAddressPanel().addAddressListener(self) # change of throttle in Current frame
        self.addressPanel = self.getContext().getCurrentThrottleFrame().getAddressPanel()
        self.throttle = self.addressPanel.getThrottle() # the throttle
        self.roster = self.addressPanel.getRosterEntry() # roster entry if any
        self.speedAction =  SpeedAction()  #Speed increase thread
        self.speedAction.setThrottle( self.throttle )
        self.speedTimer = Timer(valueSpeedTimerRepeat, self.speedAction ) # Very important to use swing Timer object (see Swing and multithreading doc)
        self.speedTimer.setRepeats(True)
        self.label = JButton(ImageIcon(self.getFolder() + "/USBControl.png","USBThrottle")) #label
        self.label.addMouseListener(self.getMouseListeners()[0]) # In order to get the popupmenu on the button too
        self.add(self.label)
        self.model = jmri.jmrix.jinput.TreeModel.instance() # USB
        self.desiredController = None
        self.ctrlMenuItem = []
        self.USBDriver = None
        self.driver = None
        mi = JCheckBoxMenuItem ("None")
        self.getPopUpMenu().add( mi )
        mi.addItemListener( ControllerItemListener(None, self) )
        self.ctrlMenuItem.append(mi)
        for ctrl in self.model.controllers(): 
            mi = JCheckBoxMenuItem (ctrl.getName())
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
        self.getContext().getCurrentThrottleFrame().getAddressPanel().removeAddressListener(self)

# Menu entry changed for Current controller and update driver
    def setSelectedController(self, ctrl, item):
        for mi in self.ctrlMenuItem :
            if ( mi != item ):  # Force deselection of other ones
                mi.setSelected(False)
        self.desiredController = ctrl
        if (ctrl != None) :
            sys.path.append(self.getFolder()) # Load a driver
            try:
                del self.driver
                del self.USBDriver
                dd=ctrl.getName()
                dd=dd.replace(" ", "")
                dd=dd.replace(".", "")
                dd=dd.replace("(", "")
                dd=dd.replace(")", "")
                dd=dd.replace("{", "")
                dd=dd.replace("}", "")
                dd=dd.replace("[", "")
                dd=dd.replace("]", "")              
                self.USBDriver = __import__(dd)           
            except ImportError:  # On error load a default one
                print "Driver \""+ dd +"\" not found in \""+self.getFolder()+"\", loading default one"
                self.USBDriver =  __import__("Default")
            reload(self.USBDriver)
            sys.path.remove(self.getFolder())
            self.driver = self.USBDriver.USBDriver()
    
    def setXml(self, elt):
        if (elt.getChildren("USBThrottle") == None):
            return
        ctrl = elt.getChildren("USBThrottle")[0].getAttributeValue("DesiredController")
        if (ctrl == None):
            return
        for mi in self.ctrlMenuItem :
            if ( mi.getText() == ctrl ):
                mi.setSelected(True)
                break
    
    def getXml(self):
       elt = Element("USBThrottle")
       for mi in self.ctrlMenuItem :
           if (mi.isSelected()) :
               elt.setAttribute("DesiredController", mi.getText())
               break
       return elt

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
