# Use an Xbox (original) controller as throttle(s)									`1AA
#
# Author: Andrew Berridge, 2010 - Based on USBThrottle.py, Bob Jacobsen, copyright 2008
# (NOT YET!) Part of the JMRI distribution
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
# 5. Multiple controllers will open multiple throttles (eventually will be two each)
# 6. Selection of locos using the "hat" four-way switch. Start selects a loco, Back dispatches current loco
# 
# Future development ideas:
# 1. Support right-hand joystick to control a second throttle
# 2. Finish implementation of function buttons
# 3. Have multiple throttles, use left and right hat switch to select between them
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

# The next line is maintained by CVS, please don't change it
# $Revision: 1.2 $

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
# Use the following if you have a relative analog or digital (on/off)
# control you'd like to use to control speed
#
componentWheel = "Y Axis"       # relative device for throttle
componentWheelInverted = True   # negative values is more positive speed
componentWheelReverses = True   # will go through zero to change direction
componentWheelIncrement = 1     # speed slider increment per click
componentWheelUpperEdge =  0.2  # counts as a positive click if more than this
componentWheelLowerEdge = -0.2  # counts as a negative click if less than this

#
# Set the follow to buttons you want to control speed
# and direction of the locomotive
#
componentUp = ""       # button, click to raise speed
componentDown = ""     # button, click to raise speed
componentStop = ""     # button, sets stop when clicked
componentReverse = ""  # button, reverse when on, forward when off

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

