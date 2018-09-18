# Connect a AAR105 Control Stand (USB device) to a throttle.
#
# ATTENTION: Special version for HidRawEnvironmentPlugin of JInput
# ATTENTION: Currently works in Windows only
#
# See <http://jmri.org/help/en/html/hardware/raildriver/index.shtml>
#
# Author: Joan Carranc, 2010
# - Based on the original RailDriver.py, Bob Jacobsen, copyright 2008
# - Throttle window management and roster selection based on xboxThrottle.py, Andrew Berridge, copyright 2010
# Part of the JMRI distribution
#
# This is still experimental code and is under development. Currently, it supports:
# 1. Each controller will open its own throttle window, identified by hashCode
# 2. All blue buttons assigned to functions (0-27)
# 3. Leftmost lever as reverser - front moves forwards, back reverses
# 4. Second from left lever as throttle - back increases speed
# 5. Selection of locos using the "zoom" rocker switch. Up selects a loco, Down dispatches 
#      current loco.
# 6. Selection of loco addresses / throttle panels using the "pov" four-way switch.
#    Using up and down on the pov switch will move between locos in the roster 
#      (as long as the last loco has been "dispatched")
#    Using left and right on the pov switch will select between throttle panels... Click the 
#      plus (+) button on the throttle window to add another panel.
# 7. Emergency stop! Push the corresponding button(s). This will e-stop the current throttle.
# 8. Gear buttons: High/Low range can be selected via these buttons, mapped to "shuntFn" function
# 9. Cab buttons: alerter, sander, pantograph and bell buttons can be mapped to a function
#10. Horn momentary lever: mapped to "hornFn" function
# 
# Future development ideas:
# 1. Think of functionalities for the other levers
# 2. Other uses for the display ??
# 3. Calibration ??
#
##
# IMPORTANT Warnings:
# 1. Make sure you calibrate and set a "Dead zone" for each of the analogue levers in the
# Calibration window! If you don't there will be too many events triggered 
# and everything will slow right down....

import java
import java.beans
import jmri

#
# Set the name of the controller you're using
#
desiredControllerName = "AAR105 Control Stand"

#
# Some function numbers, specific to decoder type / CV mapping
#
bellFn  = 1
hornFn  = 2
shuntFn = 3 # Lenz Silver and Gold
sandFn  = 7

#
# Component 'mapping'
#
# Groups of Switches: Functions 0-15
componentMaxFunction  = 15    # Max function to use for blue buttons
# Special Switches
componentSelectAddr   = "1"  # Acquire the current address shown on the list
componentDispatchAddr = "2"  # Release the previous address
componentNextAddr     = "3"  # Scroll down to the next address on the list
componentRevNeutral   = "4"  # Reverser in Neutral
componentRevReverse   = "5"  # Reverser in Neutral
componentRevForward   = "6"  # Reverser in Neutral
componentPrevAddr     = "7"  # Scroll up to the previous address on the list
componentNextFrame    = "8"  # Move the next Throttle Frame
componentPrevFrame    = "9"  # Move to the previous Throttle Frame
# Cab buttons
componentEStop    = "10"  # E-Stop: Fuel cut-off
componentEStopBis = "11"  # E-Stop: Brake cut-off
componentHorn     = "12"  # Horn momentary lever
componentSand     = "13"  # Sand left button
componentSandBis  = "14"  # Sand right button
componentBell     = "15"  # Bell button
# Axes
componentThrottle = "Axis 0"
leverMin = 0.05   # value of component for zero speed
leverMax = 1.00   # value of component for max speed
#
tempMillis = 500  # milliseconds to show (temporary) the press of a button

# From here down is the code for the throttle
# Generally, you shouldn't touch it unless you're debugging a problem

# uncomment the following two lines to run from a keyboard for debug ??
#desiredControllerName = "Keyboard"
#componentThrottle = "Axis 1"

# connect to USB device
model = jmri.jmrix.jinput.TreeModel.instance()

