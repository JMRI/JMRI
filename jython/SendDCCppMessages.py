# Sending various messages to the DCCpp connection. 
#   Useful for intializing turnout definitions, power state, etc.
#   adapted from SendReceiveLocoNetPeerXfrMessage.py 

import jmri
import java

# create a minimal listener class needed to call sendDCCppMessage()
class PeerListener (jmri.jmrix.dccpp.DCCppListener) :
    def message(self, msg) :
        return

# function to send a string message to the DCCpp connection
def send(strMessage) :
    m = jmri.jmrix.dccpp.DCCppMessage.makeMessage(strMessage)
    tc.sendDCCppMessage(m, dl)

# get the DCCpp connection stuff once
dc = jmri.InstanceManager.getList(jmri.jmrix.dccpp.DCCppSystemConnectionMemo).get(0);
tc = dc.getDCCppTrafficController()
dl = tc.addDCCppListener(0xFF,PeerListener())
    

#-----------------------------------------------------------
# list of messages to send, examples are from DCC-EX 3.1.7+
#send("T")
send("T 23 DCC 5 0")
send("T 24 SERVO 100 410 205 2")
send("T 25 VPIN 50")
send("T 26 VPIN 164")
send("T 27 DCC 27 1")
send("T 28 DCC 28 0")
send("T 29 DCC 29 1")
send("T")
