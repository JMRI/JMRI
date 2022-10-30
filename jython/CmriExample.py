# Sample script to show how to send a C/MRI frame
#
#
# Author: Bob Jacobsen, copyright 2022
#
# Part of the JMRI distribution

import jmri
import java

# First, example of receiving. Put a listener in place.
class MyListener (jmri.jmrix.cmri.serial.SerialListener) :
    def message(self, msg) :
        # this handles messages being sent by ignoring them
        return
    def reply(self, msg) :
        print "received incoming Frame"
        print "content: ", msg.toString()
        return

listener = MyListener()

# Get the traffic controller
tc = None
try:
  tc = jmri.InstanceManager.getDefault(jmri.jmrix.cmri.CMRISystemConnectionMemo).getTrafficController()
  tc.addSerialListener(listener)
except:
  print "No Traffic Controller"

# Send an outgoing message frame
frame = jmri.jmrix.cmri.serial.SerialMessage(3) # 3 byte length for example
frame.setElement(0, 0x41)  # node number
frame.setElement(1, 0x49)  # I
frame.setElement(2, 0)     # type byte

if tc != None:
  tc.sendSerialMessage(frame, None)
  print "CMRI frame sent!"
else:
  print "Sorry, no Traffic Controller!"