#Keep track of the number of throttles created
numThrottles = 0
# and the number of controllers used
numControllers = 0

def isNaN(num): 
    return num != num 

# add listener for USB events
class TreeListener(java.beans.PropertyChangeListener):

  #hash code is unique instance of controller
  controllerHashCode = 0 
  throttleWindow = None
  controlPanel = None
  functionPanel = None
  addressPanel = None
  activeThrottleFrame = None

  def __init__(self, controller):
    global numThrottles
    global numControllers
    self.controller = controller
    self.controllerHashCode = controller.hashCode()
    # open a throttle window and get components
    self.throttleWindow = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleWindow()
    self.activeThrottleFrame = self.throttleWindow.addThrottleFrame()
    # move throttle on screen so multiple throttles don't overlay each other
    self.throttleWindow.setLocation(400 * numThrottles, 50 * numThrottles)
    numThrottles += 1
    numControllers += 1
    self.activeThrottleFrame.toFront()
    self.controlPanel = self.activeThrottleFrame.getControlPanel()
    self.functionPanel = self.activeThrottleFrame.getFunctionPanel()
    self.addressPanel = self.activeThrottleFrame.getAddressPanel()
    #!self.controller.displayStrImm("hi")
    #self.controller.displayNumNext(self.addressPanel.getCurrentAddress().getNumber())
    self.throttleWindow.addPropertyChangeListener(self)
    self.activeThrottleFrame.addPropertyChangeListener(self)

  def propertyChange(self, event):
    if (event.propertyName == "ancestor"):
        #print "ancestor property change - closing throttle window"
        # Remove all property change listeners and
        # dereference all throttle components
        self.activeThrottleFrame.removePropertyChangeListener(self)
        self.throttleWindow.removePropertyChangeListener(self)
        self.activeThrottleFrame = None
        self.controlPanel = None
        self.functionPanel = None
        self.addressPanel = None
        self.throttleWindow = None
        #!self.controller.displayStrImm("bye")
        # Now remove this propertyChangeListener from the model
        global model
        model.removePropertyChangeListener(self)
        
    if (event.propertyName == "ThrottleFrame") :  # Current throttle frame changed
        #print "Throttle Frame changed"
        self.addressPanel = event.newValue.getAddressPanel()
        self.controlPanel = event.newValue.getControlPanel()
        self.functionPanel = event.newValue.getFunctionPanel()
        if (self.addressPanel.getCurrentAddress() != None) :
          #!self.controller.displayNumNext(self.addressPanel.getCurrentAddress().getNumber())
          print "skip display"

    if (event.propertyName == "Value") :
        # event.oldValue is the UsbNode
        #
        # uncomment the following line to see controller names
        #print "|"+event.oldValue.getController().toString()+"|"
        #print event.oldValue.getController().getName()
        #print event.oldValue.getController().hashCode()
        #
        # Select just the device (controller) we want
        cont = event.oldValue.getController()
        if (cont.toString() == desiredControllerName 
        and cont.hashCode() == self.controllerHashCode) :
            # event.newValue is the value, e.g. 1.0
            # Check for desired component and act
            component = event.oldValue.getComponent().getIdentifier().toString()
            value = event.newValue
            # 
            # uncomment the following to see the entries
            print "process", component, value
            
            # Function buttons
            try:
                fNum = int(component) # direct mapping of buttons 0 -> maxFunction
            except ValueError:
                fNum = 99             # axis
            # reverser buttons are not function buttons, processed in code later on
            if (component == componentRevNeutral or component == componentRevReverse or component == componentRevForward) :
                fNum = 99
            # E-stop buttons are not function buttons, processed in code later on
            if (component == componentEStop or component == componentEStopBis) :
                fNum = 99
            # horn has special treatment, processed in code later on
            if (component == componentHorn) :
                fNum = 99
            if (component == componentSand or component == componentSandBis) :
                fNum = sandFn
            if (component == componentBell) :
                fNum = bellFn
            # toggle / fixed setting depending on throttle button definition
            if fNum <= componentMaxFunction:  # component out of range (not a blue button or cab button with special mapping)
                button = self.functionPanel.getFunctionButtons()[fNum]
                if (button != None) :
                    if button.getIsLockable() :
                        if value > 0.5 :
                            button.changeState(not button.getState())
                    else :
                        button.changeState(value > 0.5)
                    if (value > 0.5 and button.getState()) : # only display if actually setting the function
                        #!cont.displayStrTemp("F" + str(fNum)) #, tempMillis)
                        print "skip display"
                return

            # Probably is better not to 'waste' 6 switches for this ?
            # Address and Throttle Frame selection
            #print "addr: " + self.addressPanel.getCurrentAddress().toString() 
            #self.addressPanel.showRosterSelectorPopup()
            selectedIndex = self.addressPanel.getRosterSelectedIndex()
            
            if (component == componentNextAddr and value > 0.5):
                self.addressPanel.setRosterSelectedIndex(selectedIndex + 1)
                return

            if (component == componentPrevAddr and value > 0.5):
                self.addressPanel.setRosterSelectedIndex(selectedIndex - 1)
                return

            if (component == componentSelectAddr and value > 0.5):
                #! cont.displayStrTemp("sel") #, tempMillis)
                self.addressPanel.selectRosterEntry()
                #! cont.displayNumNext(self.addressPanel.getCurrentAddress().getNumber())
                return

            if (component == componentDispatchAddr and value > 0.5):
                #! cont.displayStrTemp("dis") #, tempMillis)
                self.addressPanel.dispatchAddress()
                #! cont.displayNumNext(0)
                return

            if (component == componentNextFrame and value > 0.5):
                self.throttleWindow.nextThrottleFrame()
                return

            if (component == componentPrevFrame and value > 0.5):
                self.throttleWindow.previousThrottleFrame()
                return

            # Special buttons
            # "Emergency stop" button
            if (component == componentEStop or component == componentEStopBis) :
                if (value > 0.5) :
                    self.controlPanel.stop()
                    print "Emergency Stop!"
                    return

            # "Horn" (digital) lever, momentary
            if (component == componentHorn) :
                if (value > 0.5) :
                    self.functionPanel.getFunctionButtons()[hornFn].changeState(True)
                else :
                    self.functionPanel.getFunctionButtons()[hornFn].changeState(False)
                return
                        
            # Reverser 'lever'
            if (component == componentRevReverse) :
                if (value > 0.5) :
                    #! cont.displayStrTemp("rev") #, tempMillis)
                    self.controlPanel.setForwardDirection(False)
                #print "Direction changed"
                return
            if (component == componentRevForward) :
                if (value > 0.5) :
                    #! cont.displayStrTemp("fwd") #, tempMillis)
                    self.controlPanel.setForwardDirection(True)
                #print "Direction changed"
                return

            # Throttle lever
            if (component == componentThrottle) :
                # negative is lever front, positive is lever back
                # limit range to only positive side of lever
                if (value < leverMin) : value = leverMin
                if (value > leverMax) : value = leverMax
                # convert fraction of input to speed step
                fraction = (value-leverMin)/(leverMax-leverMin)
                slider = self.controlPanel.getSpeedSlider()
                setting = int(round(fraction*(slider.getMaximum()-slider.getMinimum()), 0))
                slider.setValue(setting)
                #! cont.displayNumTemp(self.controlPanel.getDisplaySlider())
                #print "Throttle:", value, setting
                # How do I get the speed "value" ???
                print "Slider Speed:", self.controlPanel.getDisplaySlider()
                return

#Iterate over the controllers, creating a new listener for each
#controller of the type we are interested in
for c in model.controllers():
    name = c.getName()
    hashCode = c.hashCode()
    if (name == desiredControllerName and c.getType().toString() == "Gamepad"):
        print "**************   Found " + name + " " + str(hashCode)
        model.addPropertyChangeListener(TreeListener(c))
