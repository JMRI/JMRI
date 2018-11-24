# This is an example script to pulse an output
# based on a fast clock.  
#
# Author: Bob Jacobsen, copyright 2007
# Part of the JMRI distribution

import java
import java.beans
import jmri

# Change the next line to the name of the turnout you want to
# be pulsed
outputTurnout = "IT100"

# First, define the listener.  This one just prints some
# information on the change, but more complicated code is
# of course possible.
timebase = jmri.InstanceManager.getDefault(jmri.Timebase)
class TimeListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    #print "change",event.propertyName
    #print "from", event.oldValue, "to", event.newValue
    turnouts.provideTurnout(outputTurnout).setState(THROWN)
    return
    
# Second, attach that listener to the timebase. 
timebase.addMinuteChangeListener(TimeListener())

# We want the output pulse to be short, so have to
# turn it off. We therefore create a thread to watch
# it, and turn it off three seconds after it comes on.
import jarray

class TimeAutomat(jmri.jmrit.automat.AbstractAutomaton) :
        
        # init() is called exactly once at the beginning to do
        # any necessary configuration.
        def init(self):
                self.output = turnouts.provideTurnout(outputTurnout)
                return

        # handle() is called repeatedly until it returns false.
        def handle(self):
                # wait for turnout to change
                self.waitChange(jarray.array([self.output], jmri.NamedBean))
                self.waitMsec(3000)
                if (self.output.getState() == THROWN) :
                    self.output.setState(CLOSED)
                # and continue around again
                return 1        # to continue

# create one of these
a = TimeAutomat()

# set the name, as a example of configuring it
a.setName("Timebase Script")

# and start it running
a.start()