# add listener for USB events
class TreeListener(java.beans.PropertyChangeListener):
 
  locked = False
  F1isOn = False
  F2isOn = False
  F0isOn = False
  #hash code is unique instance of controller
  controllerHashCode = 0 
  controlPanel = None
  functionPanel = None
  addressPanel = None
  
  def __init__(self, controllerHashCode):
  	global numThrottles
  	global numControllers
  	self.controllerHashCode = controllerHashCode
  	# open a throttle window and get components
	tf = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleFrame()
	# move throttle on screen so multiple throttles don't overlay each other
	tf.setLocation(150 * numThrottles, 50 * numThrottles)
	numThrottles += 1
	numControllers += 1
	tf.toFront()
	self.controlPanel = tf.getControlPanel()
	self.functionPanel = tf.getFunctionPanel()
	self.addressPanel = tf.getAddressPanel()
  
  def propertyChange(self, event):
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
        print component, value
        
        #print "addr: " + self.addressPanel.getCurrentAddress().toString() 
        
        if (component == hatSwitch):
        	#self.addressPanel.showRosterSelectorPopup()
        	selectedIndex = self.addressPanel.getRosterSelectedIndex()
        	if (value == 0.75):
        		self.addressPanel.setRosterSelectedIndex(selectedIndex + 1)
        	if (value == 0.25):
        		self.addressPanel.setRosterSelectedIndex(selectedIndex - 1)
        
        if (component == componentStart and value > 0.5):
        	self.addressPanel.selectRosterEntry()
        	
        if (component == componentBack and value > 0.5):
        	self.addressPanel.dispatchAddress()
        
        #
        #  uncomment the following to debug wheel (relative throttle) support
        #if (component == "A" ) :
        #    component = componentWheel
        #    value = -1
        #if (component == "S" ) :
        #    component = componentWheel
        #    value = 0
        #if (component == "D" ) :
        #    component = componentWheel
        #    value = 1
        #
        # basic throttle keys
        if (component == componentUp and value > 0.0) :
            self.controlPanel.accelerate1()
        if (component == componentDown and value > 0.0) :
            self.controlPanel.decelerate1()
        if (component == componentStop and value > 0.0) :
            self.controlPanel.stop()
        if (component == componentReverse) :
            if (value > 0.0) : 
                # reverse
                self.controlPanel.setForwardDirection(False)
                print "sFD False"
            else : 
                # forward
                self.controlPanel.setForwardDirection(True)
                print "SFD True"

        # "Lock" the throttle
        if (component == componentALock and value > 0.0) :
            if self.locked:
                self.locked = False
                slider = self.controlPanel.getSpeedSlider()
                slider.setValue(0)
            else:
                self.locked = True

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

        # relative throttle component
        if (component == componentWheel) :
            # calculate new speed
            slider = self.controlPanel.getSpeedSlider()
            isfwd = self.controlPanel.getIsForward()
            nowsetting = slider.getValue()
            if (componentWheelReverses and not isfwd) :
                # code direction into value as its sign
                nowsetting = -1 * nowsetting
            # update to new fraction
            if (not componentWheelInverted and value > componentWheelUpperEdge) :
                newsetting = nowsetting+componentWheelIncrement
            elif (componentWheelInverted and value > componentWheelUpperEdge) :
                newsetting = nowsetting-componentWheelIncrement
            elif (not componentWheelInverted and value < componentWheelLowerEdge) :
                newsetting = nowsetting-componentWheelIncrement
            elif (componentWheelInverted and value < componentWheelLowerEdge) :
                newsetting = nowsetting+componentWheelIncrement
            else : 
                newsetting = nowsetting  # don't do anything
            print "At A: ", value, nowsetting, newsetting, self.controlPanel.getIsForward()
            self.setdirection = False
            if (componentWheelReverses) :
                # negative values mean go in reverse
                if (newsetting > 0 and not isfwd ) :
                    self.setdirection = True
                    self.newdirection = True
                    print "Set FWD"
                elif (newsetting < 0 ) :
                    if (isfwd) :
                        self.setdirection = True
                        self.newdirection = False
                        print "Set RVRS"
                    newsetting = -1 * newsetting
            else :
                # do not drop down through zero
                if (newsetting < 0 ) :
                    newsetting = 0;
            # truncate
            if (newsetting < slider.getMinimum()) :
                newsetting = slider.getMinimum()
            if (newsetting > slider.getMaximum()) :
                newsetting = slider.getMaximum()
            # store the result as a scaled speed
            slider.setValue(newsetting)
            if (self.setdirection) :
                self.controlPanel.setForwardDirection(self.newdirection)
            print "At Z:", value, nowsetting, newsetting, self.controlPanel.getIsForward()

        # function keys
        if (component == componentF0) :
            if self.F0isOn and value > 0.5:
                self.functionPanel.getFunctionButtons()[0].changeState(0)
                self.F0isOn = False
            elif value > 0.5:
                self.functionPanel.getFunctionButtons()[0].changeState(1)
                self.F0isOn = True
            
        if (component == componentF1) :
            if self.F1isOn and value > 0.5:
                self.functionPanel.getFunctionButtons()[1].changeState(0)
                self.F1isOn = False
            elif value > 0.5:
                self.functionPanel.getFunctionButtons()[1].changeState(1)
                self.F1isOn = True

            
        if (component == componentF2) :
            #Code for toggling (probably not required) F2:
		#if self.F2isOn and value > 0.5:
            #    self.functionPanel.getFunctionButtons()[2].changeState(0)
            #    self.F2isOn = False
            #elif value > 0.5:
            #    self.functionPanel.getFunctionButtons()[2].changeState(1)
            #    self.F2isOn = True
		self.functionPanel.getFunctionButtons()[2].changeState(value>0.5)
                
        if (component == componentF3) :
            self.functionPanel.getFunctionButtons()[3].changeState(value>0.5)
        if (component == componentF4) :
            self.functionPanel.getFunctionButtons()[4].changeState(value>0.5)
        if (component == componentF5) :
            self.functionPanel.getFunctionButtons()[5].changeState(value>0.5)
        if (component == componentF6) :
            self.functionPanel.getFunctionButtons()[6].changeState(value>0.5)
        if (component == componentF7) :
            self.functionPanel.getFunctionButtons()[7].changeState(value>0.5)
        if (component == componentF8) :
            self.functionPanel.getFunctionButtons()[8].changeState(value>0.5)
        return

#Iterate over the controllers, creating a new listener for each
#controller of the type we are interested in
for c in model.controllers():
	name = c.getName()
	hashCode = c.hashCode()
	if (name == desiredControllerName):
		print "Found " + name + " " + str(hashCode)
		model.addPropertyChangeListener(TreeListener(hashCode))

