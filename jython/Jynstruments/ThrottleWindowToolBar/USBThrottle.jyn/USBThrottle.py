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
import jmri
import java.util.Calendar as Calendar
import javax.swing.ImageIcon as ImageIcon
import javax.swing.JButton as JButton
import javax.swing.JCheckBoxMenuItem as JCheckBoxMenuItem
import java.util.TimerTask as TimerTask
import java.util.Timer as Timer
import jmri.jmrit.jython.Jynstrument as Jynstrument
import jmri.util.ThreadingUtil as ThreadingUtil
import org.jdom2.Element as Element
import java.beans.PropertyChangeListener as PropertyChangeListener

# Some default speeds that will be used in the program
speedMaxSpeed = 1
speedCruiseSpeed = 0.8   # this one is customizable
speedSlowSpeed = 0.3     # that one too
speedStopSpeed = 0
EStopSpeed = -1

# Some constants used by the program
valueSpeedTimerRepeat = 100 # repeat time in ms for speed set task
valueSpeedDivider = 15       # a divider for the value given by the pad, then used to increment speed
delay4doubleTap = 250       # max delay in ms for double tap => EStop (on 2xStop button) and MaxSpeed (on 2xCruise button)

class USBThrottle(Jynstrument, PropertyChangeListener):
#Property listener part: USB value
    def propertyChange(self, event):        
        # Customize bellow for throttles calls:        
        if (event.propertyName == "Value") :  # USB
            if (event.oldValue.getController() == self.desiredController ) :
#                print "Value change event from controller "+event.toString()
                component = event.oldValue.getComponent().toString()
                value = event.newValue
                throttle = None
                roster = None
                if (self.getContext().getCurentThrottleController() != None) :
                    throttle = self.getContext().getCurentThrottleController().getThrottle()
                    fnThrottle = self.getContext().getCurentThrottleController().getFunctionThrottle()
                    roster = self.getContext().getCurentThrottleController().getFunctionRosterEntry()
                else:
                    return # no throttle, no control, exit
                # Uncomment bellow line to see component name and its value
#                print "Component \""+component+"\" value changed to ",value
                try:
                    # Change current ThrottleFrame
                    if ((component == self.driver.componentNextThrottleFrame) and (value == self.driver.valueNextThrottleFrame)) : #NEXT
                        ThreadingUtil.runOnGUI(lambda : self.getContext().nextThrottleFrame() )
                    if  ((component == self.driver.componentPreviousThrottleFrame) and (value == self.driver.valuePreviousThrottleFrame)) : #PREVIOUS
                        ThreadingUtil.runOnGUI(lambda : self.getContext().previousThrottleFrame() )
                    if ((component == self.driver.componentNextRunningThrottleFrame) and (value == self.driver.valueNextRunningThrottleFrame)) : #NEXT RUNNING
                        ThreadingUtil.runOnGUI(lambda : self.getContext().nextRunningThrottleFrame() )
                    if  ((component == self.driver.componentPreviousRunningThrottleFrame) and (value == self.driver.valuePreviousRunningThrottleFrame)) : #PREVIOUS RUNNING
                        ThreadingUtil.runOnGUI(lambda : self.getContext().previousRunningThrottleFrame() )
                except AttributeError:
                    pass
                
                if (throttle == None):
                    try:
                        # Browse through roster
                        if ((component == self.driver.componentNextRosterBrowse) and (value == self.driver.valueNextRoster)): #NEXT
                            ThreadingUtil.runOnGUI(lambda : self.getContext().getCurentThrottleController().getRosterEntrySelector().setRosterListSelectedIndex(self.getContext().getCurentThrottleController().getRosterEntrySelector().getRosterListSelectedIndex() + 1) )
                        if ((component == self.driver.componentPreviousRosterBrowse) and (value == self.driver.valuePreviousRoster)) : #PREVIOUS
                            ThreadingUtil.runOnGUI(lambda : self.getContext().getCurentThrottleController().getRosterEntrySelector().setRosterListSelectedIndex(self.getContext().getCurentThrottleController().getRosterEntrySelector().getRosterListSelectedIndex() - 1) )
                    except AttributeError:
                        pass
                    try:
                        # Request a throttle
                        if ((component == self.driver.componentRosterSelect) and (value == self.driver.valueRosterSelect)):
                            ThreadingUtil.runOnGUI(lambda : self.getContext().getCurentThrottleController().getRosterEntrySelector().setSelectedRosterEntry() )
                    except AttributeError:
                        pass

                # From there; current throttle control, hence require a throttle
                if (throttle != None) :
                    # Release current throttle
                    try:
                        if ((component == self.driver.componentThrottleRelease) and (value == self.driver.valueThrottleRelease)):
                            ThreadingUtil.runOnGUI(lambda : self.getContext().getCurentThrottleController().dispatchAddress() )
                    except AttributeError:
                        pass                    
                    try:
                        # Speed - dynamic controler (joystick going back to neutral position)
                        self.speedTimerTask.setThrottle( throttle )
                        if ((component == self.driver.componentSpeedIncrease) or (component == self.driver.componentSpeedDecrease) or (component == self.driver.componentSpeed)):
                            if ((component == self.driver.componentSpeedIncrease) and (value == self.driver.valueSpeedIncrease)) :
                                self.speedTimerTask.setSpeedIncrement( 0.03 )
                            if ((component == self.driver.componentSpeedDecrease) and (value == self.driver.valueSpeedDecrease)) :
                                self.speedTimerTask.setSpeedIncrement( -0.03 )
                            if (component == self.driver.componentSpeed) :
                                try:
                                    self.vsd = valueSpeedDivider * self.driver.componentSpeedMultiplier
                                except AttributeError:
                                    self.vsd = valueSpeedDivider
                                self.speedTimerTask.setSpeedIncrement(value / self.vsd)
                            if ( abs(value) > self.driver.valueSpeedTrigger ) :                                
                                self.speedTimerTask.resume()                                
                            else :
                                self.speedTimerTask.pause()
                    except AttributeError:
                        self.speedTimerTask.pause() # just in case, stop it, really should never get there
                    
