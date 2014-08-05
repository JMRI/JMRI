# Sensor Initialization
#
#This script will issue the DS54 interrogate commands 
#
# $Revision$ revision of AutomatonExample distributed with JMRI
#					 from which this script has been developed.
# Created by Phil Klein

import java
import javax.swing

class SensorInitialization(jmri.jmrit.automat.AbstractAutomaton) :
	

# handle() will only execute once here, to run a single test

 
 
	def handle(self):


		# For BDL16

#		jmri.InstanceManager.powerManagerInstance().setPower(jmri.PowerManager.OFF)
#		self.waitMsec(1000)
		jmri.InstanceManager.powerManagerInstance().setPower(jmri.PowerManager.ON)
		self.waitMsec(1000)


		# For BDL168, sE8C, SIC24 

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

		print "Sensor Initialization Complete"

		return

SensorInitialization().start()
