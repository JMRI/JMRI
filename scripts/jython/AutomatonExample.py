# This is an example script for a JMRI "Automat" in Python
#
# It listens to two sensors, running a locomotive back and 
# forth between them by changing its direction when a sensor
# detects the engine
#
# $Revision: 1.3 $

import jarray
import jmri

class AutomatExample(jmri.jmrit.automat.AbstractAutomaton) :

	# init() is called exactly once at the beginning to do
	# any necessary configuration.
	def init(self):
		
		# get the sensor and throttle objects
		self.fwdSensor = sensors.provideSensor("12")
		self.revSensor = sensors.provideSensor("13")
		self.throttle = self.getThrottle(1234, True)  # long address 1234
		 		
		return

	# handle() is called repeatedly until it returns false.
	#
	# Modify this to do your calculation.
	def handle(self):

		# set loco to forward
		self.throttle.setIsForward(True)
		
		# wait for sensor in forward direction to trigger
		self.waitSensorActive(self.fwdSensor)
		
		# set loco to reverse
		self.throttle.setIsForward(False)
		
		# wait for sensor in reverse direction to trigger
		self.waitSensorActive(self.revSensor)
		
		# and continue around again
		return 1	# to continue
	
# end of class definition

# start one of these up
AutomatExample().start()

