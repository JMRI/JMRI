# Sample of sending and receiving data via a LocoNet PeerXfr message.
#
# This is in two parts:
#  * Add a listener to handle the message
#  * Send a sample message
# You should copy the two parts into the relevant parts of your own script.
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution

import jmri

import java


# get the LocoNet connection (the first of potentially several LocoNet connections)
myLocoNetConnection = jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0);

# create the listener class
class PeerListener (jmri.jmrix.loconet.LocoNetListener) :
    def message(self, msg) :
        if (msg.getOpCode() != jmri.jmrix.loconet.LnConstants.OPC_PEER_XFER ) : return
        if (msg.getElement(1) != 0x10 ) : return
        if (msg.getNumDataElements() != 16 ) : return
        # here is valid Peer Xfr message
        data = msg.getPeerXfrData();
        # data is an 8-element array, just print
        print "Data: ", data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]
        return

# add one of those to the LocoNet connection
myLocoNetConnection.getLnTrafficController().addLocoNetListener(0xFF,PeerListener())

# now create a message and send it
source = 10
destination = 20
data = [1,2,3,4,5,6,7,8]
code = 5

message = jmri.jmrix.loconet.LocoNetMessage.makePeerXfr(source, destination, data, code)
myLocoNetConnection.getLnTrafficController().sendLocoNetMessage(message)



