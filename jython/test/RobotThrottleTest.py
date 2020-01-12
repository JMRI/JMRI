#  Minimal test that RobotThrottle can load OK
#
# Author: Bob Jacobsen, copyright 2019
# Part of the JMRI distribution

import java
import jmri
import time

if (not java.awt.GraphicsEnvironment.isHeadless()) : 
    execfile("jython/RobotThrottle.py")