#                    try:
#                        # Speed v2 - static controler (lever on RailDriver or AAR105)
#                        if (component == self.driver.componentSpeedSet):
#                            setSpeed = value * self.driver.componentValueSpeedMaxForward * self.driver.componentSpeedMaxForward
#                            if ((setSpeed > 0) and (self.isReversed)) : # it was previously going backward
#                                throttle.setIsForward(not throttle.getIsForward())
#                                self.isReversed = False
#                            if (setSpeed == -0) : setSpeed = 0 # avoid neg 0
#                            if (setSpeed < 0):  # going backward
#                                setSpeed = value * self.driver.componentValueSpeedMaxReverse * self.driver.componentSpeedMaxReverse
#                                if (not self.isReversed): # going backward for the first time
#                                    throttle.setIsForward(not throttle.getIsForward())
#                                    self.isReversed = True                            
#                            throttle.setSpeedSetting(setSpeed)
#                    except AttributeError:                        
#                        pass
                    # Direction
                    try:
                        if ((component == self.driver.componentDirectionForward) and (value == self.driver.valueDirectionForward)) :
                            throttle.setIsForward(True)
                        if ((component == self.driver.componentDirectionBackward) and (value == self.driver.valueDirectionBackward)) :
                            throttle.setIsForward(False)
                    except AttributeError:
                        pass                    
                    try:                    
                        if ((component == self.driver.componentDirectionSwitch) and (value == self.driver.valueDirectionSwitch)) :
                            throttle.setIsForward(not throttle.getIsForward())
                    except AttributeError:
                        pass
                    
                    # Speed presets
                    try:  # STOP
                        if ((component == self.driver.componentStopSpeed) and (value == self.driver.valueStopSpeed)) :
                            if ( Calendar.getInstance().getTimeInMillis() - self.lastTimeStopButton < delay4doubleTap ) : 
                                throttle.setSpeedSetting(EStopSpeed) # EStop on double tap
                            else:
                                throttle.setSpeedSetting(speedStopSpeed)
                            self.lastTimeStopButton = Calendar.getInstance().getTimeInMillis()
                    except AttributeError:
                        pass
                    try:   # EStop
                        if ((component == self.driver.componentEStopSpeed) and (value == self.driver.valueEStopSpeed)):
                            throttle.setSpeedSetting(EStopSpeed)
                    except AttributeError:
                        pass
                    try:   # EStop
                        if ((component == self.driver.componentEStopSpeedBis) and (value == self.driver.valueEStopSpeedBis)):
                            throttle.setSpeedSetting(EStopSpeed)
                    except AttributeError:
                        pass
                    try:   # SLOW
                        if ((component == self.driver.componentSlowSpeed) and (value == self.driver.valueSlowSpeed)) :
                            throttle.setSpeedSetting(speedSlowSpeed)
                    except AttributeError:
                        pass
                    try:   # CRUISE
                        if ((component == self.driver.componentCruiseSpeed) and (value == self.driver.valueCruiseSpeed)) :
                            if ( Calendar.getInstance().getTimeInMillis() - self.lastTimeCruiseButton < delay4doubleTap ) : # EStop on double tap
                                throttle.setSpeedSetting(speedMaxSpeed) # Max speed on double tap
                            else:
                                if (self.driver.cruiseSpeed == None) :
                                    throttle.setSpeedSetting(speedCruiseSpeed)
                                else:
                                    throttle.setSpeedSetting(self.driver.cruiseSpeed)
                            self.lastTimeCruiseButton = Calendar.getInstance().getTimeInMillis()
                    except AttributeError:
                        pass
                    try:   # MAX
                        if ((component == self.driver.componentMaxSpeed) and (value == self.driver.valueMaxSpeed)) :
                            throttle.setSpeedSetting(speedMaxSpeed)
                    except AttributeError:
                        pass
                        
                    # Functions
                    if (fnThrottle == None):
                        fnThrottle = throttle
