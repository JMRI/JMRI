# Receives and parses loconet messages from the SIGM20 signal logic board.
#
#

import jmri

import java

# Set the intended LocoNet connection by its name.
# It's usual "L".
connectionName = "L"

class SIGNM20receiver(jmri.jmrix.loconet.LocoNetListener):
    def message(self, msg):
        # check for a match in the first three bytes
        if ( msg.getElement(0) != 0xE4 )  : return
        if ( msg.getElement(1) != 0x09 )  : return
        if ( msg.getElement(2) != 0x05 )  : return

        # Extract the content
        a2 = msg.getElement(2)
        a3 = msg.getElement(3)
        a4 = msg.getElement(4)
        a5 = msg.getElement(5)
        a6 = msg.getElement(6)
        a7 = msg.getElement(7)
        a8 = msg.getElement(8)

        signalNumber = (a3 | (a2 << 7) ) + 1
        print ("accepted message with ", a3, a4, a5, a8, signalNumber)

        signalName = "IH"+str(signalNumber)
        print ("signal name ", signalName)
        signal = signals.getSignalHead(signalName)

        # if the signal hasn't been defined, return
        if (signal == None) : return
        print ("signal user name ", signal.getUserName())

        # set the appearance based on the speed in a6
        if a6 > 0x10 : signal.setAppearance(GREEN)
        else : signal.setAppearance(RED)
        print ("appearance now ", signal.getAppearance())
        # do something with that content

# create the LocoNet listener
lnListener = SIGNM20receiver()

# install the LocoNet listener
tc = jmri.jmrix.SystemConnectionMemoManager.getConnection(connectionName, jmri.jmrix.loconet.LocoNetSystemConnectionMemo).getLnTrafficController()
tc.addLocoNetListener(0xFF, lnListener)
