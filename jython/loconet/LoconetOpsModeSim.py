# This script does a simple Digitrax SIM for Decoder Ops Mode  Read and Writes.
# It can be useful when testing e.g. DecoderPro using a LocoNet simulator connection.
#
# Author: Robin Becker, 2023


import java
import javax.swing
import jmri
import jmri.jmrit.automat.AbstractAutomaton as AbstractAutomaton
import jmri.jmrix.loconet.LnConstants as LnConstants
import jmri.jmrix.loconet.LocoNetMessage as LocoNetMessage
import jmri.jmrix.loconet.LocoNetListener as LocoNetListener
import time

# set the intended LocoNet connection by its index; when you have just 1 connection index = 0
connectionIndex = 0
# decoder CV value buffer
CVBuffer = [0]*256;

class MessageBuffer(object):
    def __init__ (self):
        self.size = 32
        self.buffer = [LocoNetMessage(4)] * self.size
        self.delay = [int] * self.size
        self.addIndex = 0
        self.getIndex = 0
        self.messages = 0

    def addMessage (self, lmsg):
        self.buffer[self.addIndex] = lmsg
        self.delay[self.addIndex] = time.time()
        self.messages +=1
        self.addIndex +=1
        if (self.addIndex >= self.size):
            self.addIndex = 0

    def getMessage (self):
        lmsg = self.buffer[self.getIndex]
        self.messages -=1
        self.getIndex +=1
        if (self.getIndex >= self.size):
            self.getIndex = 0
        return lmsg



class LoconetOpsModeSIM(AbstractAutomaton, LocoNetListener):

    def init (self):
        # create loconet message buffer
        self.loconetMessageBuffer = MessageBuffer()
        # get LnTrafficController
        self.tc = jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController()
        # register LoconetListner for Programming messages only
        self.tc.addLocoNetListener(jmri.jmrix.loconet.LocoNetInterface.ALL, self)
        # add sensor for clean exit
        self.exitSensor = sensors.provideSensor("ISOpsModeSimExit")
        self.exitSensor.setKnownState(INACTIVE)

    def handle (self):
        self.waitSensorActive(self.exitSensor);
        self.tc.removeLocoNetListener(jmri.jmrix.loconet.LocoNetInterface.TURNOUTS, self)
        return False

    # LoconetListener handler
    def message (self, msg):
        # check if Loconet Message is an Programmer Read or Write
        if (msg.getOpCode() == LnConstants.OPC_WR_SL_DATA) and (msg.getElement(2) == LnConstants.PRG_SLOT) :
            # check for Ops Mode Byte Read or Write
            pcmd = msg.getElement(3)
            if (pcmd & LnConstants.PCMD_OPS_MODE) != 0 :
                hopsa = msg.getElement(5);
                lopsa = msg.getElement(6);
                # accept any device address for now
                cvNumHi = msg.getElement(8);
                cvNumLo = msg.getElement(9);
                data = msg.getElement(10);
                # extract CV7 and D7 from cvNumHi
                if (cvNumHi & 0x01) != 0 :
                    cnNumLo |= 0x80;
                if (cvNumHi & 0x02) != 0 :
                    data |= 0x80;
                # adjust bits CV9 and CV8 into proper position
                temp = cvNumHi / 16
                cvNumHi = (cvNumHi / 8) | (cvNumHi & 0x01);
                # just set cvNumHi to 0 for now so we do CV number mod 256
                cvNumHi = 0;

                #send LACK with Accepted Blind code 0x40
                lmsg = LocoNetMessage(4)
                lmsg.setOpCode(LnConstants.OPC_LONG_ACK)
                lmsg.setElement(1, LnConstants.OPC_WR_SL_DATA & 0x7f)
                lmsg.setElement(2, 0x40)
                self.tc.sendLocoNetMessage(lmsg)

                # check if CV Write or CV Read
                if (pcmd & LnConstants.PCMD_RW) != 0 :
                    # CV Write - update buffer
                    CVBuffer[cvNumLo] = data;
                else :
                    #CV Read - send response
                    lmsg = LocoNetMessage(msg)
                    lmsg.setElement(0,LnConstants.OPC_SL_RD_DATA)
                    lmsg.setElement(10, CVBuffer[cvNumLo]);
                    self.tc.sendLocoNetMessage(lmsg)

LoconetOpsModeSIM("Loconet Ops Mode SIM").start()
