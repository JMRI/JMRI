#  Minimal test that AutoDispatcher2 can load OK
#
# Author: Bob Jacobsen, copyright 2019
# Part of the JMRI distribution

import java
import java.awt

if (not java.awt.GraphicsEnvironment.isHeadless()) : 
    execfile("jython/AutoDispatcher2.py")
