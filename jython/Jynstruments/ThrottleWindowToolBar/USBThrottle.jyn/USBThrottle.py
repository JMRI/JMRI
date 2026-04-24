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
                    roster = self.getContext().getCurentThrottleController().getRosterEntry()
                else:
                    return # no throttle, no control, exit
                # Uncomment bellow line to see component name and its value
                print "Component \""+component+"\" value changed to ",value
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
                    try:
                        if ((component == self.driver.componentF0) and (value == self.driver.valueF0)) :
                            throttle.setF0( not throttle.getF0() )
                        if ((roster != None) and (not roster.getFunctionLockable(0)) and (component == self.driver.componentF0) and (value == self.driver.valueF0Off)) :
                            throttle.setF0( not throttle.getF0() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF1) and (value == self.driver.valueF1)) :
                            throttle.setF1( not throttle.getF1() )
                        if ((roster != None) and (not roster.getFunctionLockable(1)) and (component == self.driver.componentF1) and (value == self.driver.valueF1Off)) :
                            throttle.setF1( not throttle.getF1() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF2) and (value == self.driver.valueF2)) :
                            throttle.setF2( not throttle.getF2() )
                        if ((roster != None) and (not roster.getFunctionLockable(2)) and (component == self.driver.componentF2) and (value == self.driver.valueF2Off)) :
                            throttle.setF2( not throttle.getF2() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF3) and (value == self.driver.valueF3)) :
                            throttle.setF3( not throttle.getF3() )
                        if ((roster != None) and (not roster.getFunctionLockable(3)) and (component == self.driver.componentF3) and (value == self.driver.valueF3Off)) :
                            throttle.setF3( not throttle.getF3() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF4) and (value == self.driver.valueF4)) :
                            throttle.setF4( not throttle.getF4() )
                        if ((roster != None) and (not roster.getFunctionLockable(4)) and (component == self.driver.componentF4) and (value == self.driver.valueF4Off)) :
                            throttle.setF4( not throttle.getF4() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF5) and (value == self.driver.valueF5)) :
                            throttle.setF5( not throttle.getF5() )
                        if ((roster != None) and (not roster.getFunctionLockable(5)) and (component == self.driver.componentF5) and (value == self.driver.valueF5Off)) :
                            throttle.setF5( not throttle.getF5() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF6) and (value == self.driver.valueF6)) :
                            throttle.setF6( not throttle.getF6() )
                        if ((roster != None) and (not roster.getFunctionLockable(6)) and (component == self.driver.componentF6) and (value == self.driver.valueF6Off)) :
                            throttle.setF6( not throttle.getF6() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF7) and (value == self.driver.valueF7)) :
                            throttle.setF7( not throttle.getF7() )
                        if ((roster != None) and (not roster.getFunctionLockable(7)) and (component == self.driver.componentF7) and (value == self.driver.valueF7Off)) :
                            throttle.setF7( not throttle.getF7() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF8) and (value == self.driver.valueF8)) :
                            throttle.setF8( not throttle.getF8() )
                        if ((roster != None) and (not roster.getFunctionLockable(8)) and (component == self.driver.componentF8) and (value == self.driver.valueF8Off)) :
                            throttle.setF8( not throttle.getF8() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF9) and (value == self.driver.valueF9)) :
                            throttle.setF9( not throttle.getF9() )
                        if ((roster != None) and (not roster.getFunctionLockable(9)) and (component == self.driver.componentF9) and (value == self.driver.valueF9Off)) :
                            throttle.setF9( not throttle.getF9() )
                    except AttributeError:
                        pass  
                    try:
                        if ((component == self.driver.componentF10) and (value == self.driver.valueF10)) :
                            throttle.setF10( not throttle.getF10() )
                        if ((roster != None) and (not roster.getFunctionLockable(10)) and (component == self.driver.componentF10) and (value == self.driver.valueF10Off)) :
                            throttle.setF10( not throttle.getF10() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF11) and (value == self.driver.valueF11)) :
                            throttle.setF11( not throttle.getF11() )
                        if ((roster != None) and (not roster.getFunctionLockable(11)) and (component == self.driver.componentF11) and (value == self.driver.valueF11Off)) :
                            throttle.setF11( not throttle.getF11() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF12) and (value == self.driver.valueF12)) :
                            throttle.setF12( not throttle.getF12() )
                        if ((roster != None) and (not roster.getFunctionLockable(12)) and (component == self.driver.componentF12) and (value == self.driver.valueF12Off)) :
                            throttle.setF12( not throttle.getF12() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF13) and (value == self.driver.valueF13)) :
                            throttle.setF13( not throttle.getF13() )
                        if ((roster != None) and (not roster.getFunctionLockable(13)) and (component == self.driver.componentF13) and (value == self.driver.valueF13Off)) :
                            throttle.setF13( not throttle.getF13() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF14) and (value == self.driver.valueF14)) :
                            throttle.setF14( not throttle.getF14() )
                        if ((roster != None) and (not roster.getFunctionLockable(14)) and (component == self.driver.componentF14) and (value == self.driver.valueF14Off)) :
                            throttle.setF14( not throttle.getF14() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF15) and (value == self.driver.valueF15)) :
                            throttle.setF15( not throttle.getF15() )
                        if ((roster != None) and (not roster.getFunctionLockable(15)) and (component == self.driver.componentF15) and (value == self.driver.valueF15Off)) :
                            throttle.setF15( not throttle.getF15() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF16) and (value == self.driver.valueF16)) :
                            throttle.setF16( not throttle.getF16() )
                        if ((roster != None) and (not roster.getFunctionLockable(16)) and (component == self.driver.componentF16) and (value == self.driver.valueF16Off)) :
                            throttle.setF16( not throttle.getF16() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF17) and (value == self.driver.valueF17)) :
                            throttle.setF17( not throttle.getF17() )
                        if ((roster != None) and (not roster.getFunctionLockable(17)) and (component == self.driver.componentF17) and (value == self.driver.valueF17Off)) :
                            throttle.setF17( not throttle.getF17() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF18) and (value == self.driver.valueF18)) :
                            throttle.setF18( not throttle.getF18() )
                        if ((roster != None) and (not roster.getFunctionLockable(18)) and (component == self.driver.componentF18) and (value == self.driver.valueF18Off)) :
                            throttle.setF18( not throttle.getF18() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF19) and (value == self.driver.valueF19)) :
                            throttle.setF19( not throttle.getF19() )
                        if ((roster != None) and (not roster.getFunctionLockable(19)) and (component == self.driver.componentF19) and (value == self.driver.valueF19Off)) :
                            throttle.setF19( not throttle.getF19() )
                    except AttributeError:
                        pass   
                    
                    try:
                        if ((component == self.driver.componentF20) and (value == self.driver.valueF20)) :
                            throttle.setF20( not throttle.getF20() )
                        if ((roster != None) and (not roster.getFunctionLockable(20)) and (component == self.driver.componentF20) and (value == self.driver.valueF20Off)) :
                            throttle.setF20( not throttle.getF20() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF21) and (value == self.driver.valueF21)) :
                            throttle.setF21( not throttle.getF21() )
                        if ((roster != None) and (not roster.getFunctionLockable(21)) and (component == self.driver.componentF21) and (value == self.driver.valueF21Off)) :
                            throttle.setF21( not throttle.getF21() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF22) and (value == self.driver.valueF22)) :
                            throttle.setF22( not throttle.getF22() )
                        if ((roster != None) and (not roster.getFunctionLockable(22)) and (component == self.driver.componentF22) and (value == self.driver.valueF22Off)) :
                            throttle.setF22( not throttle.getF22() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF23) and (value == self.driver.valueF23)) :
                            throttle.setF23( not throttle.getF23() )
                        if ((roster != None) and (not roster.getFunctionLockable(23)) and (component == self.driver.componentF23) and (value == self.driver.valueF23Off)) :
                            throttle.setF23( not throttle.getF23() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF24) and (value == self.driver.valueF24)) :
                            throttle.setF24( not throttle.getF24() )
                        if ((roster != None) and (not roster.getFunctionLockable(24)) and (component == self.driver.componentF24) and (value == self.driver.valueF24Off)) :
                            throttle.setF24( not throttle.getF24() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF25) and (value == self.driver.valueF25)) :
                            throttle.setF25( not throttle.getF25() )
                        if ((roster != None) and (not roster.getFunctionLockable(25)) and (component == self.driver.componentF25) and (value == self.driver.valueF25Off)) :
                            throttle.setF25( not throttle.getF25() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF26) and (value == self.driver.valueF26)) :
                            throttle.setF26( not throttle.getF26() )
                        if ((roster != None) and (not roster.getFunctionLockable(26)) and (component == self.driver.componentF26) and (value == self.driver.valueF26Off)) :
                            throttle.setF26( not throttle.getF26() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF27) and (value == self.driver.valueF27)) :
                            throttle.setF27( not throttle.getF27() )
                        if ((roster != None) and (not roster.getFunctionLockable(27)) and (component == self.driver.componentF27) and (value == self.driver.valueF27Off)) :
                            throttle.setF27( not throttle.getF27() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF28) and (value == self.driver.valueF28)) :
                            throttle.setF28( not throttle.getF28() )
                        if ((roster != None) and (not roster.getFunctionLockable(28)) and (component == self.driver.componentF28) and (value == self.driver.valueF28Off)) :
                            throttle.setF28( not throttle.getF28() )
                    except AttributeError:
                        pass
                    try:
                        if ((component == self.driver.componentF29) and (value == self.driver.valueF29)) :
                            throttle.setF29( not throttle.getF29() )
                        if ((roster != None) and (not roster.getFunctionLockable(29)) and (component == self.driver.componentF29) and (value == self.driver.valueF29Off)) :
                            throttle.setF29( not throttle.getF29() )
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
