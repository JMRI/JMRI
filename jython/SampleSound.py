# This is an example script for playing a sound in a JMRI script
#
# Author: Bob Jacobsen, copyright 2004
# Part of the JMRI distribution

import jmri

# create the sound object by loading a file
snd = jmri.jmrit.Sound("resources/sounds/Crossing.wav")

# play the sound once
snd.play()

# You can also do snd.loop() to start playing the sound
# as a continuous loop, and snd.stop() to stop it
