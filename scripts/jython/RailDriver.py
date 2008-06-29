# Connect a RailDriver Modern Desktop (USB device) to a throttle
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $


# set the name of the controller we're looking for
desiredControllerName = "RailDriver Modern Desktop"


# some function numbers
bellFn = 3
hornFn = 2

# open a throttle window and get components
tf = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleFrame()
tf.setVisible(True)
controlPanel = tf.getControlPanel()
functionPanel = tf.getFunctionPanel()

# connect to USB device
model = jmri.jmrix.jinput.TreeModel.instance()

# add listener for USB events
class TreeListener(java.beans.PropertyChangeListener):
  def __init__(self) :
    self.loaded = False
  def propertyChange(self, event):
    if (event.propertyName == "Value") :
      # event.oldValue is the UsbNode
      #
      # uncomment the following line to see controller names and events
      #print "|"+event.oldValue.getController().toString()+"|"
      #
      # Select just the device (controller) we want
      if (event.oldValue.getController().toString().find(desiredControllerName) != -1) :
        #
        # check if initialized
        if (self.loaded == False) :
            self.loaded = True
            # print "Starting initialization"
            # find Controller
            controllers = jmri.jmrix.jinput.TreeModel.instance().controllers()
            print controllers
            for c in controllers :
                if (c.toString().find(desiredControllerName) != -1) :
                    # that's the one!
                    self.components = c.getComponents()
                    print self.components
        # event.newValue is the value, e.g. 1.0
        # Check for desired component and act
        component = event.oldValue.getComponent()
        value = event.newValue
        # 
        # uncomment the following to see all entries
        #print "event", component.toString(), value
        # scan for source index, since names differ from system to system
        i = 0
        for c in self.components :
            if ( c == component) :
                break
            i = i+1
        #print "component index ", i
        #
        # Now start turning index into operation
        #
        # First, the functions. Button number i and function
        # number are the same here (by coincidence), so do simple mapping.
        # Pressing the button alternates the state
        if (i <= 12) :
            if (value>0.1) :
                button = functionPanel.getFunctionButtons()[i]
                print i, button.getState(), (not button.getState())
                button.changeState(not button.getState())
        # A couple special buttons
        # "Bell" is F3, toggles
        if (i == 41) :
            if (value>0.1) :
                button = functionPanel.getFunctionButtons()[bellFn]
                button.changeState(not button.getState())
        # "Horn", momentary
        if (i == 42 or i == 43) :
            if (value>0.1) :
                functionPanel.getFunctionButtons()[hornFn].changeState(True)
            else :
                functionPanel.getFunctionButtons()[hornFn].changeState(False)
        # Then, the throttle controls
        # Left-most fwd-reverse lever
        if (i == 57) :
            # negative is lever front, positive is lever back
            #print "57", value
            if (value<0.1) :
                controlPanel.setForwardDirection(True)
            else :
                controlPanel.setForwardDirection(False)
        # estop
        if (i == 36) :
            if (value>0.1) :
                controlPanel.stop()
        # speed control
        if (i == 58) :
            # negative is lever front, positive is lever back
            #print "58", value
            # convert speed
            mininput = 0.15
            maxinput = 0.4
            if (value < mininput) : value = mininput
            if (value > maxinput) : value = maxinput
            fraction = (value-mininput)/(maxinput-mininput)
            slider = controlPanel.getSpeedSlider()
            setting = int(round(fraction*(slider.getMaximum()-slider.getMinimum()), 0))
            slider.setValue(setting)
            print "58", value, setting
        # end of handling valid input
    # end of handling input event
    return

model.addPropertyChangeListener(TreeListener())