#                    print "Using ",fnThrottle.getLocoAddress()," for functions"
                    try:
                        if ((component == self.driver.componentF0) and (value == self.driver.valueF0)) :
                            fnThrottle.setF0( not fnThrottle.getF0() )
                        if ((roster != None) and (not roster.getFunctionLockable(0)) and (component == self.driver.componentF0) and (value == self.driver.valueF0Off)) :
                            fnThrottle.setF0( not fnThrottle.getF0() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF1) and (value == self.driver.valueF1)) :
                            fnThrottle.setF1( not fnThrottle.getF1() )
                        if ((roster != None) and (not roster.getFunctionLockable(1)) and (component == self.driver.componentF1) and (value == self.driver.valueF1Off)) :
                            fnThrottle.setF1( not fnThrottle.getF1() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF2) and (value == self.driver.valueF2)) :
                            fnThrottle.setF2( not fnThrottle.getF2() )
                        if ((roster != None) and (not roster.getFunctionLockable(2)) and (component == self.driver.componentF2) and (value == self.driver.valueF2Off)) :
                            fnThrottle.setF2( not fnThrottle.getF2() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF3) and (value == self.driver.valueF3)) :
                            fnThrottle.setF3( not fnThrottle.getF3() )
                        if ((roster != None) and (not roster.getFunctionLockable(3)) and (component == self.driver.componentF3) and (value == self.driver.valueF3Off)) :
                            fnThrottle.setF3( not fnThrottle.getF3() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF4) and (value == self.driver.valueF4)) :
                            fnThrottle.setF4( not fnThrottle.getF4() )
                        if ((roster != None) and (not roster.getFunctionLockable(4)) and (component == self.driver.componentF4) and (value == self.driver.valueF4Off)) :
                            fnThrottle.setF4( not fnThrottle.getF4() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF5) and (value == self.driver.valueF5)) :
                            fnThrottle.setF5( not fnThrottle.getF5() )
                        if ((roster != None) and (not roster.getFunctionLockable(5)) and (component == self.driver.componentF5) and (value == self.driver.valueF5Off)) :
                            fnThrottle.setF5( not fnThrottle.getF5() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF6) and (value == self.driver.valueF6)) :
                            fnThrottle.setF6( not fnThrottle.getF6() )
                        if ((roster != None) and (not roster.getFunctionLockable(6)) and (component == self.driver.componentF6) and (value == self.driver.valueF6Off)) :
                            fnThrottle.setF6( not fnThrottle.getF6() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF7) and (value == self.driver.valueF7)) :
                            fnThrottle.setF7( not fnThrottle.getF7() )
                        if ((roster != None) and (not roster.getFunctionLockable(7)) and (component == self.driver.componentF7) and (value == self.driver.valueF7Off)) :
                            fnThrottle.setF7( not fnThrottle.getF7() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF8) and (value == self.driver.valueF8)) :
                            fnThrottle.setF8( not fnThrottle.getF8() )
                        if ((roster != None) and (not roster.getFunctionLockable(8)) and (component == self.driver.componentF8) and (value == self.driver.valueF8Off)) :
                            fnThrottle.setF8( not fnThrottle.getF8() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF9) and (value == self.driver.valueF9)) :
                            fnThrottle.setF9( not fnThrottle.getF9() )
                        if ((roster != None) and (not roster.getFunctionLockable(9)) and (component == self.driver.componentF9) and (value == self.driver.valueF9Off)) :
                            fnThrottle.setF9( not fnThrottle.getF9() )
                    except AttributeError:
                        pass  
                    try:
                        if ((component == self.driver.componentF10) and (value == self.driver.valueF10)) :
                            fnThrottle.setF10( not fnThrottle.getF10() )
                        if ((roster != None) and (not roster.getFunctionLockable(10)) and (component == self.driver.componentF10) and (value == self.driver.valueF10Off)) :
                            fnThrottle.setF10( not fnThrottle.getF10() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF11) and (value == self.driver.valueF11)) :
                            fnThrottle.setF11( not fnThrottle.getF11() )
                        if ((roster != None) and (not roster.getFunctionLockable(11)) and (component == self.driver.componentF11) and (value == self.driver.valueF11Off)) :
                            fnThrottle.setF11( not fnThrottle.getF11() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF12) and (value == self.driver.valueF12)) :
                            fnThrottle.setF12( not fnThrottle.getF12() )
                        if ((roster != None) and (not roster.getFunctionLockable(12)) and (component == self.driver.componentF12) and (value == self.driver.valueF12Off)) :
                            fnThrottle.setF12( not fnThrottle.getF12() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF13) and (value == self.driver.valueF13)) :
                            fnThrottle.setF13( not fnThrottle.getF13() )
                        if ((roster != None) and (not roster.getFunctionLockable(13)) and (component == self.driver.componentF13) and (value == self.driver.valueF13Off)) :
                            fnThrottle.setF13( not fnThrottle.getF13() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF14) and (value == self.driver.valueF14)) :
                            fnThrottle.setF14( not fnThrottle.getF14() )
                        if ((roster != None) and (not roster.getFunctionLockable(14)) and (component == self.driver.componentF14) and (value == self.driver.valueF14Off)) :
                            fnThrottle.setF14( not fnThrottle.getF14() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF15) and (value == self.driver.valueF15)) :
                            fnThrottle.setF15( not fnThrottle.getF15() )
                        if ((roster != None) and (not roster.getFunctionLockable(15)) and (component == self.driver.componentF15) and (value == self.driver.valueF15Off)) :
                            fnThrottle.setF15( not fnThrottle.getF15() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF16) and (value == self.driver.valueF16)) :
                            fnThrottle.setF16( not fnThrottle.getF16() )
                        if ((roster != None) and (not roster.getFunctionLockable(16)) and (component == self.driver.componentF16) and (value == self.driver.valueF16Off)) :
                            fnThrottle.setF16( not fnThrottle.getF16() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF17) and (value == self.driver.valueF17)) :
                            fnThrottle.setF17( not fnThrottle.getF17() )
                        if ((roster != None) and (not roster.getFunctionLockable(17)) and (component == self.driver.componentF17) and (value == self.driver.valueF17Off)) :
                            fnThrottle.setF17( not fnThrottle.getF17() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF18) and (value == self.driver.valueF18)) :
                            fnThrottle.setF18( not fnThrottle.getF18() )
                        if ((roster != None) and (not roster.getFunctionLockable(18)) and (component == self.driver.componentF18) and (value == self.driver.valueF18Off)) :
                            fnThrottle.setF18( not fnThrottle.getF18() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF19) and (value == self.driver.valueF19)) :
                            fnThrottle.setF19( not fnThrottle.getF19() )
                        if ((roster != None) and (not roster.getFunctionLockable(19)) and (component == self.driver.componentF19) and (value == self.driver.valueF19Off)) :
                            fnThrottle.setF19( not fnThrottle.getF19() )
                    except AttributeError:
                        pass   
                    
                    try:
                        if ((component == self.driver.componentF20) and (value == self.driver.valueF20)) :
                            fnThrottle.setF20( not fnThrottle.getF20() )
                        if ((roster != None) and (not roster.getFunctionLockable(20)) and (component == self.driver.componentF20) and (value == self.driver.valueF20Off)) :
                            fnThrottle.setF20( not fnThrottle.getF20() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF21) and (value == self.driver.valueF21)) :
                            fnThrottle.setF21( not fnThrottle.getF21() )
                        if ((roster != None) and (not roster.getFunctionLockable(21)) and (component == self.driver.componentF21) and (value == self.driver.valueF21Off)) :
                            fnThrottle.setF21( not fnThrottle.getF21() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF22) and (value == self.driver.valueF22)) :
                            fnThrottle.setF22( not fnThrottle.getF22() )
                        if ((roster != None) and (not roster.getFunctionLockable(22)) and (component == self.driver.componentF22) and (value == self.driver.valueF22Off)) :
                            fnThrottle.setF22( not fnThrottle.getF22() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF23) and (value == self.driver.valueF23)) :
                            fnThrottle.setF23( not fnThrottle.getF23() )
                        if ((roster != None) and (not roster.getFunctionLockable(23)) and (component == self.driver.componentF23) and (value == self.driver.valueF23Off)) :
                            fnThrottle.setF23( not fnThrottle.getF23() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF24) and (value == self.driver.valueF24)) :
                            fnThrottle.setF24( not fnThrottle.getF24() )
                        if ((roster != None) and (not roster.getFunctionLockable(24)) and (component == self.driver.componentF24) and (value == self.driver.valueF24Off)) :
                            fnThrottle.setF24( not fnThrottle.getF24() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF25) and (value == self.driver.valueF25)) :
                            fnThrottle.setF25( not fnThrottle.getF25() )
                        if ((roster != None) and (not roster.getFunctionLockable(25)) and (component == self.driver.componentF25) and (value == self.driver.valueF25Off)) :
                            fnThrottle.setF25( not fnThrottle.getF25() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF26) and (value == self.driver.valueF26)) :
                            fnThrottle.setF26( not fnThrottle.getF26() )
                        if ((roster != None) and (not roster.getFunctionLockable(26)) and (component == self.driver.componentF26) and (value == self.driver.valueF26Off)) :
                            fnThrottle.setF26( not fnThrottle.getF26() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF27) and (value == self.driver.valueF27)) :
                            fnThrottle.setF27( not fnThrottle.getF27() )
                        if ((roster != None) and (not roster.getFunctionLockable(27)) and (component == self.driver.componentF27) and (value == self.driver.valueF27Off)) :
                            fnThrottle.setF27( not fnThrottle.getF27() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF28) and (value == self.driver.valueF28)) :
                            fnThrottle.setF28( not fnThrottle.getF28() )
                        if ((roster != None) and (not roster.getFunctionLockable(28)) and (component == self.driver.componentF28) and (value == self.driver.valueF28Off)) :
                            fnThrottle.setF28( not fnThrottle.getF28() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF29) and (value == self.driver.valueF29)) :
                            fnThrottle.setF29( not fnThrottle.getF29() )
                        if ((roster != None) and (not roster.getFunctionLockable(29)) and (component == self.driver.componentF29) and (value == self.driver.valueF29Off)) :
                            fnThrottle.setF29( not fnThrottle.getF29() )
                    except AttributeError:
                        pass
                    #
                    # Advanced Functions (strings defined in the rosterEntry property "advancedFunctionNames" comma separated list, and then used in the driver as componentAdvFunctionX and valueAdvFunctionX)
                    #  like advF1 = F2;F3 means that F2 and F3 will be toggled when advF1 is triggered, and if F2 or F3 are lockable then they will be toggled back if the value is valueAdvFunctionXOff 
                    #
                    try:
                        if (component == self.driver.componentAdvancedF0) :
                            self.advFunctions.call(roster, "0", (value == self.driver.valueAdvancedF0), fnThrottle)
                        if (component == self.driver.componentAdvancedF1) :
                            self.advFunctions.call(roster, "1", (value == self.driver.valueAdvancedF1), fnThrottle)
                        if (component == self.driver.componentAdvancedF2) :
                            self.advFunctions.call(roster, "2", (value == self.driver.valueAdvancedF2), fnThrottle)
                        if (component == self.driver.componentAdvancedF3) :
                            self.advFunctions.call(roster, "3", (value == self.driver.valueAdvancedF3), fnThrottle)
                        if (component == self.driver.componentAdvancedF4) :
                            self.advFunctions.call(roster, "4", (value == self.driver.valueAdvancedF4), fnThrottle)                                                        
                    except AttributeError:
                        pass          

