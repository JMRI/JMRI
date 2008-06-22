# Sample script to show how to send and receive CAN Frames
#
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

import java

# First, example of receiving. Put a listener in place.
class MyCanListener (jmri.jmrix.can.CanListener) :
    def message(self, msg) :
        print "received Frame"
        print "ID:", msg.getId()
        print "content: ", msg.toString()
        return
    
jmri.jmrix.can.TrafficController.instance().addCanListener(MyCanListener())


# Send a frame
frame = jmri.jmrix.can.CanMessage()
frame.setId(0x123)
frame.setElement(0, 0x45)
frame.setElement(1, 0x67)
jmri.jmrix.can.TrafficController.instance().sendCanMessage(packet)

