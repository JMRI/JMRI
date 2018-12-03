# Use an Xbox (original) controller as throttle(s)                                  
#
# Author: Andrew Berridge, 2010 - Based on USBThrottle.py, Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#
# This is still experimental code and is under development. Currently, it supports:
# 1. Left-hand joystick as throttle - up moves forwards, down reverses
# 2. Some buttons assigned to functions (not complete)
# 3. Throttle lock: standard operation is immediate control of throttle. Releasing the joystick
#    sets the throttle to zero (note: a dead zone is currently not implemented, but should be).
#    To lock the throttle so it stays in its current position, pull the left trigger. This is useful
#    for running a train round a loop (for example)
# 4. Some support for toggling functions though this may not be required (use standard throttle function
#    locking?
# 5. Each controller will open its own throttle window
# 6. Selection of locos using the "hat" four-way switch. Start selects a loco, Back dispatches 
#    current loco. Using up and down on the hat switch will move between locos in the roster 
#    (as long as the last loco has been "dispatched"
# 7. Using left and right on the hat switch will select between throttle panels... Click the 
#    plus (+) button on the throttle window to add another panel
# 8. Emergency stop! Push the left stick down to trigger its button. This will
# e-stop the current throttle
# 
# Future development ideas:
# 1. Support right-hand joystick to control a second throttle
# 2. Finish implementation of function buttons
#
# Notes:
# Hat switch is 0.0 for "released"
# 0.25 -> up
# 0.5 -> right
# 0.75 -> down
# 1.0 -> left
#
# Requires:
# XBCD Xbox Controller Driver (from http://www.redcl0ud.com/xbcd.html)
# Original Xbox (not 360) controller
# 
# Currently tested on Windows only
#
# IMPORTANT Warnings:
# 1. Make sure you set a "Dead zone" for each of the analogue sticks (in the
# Windows Control planel)! If you don't there will be too many events triggered 
# and everything will slow right down....
#

import jmri
import java
import java.beans

#
# Set the name of the controller you're using
#
desiredControllerName = "XBCD XBox Gamepad"

#
# Use the following if you have an absolute, analog control
# (as "slider") you'd like to use to control speed
#
componentASlider = "Y"   # absolute proportional device for throttle
sliderAMin = 0.0         # value of componentASlider for zero speed
sliderAMax = 0.99        # value of componentASlider for max speed
componentALock = "11"    # the left trigger

hatSwitch = "Hat Switch" # The name of the Hat Switch

#
# Set the following to the buttons you want
# to use for function control
#
componentF0 = "6"    # F0 follows this
componentF1 = "2"    # 2 is Button B - F1 follows this
componentF2 = "1"    # 1 is Button A - F2 follows this
componentF3 = "0"    # F3 follows this
componentF4 = "5"    # F4 follows this
componentF5 = ""    # F5 follows this
componentF6 = ""    # F6 follows this
componentF7 = ""    # F7 follows this
componentF8 = ""    # F8 follows this

componentStart = "7"
componentBack = "8"

componentLeftStick = "9" # The button on the left stick. Used as E-stop in this configuration

# from here down is the code for the throttle
# Generally, you shouldn't touch it unless you're debugging a problem