#Jynstrument main and mandatory methods
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleWindow"
    
    def init(self):
        self.speedTimerTask = SpeedTimerTask() #Speed increase thread
        self.speedTimer = Timer()
        self.speedTimer.schedule(self.speedTimerTask, 0, valueSpeedTimerRepeat)
        self.label = JButton(ImageIcon(self.getFolder() + "/USBControl.png","USBThrottle")) #label
        self.label.addMouseListener(self.getMouseListeners()[0]) # In order to get the popupmenu on the button too
        self.add(self.label)        
        self.desiredController = None
        self.ctrlMenuItem = []
        self.USBDriver = None
        self.driver = None        
        self.model = jmri.jmrix.jinput.TreeModel.instance() # USB controllers model        
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
        self.lastTimeStopButton = Calendar.getInstance().getTimeInMillis()
        self.lastTimeCruiseButton = Calendar.getInstance().getTimeInMillis()
        self.model.addPropertyChangeListener(self) # Listen to USB device changes to update the list of available controllers in the menu

# On quit clean up resources       
    def quit(self):
        self.model.removePropertyChangeListener(self)        
        self.speedTimerTask.cancel()
        self.speedTimer.cancel()
        self.speedTimer.purge()
        for mi in self.ctrlMenuItem :
            self.getPopUpMenu().remove( mi )
        self.model = None
        self.ctrlMenuItem = None
        self.speedTimerTask = None
        self.speedTimer = None
        self.driver = None
        self.USBDriver = None

