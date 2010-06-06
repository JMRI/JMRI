#
# This script starts a JMRI Throttle window and set up a USB Control on it
#
# Author: Lionel Jeanson, copyright 2010
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

tw = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleWindow()
tw.ynstrument("jython/Jynstruments/ThrottleWindowToolBar/USBThrottle.jyn")
tw.setVisible(True)
