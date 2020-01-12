# Use a USB device as a throttle
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#

import jmri
import java
import java.beans

#
# Set the name of the controller you're using
#
desiredControllerName = "WingMan Cordless Gamepad"

#
# Use the following if you have an absolute, analog control
# (as "slider") you'd like to use to control speed
#
componentASlider = ""   # absolute proportional device for throttle
sliderAMin = 0.0         # value of componentASlider for zero speed
sliderAMax = 0.99        # value of componentASlider for max speed

#
# Use the following if you have a relative analog or digital (on/off)
# control you'd like ot use to control speed
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
componentF0 = ""    # F0 follows this
componentF1 = "1"    # F1 follows this
componentF2 = "2"    # F2 follows this
componentF3 = "0"    # F3 follows this
componentF4 = "5"    # F4 follows this
componentF5 = ""    # F5 follows this
componentF6 = ""    # F6 follows this
componentF7 = ""    # F7 follows this
componentF8 = ""    # F8 follows this

# from here down is the code for the throttle
# Generally, you shouldn't touch it unless you're debugging a problem

# uncomment the following two lines to run from a keyboard for debug
#desiredControllerName = "Apple Internal Keyboard / Trackpad"
#componentWheel = "a"

# open a throttle window and get components
tf = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleFrame()
tf.toFront()
controlPanel = tf.getControlPanel()
functionPanel = tf.getFunctionPanel()

# connect to USB device
model = jmri.jmrix.jinput.TreeModel.instance()

# add listener for USB events
class TreeListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    if (event.propertyName == "Value") :
      # event.oldValue is the UsbNode
      #
      # uncomment the following line to see controller names
      # print "|"+event.oldValue.getController().toString()+"|"
      #
      # Select just the device (controller) we want
      if (event.oldValue.getController().toString() == desiredControllerName) :
        # event.newValue is the value, e.g. 1.0
        # Check for desired component and act
        component = event.oldValue.getComponent().toString()
        value = event.newValue
        # 
        # uncomment the following to see the entries
        #print component, value
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
            controlPanel.accelerate1()
        if (component == componentDown and value > 0.0) :
            controlPanel.decelerate1()
        if (component == componentStop and value > 0.0) :
            controlPanel.stop()
        if (component == componentReverse) :
            if (value > 0.0) : 
                # reverse
                controlPanel.setForwardDirection(False)
                print "sFD False"
            else : 
                # forward
                controlPanel.setForwardDirection(True)
                print "SFD True"

        # absolute throttle component
        if (component == componentASlider) :
            # handle speed setting input
            # limit range
            if (value < sliderAMin) :
                value = sliderAMin
            if (value > sliderAMax) :
                value = sliderAMax
            # convert fraction of input to speed step
            fraction = (value-sliderAMin)/(sliderAMax-sliderAMin)
            slider = controlPanel.getSpeedSlider()
            setting = int(round(fraction*(slider.getMaximum()-slider.getMinimum()), 0))
            slider.setValue(setting)

        # relative throttle component
        if (component == componentWheel) :
            # calculate new speed
            slider = controlPanel.getSpeedSlider()
            isfwd = controlPanel.getIsForward()
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
            print "At A: ", value, nowsetting, newsetting, controlPanel.getIsForward()
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
                controlPanel.setForwardDirection(self.newdirection)
            print "At Z:", value, nowsetting, newsetting, controlPanel.getIsForward()

        # function keys
        if (component == componentF0) :
            functionPanel.getFunctionButtons()[0].changeState(value>0.5)
        if (component == componentF1) :
            functionPanel.getFunctionButtons()[1].changeState(value>0.5)
        if (component == componentF2) :
            functionPanel.getFunctionButtons()[2].changeState(value>0.5)
        if (component == componentF3) :
            functionPanel.getFunctionButtons()[3].changeState(value>0.5)
        if (component == componentF4) :
            functionPanel.getFunctionButtons()[4].changeState(value>0.5)
        if (component == componentF5) :
            functionPanel.getFunctionButtons()[5].changeState(value>0.5)
        if (component == componentF6) :
            functionPanel.getFunctionButtons()[6].changeState(value>0.5)
        if (component == componentF7) :
            functionPanel.getFunctionButtons()[7].changeState(value>0.5)
        if (component == componentF8) :
            functionPanel.getFunctionButtons()[8].changeState(value>0.5)
        return

model.addPropertyChangeListener(TreeListener())

