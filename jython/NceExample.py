# Sample script to show how to send and receive NCE messages
#
# Author: Bob Jacobsen, copyright 2026
#
# Part of the JMRI distribution

import jmri
import java

# First, example of receiving. Put a listener in place.
class MyListener (jmri.jmrix.nce.NceListener) :
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
  tc = jmri.InstanceManager.getDefault(jmri.jmrix.nce.NceSystemConnectionMemo).getNceTrafficController()
  # add a listener to process messages and replies
  tc.addNceListener(listener)
except:
  print "No Traffic Controller"

# Send an outgoing ASCII message
message = jmri.jmrix.nce.NceMessage("Foo") # forms for binary are also available

# In the alternative, send an outgoing binary message
# message = NceMessage(3) # 3 byte message containing 0x123456
# message.setBinary(True)
# message.setOpCode(0x12)
# message.setElement(1, 0x34)
# message.setElement(2, 0x56)

if tc != None:
  tc.sendNceMessage(message, None)
  print "NCE message sent!"
else:
  print "Sorry, no Traffic Controller!"

