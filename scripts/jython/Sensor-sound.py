# Listens for Coalwood Sensor (Sensor #4), plays crossing gate sound on train entering block.
#
# Based on Listen example and sound example by Bob Jacobsen, copyright 2004
# Modified by Scott CR Henry
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

# create the sound object by loading a file
snd = jmri.jmrit.Sound("resources/sounds/Crossing.wav")

# First, define the listener.  Kept the original print command
# and added play sound.
class MyListener(java.beans.PropertyChangeListener):
  
  def propertyChange(self, event):
    if ((event.newValue == ACTIVE) and (event.oldValue == INACTIVE)) : snd.play()
    # play the sound once
    
    # You can also do snd.loop() to start playing the sound
    # as a continuous loop, and snd.stop() to stop it

# Second, attach that listener to a particular sensor - Coalwood (#4).
t = sensors.provideSensor("4")
m = MyListener()
t.addPropertyChangeListener(m)

print "Finished playing the sound" 