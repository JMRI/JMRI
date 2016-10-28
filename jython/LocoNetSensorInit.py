# LocoNetSensorInit.py
#
#This script will issue the DS54 interrogate commands 
#
# Created by Phil Klein  copyright 2010

import java
import javax.swing
import jmri

class LocoNetSensorInit(jmri.jmrit.automat.AbstractAutomaton) :
	

# handle() will only execute once here, to run a single test

 
 
	def handle(self):


		# For BDL16 (remove the 4 "#" symbols if you have a BDL16)

		jmri.InstanceManager.powerManagerInstance().setPower(jmri.PowerManager.OFF)
		self.waitMsec(1000)
		jmri.InstanceManager.powerManagerInstance().setPower(jmri.PowerManager.ON)
		self.waitMsec(1000)


		# For BDL168, SE8C, SIC24 SIC24AD, SRC16, SRC8 

		l = jmri.jmrix.loconet.LocoNetMessage(4)
		l.setOpCode(0xB0)
		l.setElement(1,0x78)
		l.setElement(2,0x27)
		jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(l)
		self.waitMsec(1000)

		l.setElement(1,0x79)
		jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(l)
		self.waitMsec(1000)

		l.setElement(1,0x7A)
		jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(l)
		self.waitMsec(1000)

		l.setElement(1,0x7B)
		jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(l)
		self.waitMsec(1000)

		l.setElement(1,0x78)
		l.setElement(2,0x07)
		jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(l)
		self.waitMsec(1000)

		l.setElement(1,0x79)
		jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(l)
		self.waitMsec(1000)

		l.setElement(1,0x7A)
		jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(l)
		self.waitMsec(1000)

		l.setElement(1,0x7B)
		jmri.jmrix.loconet.LnTrafficController.instance().sendLocoNetMessage(l)

		print "LocoNet Sensor Initialization Complete"

		return

LocoNetSensorInit().start()
