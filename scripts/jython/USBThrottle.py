# Connect a Griffin PowerMate USB device to a throttle
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $


# set the name of the controller we're looking for
desiredControllerName = "Griffin PowerMate" # "Apple Internal Keyboard / Trackpad"
componentWheel = "rx" # "2" # relative turn of wheel
componentPress = "1"  # pressing the wheel
componentForSwitchDirection = "."

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
        # uncomment the following to see all entries
        #print component, value
        #
        if (component == componentWheel and value > 0.0) :
            controlPanel.accelerate1()
        if (component == componentWheel and value < 0.0) :
            controlPanel.decelerate1()
        if (component == componentPress and value > 0.0) :
            # alternate direction
            controlPanel.setIsForward(not controlPanel.getIsForward())
    return

model.addPropertyChangeListener(TreeListener())