# uncomment the following two lines to run from a keyboard for debug
#desiredControllerName = "Apple Internal Keyboard / Trackpad"
#componentWheel = "a"

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
 
  locked = False
  F1isOn = False
  F2isOn = False
  F0isOn = False
  #hash code is unique instance of controller
  controllerHashCode = 0 
  throttleWindow = None
  controlPanel = None
  functionPanel = None
  addressPanel = None
  activeThrottleFrame = None
   
  def __init__(self, controllerHashCode):
    global numThrottles
    global numControllers
    self.controllerHashCode = controllerHashCode
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
        # Now remove this propertyChangeListener from the model
        global model
        model.removePropertyChangeListener(self)
        
    if (event.propertyName == "ThrottleFrame") :  # Current throttle frame changed
        #print "Throttle Frame changed"
        self.addressPanel = event.newValue.getAddressPanel()
        self.controlPanel = event.newValue.getControlPanel()
        self.functionPanel = event.newValue.getFunctionPanel()
            
    if (event.propertyName == "Value") :
        # event.oldValue is the UsbNode
        #
        # uncomment the following line to see controller names
        #print "|"+event.oldValue.getController().toString()+"|"
        #print event.oldValue.getController().getName()
        #print event.oldValue.getController().hashCode()
        #
        # Select just the device (controller) we want
        if (event.oldValue.getController().toString() == desiredControllerName 
        and event.oldValue.getController().hashCode() == self.controllerHashCode) :
            # event.newValue is the value, e.g. 1.0
            # Check for desired component and act
            component = event.oldValue.getComponent().toString()
            value = event.newValue
            # 
            # uncomment the following to see the entries
            # print component, value
            
            #print "addr: " + self.addressPanel.getCurrentAddress().toString() 
            
            #Hat switch switches between locos and throttles in a window
            if (component == hatSwitch):
                #self.addressPanel.showRosterSelectorPopup()
                selectedIndex = self.addressPanel.getRosterSelectedIndex()
                if (value == 0.75):
                    self.addressPanel.setRosterSelectedIndex(selectedIndex + 1)
                if (value == 0.25):
                    self.addressPanel.setRosterSelectedIndex(selectedIndex - 1)
                if (value == 0.5 or value == 1):
                    if (value == 0.5):
                        # advance to next throttle frame
                        self.throttleWindow.nextThrottleFrame()
                    else :
                        # go to previous throttle frame
                        self.throttleWindow.previousThrottleFrame()
                return
                    
            if (component == componentStart and value > 0.5):
                self.addressPanel.selectRosterEntry()
                return
                
            if (component == componentBack and value > 0.5):
                self.addressPanel.dispatchAddress()
                return
            
            # "Lock" the throttle
            if (component == componentALock and value > 0.0) :
                if self.locked:
                    self.locked = False
                    slider = self.controlPanel.getSpeedSlider()
                    slider.setValue(0)
                else:
                    self.locked = True
                return
                        
            # absolute throttle component
            if (component == componentASlider) :
                if not self.locked:
                    isfwd = self.controlPanel.getIsForward()
                    if value > 0 :
                        # reverse
                        if isfwd :
                            self.controlPanel.setForwardDirection(False)
                            forward = False
                       
                    else :
                        if not isfwd:
                            self.controlPanel.setForwardDirection(True)
                            forward = True
                        value = - value
                    
                    # handle speed setting input
                    # limit range
                    if (value < sliderAMin) :
                        value = sliderAMin
                    if (value > sliderAMax) :
                        value = sliderAMax
                    # convert fraction of input to speed step
                    fraction = (value-sliderAMin)/(sliderAMax-sliderAMin)
                    slider = self.controlPanel.getSpeedSlider()
                    setting = int(round(fraction*(slider.getMaximum()-slider.getMinimum()), 0))
                    slider.setValue(setting)
                return
            if component == componentLeftStick: #emergency stop!
                self.controlPanel.stop()
                print "Emergency Stop!"
            #print component, value
            fNum = -1
            if component == componentF0:
                fNum = 0
            elif component == componentF1:
                fNum = 1
            elif component == componentF2:
                fNum = 2
            elif component == componentF3:
                fNum = 3
            elif component == componentF4:
                fNum = 4
            elif component == componentF5:
                fNum = 5
            elif component == componentF6:
                fNum = 6
            elif component == componentF7:
                fNum = 7
            elif component == componentF8:
                fNum = 8
            
            button = self.functionPanel.getFunctionButtons()[fNum]
            if button.getIsLockable() :
                if value > 0.5 :
                    state = button.getState()
                    button.changeState(not state)
            else :
                button.changeState(value > 0.5)

            return

#Iterate over the controllers, creating a new listener for each
#controller of the type we are interested in
for c in model.controllers():
    name = c.getName()
    hashCode = c.hashCode()
    if (name == desiredControllerName):
        print "Found " + name + " " + str(hashCode)
        model.addPropertyChangeListener(TreeListener(hashCode))

