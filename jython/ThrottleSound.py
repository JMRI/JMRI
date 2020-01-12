# Play a sound when the F3 key is pressed on the
# handheld and loco 1001 selected.
#
# Authors: Peter Lloyd-Jones, Bob Jacobsen, copyright 2006
# Part of the JMRI distribution
#

import jmri
import java
import java.beans

# Define the listener class to play a sound when a  throttle function changes
class ThrottleListener(java.beans.PropertyChangeListener):

    def propertyChange(self, event):
      if ((event.propertyName == "F3") and (event.newValue == True) and (event.oldValue == False)) :
         self.snd.play()
     print "F3"

# create a specific listener object, with a specific sound
m = ThrottleListener()
m.snd = jmri.jmrit.Sound("resources/sounds/Crossing.wav")

# define class to request a throttle, wait for it, then attach a listener
class BlowWhistle(jmri.jmrit.automat.AbstractAutomaton) :

          def init(self):

                  # get the throttle object
                  throttle = self.getThrottle(self.number, self.long)
                  if (throttle == None) :
                          print "Couldn't assign throttle!"
                  # set listener
                  throttle.addPropertyChangeListener(m)
                  return

          # handle() does nothing on this one
          def handle(self):
                return  0   # want to end

# run it
print "Lets Go!"
a = BlowWhistle()
a.number = 1001
a.long = True
a.start()
