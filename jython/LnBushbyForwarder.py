# Provides a "Bushby" forwarder for LocoNet.
#
# This mechanism listens for "normal" LocoNet turnout control messages, and,
# for user-configured turnouts, issues the corresponding "special" LocoNet
# turnout control messages (i.e. "forwards" the message).
#
# This mechanism is useful when the command station's "Bushby" bit is enabled,
# typically via Digitrax command station OpSw27="C"losed, and certain devices
# must get their control information from the DCC track signal's "Basic Accessory
# Decoder" control packets.  Note that some LocoNet-connected devices get turnout
# control information from the low-power version of the DCC track signal which is
# found on the "RailSync" wires in the LocoNet cable, so it may be appropriate
# to configure this mechanism for the turnouts related to some LocoNet-connected
# devices.
#
# A "normal" LocoNet turnout control message is one with the OpCode 0xB0.
#
# A "special" LocoNet turnout control message is one with the OpCode 0xBD.
#
# User-configuration is via declaration in the toBeForwarded array of turnout
# numbers.  If toBeForwarded is initialized as:
#    toBeForwarded = 27, 52, 8, 1902
# then any "normal" LocoNet turnout control message for any of turnouts 8, 27,
# 52, or 1902 will be forwarded by this mechanism using a comparable "special"
# LocoNet turnout control message (to the same turnout address).
#
# This mechanism ignores all "normal" LocoNet turnout messages to turnout
# addresses other than those defined in toBeForwarded.
#
# This mechanism ignores all "special" LocoNet turnout control messages.
#
# Created by B. Milhaupt     Copyright 2021
#

import jmri

import java

# set the intended LocoNet connection by its index.  When you have just 1
# connection, set  the connectionIndex = 0
connectionIndex = 0

# set the list of LocoNet turnout addresses for which "forwarding" will take
# place in toBeForwarded
# example:
# toBeForwarded = [27, 52, 8, 1902]
toBeForwarded = [10, 11, 12, 13]

class BushbyForwarder(jmri.jmrix.loconet.LocoNetListener):
    def message(self, msg):
        # is this a "normal" turnout control msg?
        if (msg.getNumDataElements() <> 4) :
            # ignore messages <> 4 bytes length
            return
        elif (msg.getOpCode() <> jmri.jmrix.loconet.LnConstants.OPC_SW_REQ) :
            # ignore all LocoNet messages _other_ than normal turnout control
            # messages
            return

        # Extract the turnout address
        a1 = msg.getElement(1)
        a2 = msg.getElement(2)
        address = (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1)

        # is the message address one which is to be forwarded?
        for ad in toBeForwarded:
            if (address == ad):
                # a match is found! create the forwarded turnout control message
                msg2 = msg
                msg2.setElement(0, jmri.jmrix.loconet.LnConstants.OPC_SW_ACK)

                # send it!
                tc.sendLocoNetMessage(msg2)
                print ("Forwarded a message for address ", address)
                # may now exit this mechanism
                return

# create the LocoNet listener
lnListener = BushbyForwarder()

# install the LocoNet listener (i.e. the BushbyForwarder implementation)
tc = jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController()
tc.addLocoNetListener(0xFF, lnListener)
