# This is an example script for playing a sound in a JMRI script
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.2 $

import jmri

# create the sound object by loading a file
snd = jmri.jmrit.Sound("resources/sounds/Crossing.wav")

# play the sound once
snd.play()

# You can also do snd.loop() to start playing the sound
# as a continuous loop, and snd.stop() to stop it
