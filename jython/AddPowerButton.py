# Sample script to add a button to the main
# JMRI application window that controls layout power
#
# Author: Bob Jacobsen, copyright 2007, 2024
# Part of the JMRI distribution

#
# NOTE: The recommended way to add a script button to JMRI is to
# use the Startup Items preferences to add that button when the function is available.
#
# NOTE: This script does not support DecoderPro. Use the recommended
# method if you need to add a script to a button in DecoderPro.
#

import jmri
import java
import javax.swing.JButton
import apps

# create the button, and add an action routine to it
powerButton = javax.swing.JButton("Starting...")
def whenMyButtonClicked(event) :
    # alternate the power state
    power = jmri.InstanceManager.getDefault(jmri.PowerManager).getPower()
    if (power == jmri.PowerManager.ON) :
       powermanager.setPower(jmri.PowerManager.OFF) 
    else :
       powermanager.setPower(jmri.PowerManager.ON)
    return
powerButton.actionPerformed = whenMyButtonClicked

# add the button to the main screen
apps.Apps.buttonSpace().add(powerButton)

# force redisplay
apps.Apps.buttonSpace().revalidate()

# create a listener to the powerManager to update the button
class PowerListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    # here, power has changed. Find new state
    power = jmri.InstanceManager.getDefault(jmri.PowerManager).getPower()
    if (power == jmri.PowerManager.ON) :
       powerButton.setText("Turn Off")
    else :
       powerButton.setText("Turn On")
    return 
    
# attach that to the power manager
powerListener = PowerListener()
p = jmri.InstanceManager.getDefault(jmri.PowerManager)
p.addPropertyChangeListener(powerListener)

# and run it once to set the initial state
powerListener.propertyChange(None)
