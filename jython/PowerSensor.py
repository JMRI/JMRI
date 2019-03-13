# Provides an example of listening to the PowerManager, and
# operating a Sensor to indicate changes.
#
# Author: Bob Jacobsen, copyright 2005
# Part of the JMRI distribution

import jmri
import java
import java.beans

# The sensor number used to indicate the power status is hardcoded
# below as "100". Change this if you want to use some other sensor.
#

# First, define the listener.  This one just prints some
# information on the change, but more complicated code is
# of course possible.
class PowerListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    # here, power has changed. Find new state
    power = jmri.InstanceManager.getDefault(jmri.PowerManager).getPower()
    if (power == jmri.PowerManager.ON) :
       state = ACTIVE   
    else :
       state = INACTIVE
    sensors.provideSensor("100").setState(state)   
    return 

# Second, attach that listener to the PowerManager. The variable m
# is used to remember the listener so we can remove it later
p = jmri.InstanceManager.getDefault(jmri.PowerManager)
m = PowerListener()
p.addPropertyChangeListener(m)

# Finally, invoke the listener once to set the initial state
m.propertyChange(None)

# This script is done, and leaves behind the PowerListener object
# listening to any changes.  If you want that to stop, in some
# other script you need to do
#    p.removePropertyListener(m)
#
