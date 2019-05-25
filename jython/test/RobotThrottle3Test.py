#  Minimal test that RobotThrottle3 can load OK
#
# Author: Bob Jacobsen, copyright 2019
# Part of the JMRI distribution

import java
import jmri
import time

if (not java.awt.GraphicsEnvironment.isHeadless()) : 
    execfile("jython/RobotThrottle3.py")

    # rb1 is the thread from the test
    # wait for it to start
    time.sleep(0.5) # wait 0.5 second

    # request stop
    rb1.stop()
