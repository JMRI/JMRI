# Use a USB device as a throttle
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.3 $


# set the name of the controller we're looking for
desiredControllerName = "CH GS3D "

# set the names of things that drive the throttle

componentSlider = "y"   # absolute device, used for throttle
sliderMin = 0.0         # value of componentSlider for zero speed
sliderMax = 0.39        # value of componentSlider for max speed

componentUp = "3"       # button, click to raise speed
componentDown = "4"     # button, click to raise speed
componentStop = "1"     # button, sets stop when clicked
componentReverse = "2"  # button, reverse when on

# from here down is the code for the throttle

# open a throttle window and get components
tf = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleFrame()
tf.setVisible(True)
controlPanel = tf.getControlPanel()

# connect to USB device
model = jmri.jmrix.jinput.TreeModel.instance()

# add listener for USB events
class TreeListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    if (event.propertyName == "Value") :
      # event.oldValue is the UsbNode
      #
      # uncomment the following line to see controller names
      #print "|"+event.oldValue.getController().toString()+"|"
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
            else : 
                # forward
                controlPanel.setForwardDirection(True)
        if (component == componentSlider) :
            # handle speed setting input
            # limit range
            if (value < sliderMin) :
                value = sliderMin
            if (value > sliderMax) :
                value = sliderMax
            # convert fraction of input to speed step
            fraction = (value-sliderMin)/(sliderMax-sliderMin)
            slider = controlPanel.getSpeedSlider()
            setting = int(round(fraction*(slider.getMaximum()-slider.getMinimum()), 0))
            slider.setValue(setting)
        return

model.addPropertyChangeListener(TreeListener())
