# Simple script to setup audio objects
#
# Author: Matthew Harris, copyright 2009
# Part of the JMRI distribution

import time
import jmri

from javax.vecmath import Vector3f

# Create Audio Buffer object and associate file
buffer = audio.provideAudio("IAB1")
buffer.setURL("program:resources/sounds/Crossing.wav")

# Create the first Audio Source object
source1 = audio.provideAudio("IAS1")
# Assign Audio Buffer to this Audio Source
source1.setAssignedBuffer("IAB1")
# Set the pitch of this Audio Source
source1.setPitch(1)
# Set this Audio Source to loop indefinitely (or until stopped)
source1.setLooped(True)
# Set the position of this Audio Source using x, y, z coordinates
#   20 units to the left
#   10 units to the rear
#    0 units up
source1.setPosition(-20.0,10.0,0.0)

# Create the second Audio Source object
source2 = audio.provideAudio("IAS2")
# Assign Audio Buffer to this Audio Source
source2.setAssignedBuffer("IAB1")
# Set the pitch of this Audio Source
source2.setPitch(1.1)
# Set the minimum and maximum number of times this Audio Source should loop
source2.setMinLoops(3)
source2.setMaxLoops(6)
# Set the position of this Audio Source using x, y, z coordinates
#   10 units to the right
#    0 units to the rear
#    0 units up
source2.setPosition(10.0,0.0,0.0)
# Set the velocity of this Audio Source using Vector3f object
#    1 units/sec to the left
#    0 units/sec to the rear
#    0 units/sec up
source2.setVelocity(Vector3f(-1.0,0.0,0.0))

# Start playing the first Audio Source
source1.play()
# Go to sleep for 3 seconds
time.sleep(3)
# Fade-in the second Audio Source
source2.fadeIn()
# Go to sleep for 1 second
time.sleep(1)
# Fade-out the first Audio Source
source1.fadeOut()
# Second Audio Source will stop after playback of the requisite number of loops