# Menu entry changed for Current controller and update driver
    def setSelectedController(self, ctrl, item):
        for mi in self.ctrlMenuItem :
            if ( mi != item ):  # Force deselection of other ones
                mi.setSelected(False)
        self.desiredController = ctrl  # we will compare against this reference in the property change event to be sure we are listening to the right one
        if (ctrl != None) :
            sys.path.append(self.getFolder()) # Load a driver
            try:
                if (self.driver != None):
                  del self.driver
                if (self.USBDriver != None):                  
                  del self.USBDriver
                dd=ctrl.getName()
                dd=self.formatDriverName(dd)
                print "Trying to import driver by name \""+ dd +".py\" ..."
                self.USBDriver = __import__(dd)
                print "...driver \""+ dd +".py\" imported"
            except ImportError:  # On error try by device type name
                try:
                    print "...driver by name not found in \""+self.getFolder()+"\""
                    dd=ctrl.getType().toString()
                    dd=self.formatDriverName(dd)
                    print "Trying to import driver by type \""+ dd +".py\" ..."
                    self.USBDriver = __import__(dd)
                    print "...driver \""+ dd +".py\" imported"
                except ImportError:  # On error load a default one                                      
                    print "...driver by type not found in \""+self.getFolder()+"\", importing default one"
                    self.USBDriver =  __import__("Default")
                    print "Default.py driver imported"
            reload(self.USBDriver)
            sys.path.remove(self.getFolder())
            self.driver = self.USBDriver.USBDriver()
    
    def formatDriverName(self, strparam):
      strparam=strparam.replace(" ", "")
      strparam=strparam.replace(",", "")
      strparam=strparam.replace(".", "")
      strparam=strparam.replace("(", "")
      strparam=strparam.replace(")", "")
      strparam=strparam.replace("{", "")
      strparam=strparam.replace("}", "")
      strparam=strparam.replace("[", "")
      strparam=strparam.replace("]", "")  
      return strparam
    
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

# Item listeners for the PopUp menu
class ControllerItemListener( java.awt.event.ItemListener):
    def __init__(self, ctrl, jyns):
        self.ctrl = ctrl
        self.jyns = jyns

    def itemStateChanged(self, evt):
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED ):
            self.jyns.setSelectedController( self.ctrl, evt.getItem() )     
                
# Speed timer class, to increase speed regularly once button pushed, thread stopped on button release
class SpeedTimerTask(TimerTask):
    def __init__(self):
        self.sync = thread.allocate_lock() # Protects properties getters and setters
        self.speedIncrement = 0
        self.throttle = None
        self.isPaused = True

    def pause(self):
        self.sync.acquire()
        self.isPaused = True
        self.sync.release()

    def resume(self):
        self.sync.acquire()
        self.isPaused = False
        self.sync.release()

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

    def run(self):
        if (self.isPaused) :
            return
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
        if ((rosterEntry == None) or (advFn == None) or (status==None) or (throttle==None)):
            return
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
                    elif (status):
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
