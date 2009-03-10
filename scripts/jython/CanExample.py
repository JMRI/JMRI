# Sample script to show how to send and receive CAN Frames
#
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.3 $

import java

# First, example of receiving. Put a listener in place.
class MyCanListener (jmri.jmrix.can.CanListener) :
    def message(self, msg) :
        # this handles messages being sent by ignoring them
        return
    def reply(self, msg) :
        print "received Frame"
        print "ID: 0x"+java.lang.Integer.toHexString(msg.getHeader())
        print "content: ", msg.toString()
        return
    
jmri.jmrix.can.TrafficController.instance().addCanListener(MyCanListener())


# Send a frame
frame = jmri.jmrix.can.CanMessage()
frame.setHeader(0x123)
frame.setNumDataElements(2)   # will load 2 bytes
frame.setElement(0, 0x45)
frame.setElement(1, 0x67)
jmri.jmrix.can.TrafficController.instance().sendCanMessage(frame, None)

