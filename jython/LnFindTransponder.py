# Trigger the LocoNet Find Transponder interaction
#
# The DCC address being found is hard-coded into this sample script
#
# After sending the LocoNet Find message, the script also 
# commands a few DCC packets to be sent.  This is in case
# the command station is not currently addressing the 
# decoder, e.g. if it's not currently on a throttle. Transponding
# decoders only reply when they receive a DCC packet addressed
# to them.
#
# Created by Bob Jacobsen 2014
#
# A find request to (long)433 should send E5 09 40 03 31 00 00 00 61
# and the hardware should return E5 09 00 03 31 00 20 00 01
#
# For (short)10: E5 09 40 7D 0A 00 00 00 24
# and reply: E5 09 40 7D 0A 00 20 00 44

import jmri

import java
import javax.swing

# set the intended LocoNet connection by its index; when you have just 1 connection index = 0
connectionIndex = 0

class LnFindTransponder(jmri.jmrit.automat.AbstractAutomaton) :
    # handle() will only execute once here, to run a single test
    def handle(self):

        self.long = True
        self.address = 433
        
        m = jmri.jmrix.loconet.LocoNetMessage(9)
        m.setOpCode(   0xE5)
        m.setElement(1,0x09)
        m.setElement(2,0x40)
        if (self.long) :
            m.setElement(3,self.address/128)
            m.setElement(4,self.address&0x7F)    
        else :
            m.setElement(3,0x7D)
            m.setElement(4,self.address)    
        m.setElement(5,0x00)
        m.setElement(6,0x00)
        m.setElement(7,0x00)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(m)
        
        # now force sending of null packets to same address
        c = jmri.InstanceManager.getDefault(jmri.CommandStation)
        c.sendPacket(jmri.NmraPacket.oneBytePacket(self.address, self.long, 0x08),4)  # reserved instruction
        
        return 0

LnFindTransponder().start()
