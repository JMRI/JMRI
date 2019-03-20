# Sensor Initialization
#
# This script will issue the DS54 interrogate commands
#
# AutomatonExample distributed with JMRI
#                    from which this script has been developed.
# Created by Phil Klein

import java
import javax.swing
import jmri

# set the intended LocoNet connection by its index; when you have just 1 connection index = 0
connectionIndex = 0

class SensorInitialization(jmri.jmrit.automat.AbstractAutomaton) :

# handle() will only execute once here, to run a single test

    def handle(self):

        # For BDL16

#       jmri.InstanceManager.getDefault(jmri.PowerManager).setPower(jmri.PowerManager.OFF)
#       self.waitMsec(1000)
        jmri.InstanceManager.getDefault(jmri.PowerManager).setPower(jmri.PowerManager.ON)
        self.waitMsec(1000)

        # For BDL168, SE8C, SIC24

        l = jmri.jmrix.loconet.LocoNetMessage(4)
        l.setOpCode(0xB0)
        l.setElement(1,0x78)
        l.setElement(2,0x27)
        
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)
        self.waitMsec(1000)

        l.setElement(1,0x79)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)
        self.waitMsec(1000)

        l.setElement(1,0x7A)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)
        self.waitMsec(1000)

        l.setElement(1,0x7B)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)
        self.waitMsec(1000)

        l.setElement(1,0x78)
        l.setElement(2,0x07)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)
        self.waitMsec(1000)

        l.setElement(1,0x79)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)
        self.waitMsec(1000)

        l.setElement(1,0x7A)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)
        self.waitMsec(1000)

        l.setElement(1,0x7B)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)

        print "Sensor Initialization Complete"

        return

SensorInitialization().start()
