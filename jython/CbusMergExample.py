# Sample script to show how to send and receive CAN Frames
#
#
# Author: Bob Jacobsen, copyright 2008
# Author: Gert Muller, copyright 2012
#
# Part of the JMRI distribution

import jmri
import java
import javax.swing









print "hello"




# First, example of receiving. Put a listener in place.
class MyCanListener (jmri.jmrix.can.CanListener) :
    def message(self, msg) :
        print "Sending own frame"

        print "ID: 0x"+java.lang.Integer.toHexString(msg.getHeader())
        print "content: ", msg.toString()
        print "..................................................................................."
        # this handles messages being sent by ignoring them
        return
    def reply(self, msg) :
        print "received Frame"
        print "Sender CAN ID: 0x"+java.lang.Integer.toHexString(msg.getHeader())
        print "content: ", msg.toString()
        print "..................................................................................."
        return
    
# Get the traffic controller
tc = None
 
# Old way first...
try:
  tc = jmri.jmrix.can.TrafficController.instance()
except:
  # Then the new way...
  try:
    tc = jmri.InstanceManager.getDefault(jmri.jmrix.can.CanSystemConnectionMemo).getTrafficController()
    print "traffic controller found"
  except:
    print "no traffic controller"
    tc = None
 
tc.addCanListener(MyCanListener())


# Send a frame
frame = jmri.jmrix.can.CanMessage(tc.getCanid()) # use the CAN ID of the MERG connection, 120-127

frame.setNumDataElements(5)   # will load opscode + 4 bytes

frame.setElement(0, CbusConstants.CBUS_ACON);
frame.setElement(1, 00)  # node number 1st half of 4 byte hex string
frame.setElement(2, 01)  # node number 2nd half hex
frame.setElement(3, 00)  # event number 1st half hex
frame.setElement(4, 18)  # event number 2nd half hex



if tc != None:
  tc.sendCanMessage(frame, None)
  print "CAN frame sent!"
else:
  print "Sorry, no Traffic Controller!"

