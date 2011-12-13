#
# This script starts a JMRI Throttle window and set up a USB Control on it
#
# Author: Lionel Jeanson, copyright 2010
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision$
import net.java.games.input.Controller as Controller

numThrottles  = 0
for ctrl in jmri.jmrix.jinput.TreeModel.instance().controllers(): 
    # The selection bellow might have to be modified
    if ((ctrl.getType() == Controller.Type.GAMEPAD) or (ctrl.getType() == Controller.Type.STICK)) :
        tw = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleWindow()
        tw.ynstrument("jython/Jynstruments/ThrottleWindowToolBar/USBThrottle.jyn")
        tw.setLocation(400 * numThrottles, 50 * numThrottles)
        numThrottles+=1
        tw.setVisible(True)
