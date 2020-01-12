# Listens for specific Sensor (Sensor #4), plays crossing gate sound on train entering block.
#
# Based on Listen example and sound example by Bob Jacobsen, copyright 2004
# Modified by Scott CR Henry

import jmri
import java
import java.beans

# Define the listener to play a sound when a sensor goes active
class SoundListener(java.beans.PropertyChangeListener):
  
  def propertyChange(self, event):
    if ((event.newValue == ACTIVE) and (event.oldValue == INACTIVE)) : self.snd.play()
    # play the sound once
    
    # You can also do snd.loop() to start playing the sound
    # as a continuous loop, and snd.stop() to stop it

# Create one of these and set it's sound
# create the sound object by loading a file
m = SoundListener()
m.snd = jmri.jmrit.Sound("resources/sounds/Crossing.wav")

# Attach that listener to desired sensors, e.g. number 4 and 7
sensors.provideSensor("4").addPropertyChangeListener(m)
sensors.provideSensor("7").addPropertyChangeListener(m)

# To use a different sound, create another copy of the listener
# and set its sound to a different file
m = SoundListener()
# ("m" is the same variable, but it now contains a different copy of SoundListener)
m.snd = jmri.jmrit.Sound("resources/sounds/RlyClick.wav")

# and attach this new one to another sensor
sensors.provideSensor("9").addPropertyChangeListener(m)
