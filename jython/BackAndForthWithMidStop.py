# This is an example script for a JMRI "Automat" in Python
# It is based on the AutomatonExample.
#
# It listens to two sensors, running a locomotive back and
# forth between them by changing its direction when a sensor
# detects the engine.
#
# Author:  Howard Watkins, January 2007.
# Part of the JMRI distribution
#
# Modified by Peter Ulvestad, June 2009
# Added Mid Station stop

import jarray
import jmri

class FireValleyTest(jmri.jmrit.automat.AbstractAutomaton) :

    def init(self):
        # init() is called exactly once at the beginning to do
        # any necessary configuration.
        print "Inside init(self)"

        # set up sensor numbers
        # fwdSensor is reached when loco is running forward
        self.fwdSensor = sensors.provideSensor("LS5")
        self.revSensor = sensors.provideSensor("LS8")
        self.fwdstnSensor = sensors.provideSensor("LS7")
        self.revstnSensor = sensors.provideSensor("LS6")

        # get loco address. For long address change "False" to "True"
        self.throttle = self.getThrottle(11, False)  # short address 11

        return

    def handle(self):
        # handle() is called repeatedly until it returns false.
        print "Inside handle(self)"

        # turn on Headlight
        self.throttle.setF0(True)

        # set loco to forward
        print "Set Loco Forward"
        self.throttle.setIsForward(True)

        # wait 1 second for layout to catch up, then set speed
        self.waitMsec(1000)
        print "Set Speed"
        self.throttle.setSpeedSetting(0.40)

        # wait for middle sensor (Station Stop) in forward direction to trigger, then stop
        print "Wait for Middle Forward Sensor"
        self.waitSensorActive(self.fwdstnSensor)
        print "Set Speed Stop - Station Stop"
        self.throttle.setSpeedSetting(0)

        # wait for Station stop, then set speed
        self.waitMsec(20000)          # wait for 20 seconds
        print "Set Speed"
        self.throttle.setSpeedSetting(0.40)

        # wait for sensor in forward direction to trigger, then stop
        print "Wait for Forward Sensor"
        self.waitSensorActive(self.fwdSensor)
        print "Set Speed Stop"
        self.throttle.setSpeedSetting(0)

        # delay for a time (remember loco could still be moving
        # due to simulated or actual inertia). Time is in milliseconds
        print "wait 20 seconds"
        self.waitMsec(20000)          # wait for 20 seconds

        # turn on whistle, set direction to reverse, set speed
        self.throttle.setF3(True)     # turn on whistle
        self.waitMsec(1000)           # wait for 1 seconds
        self.throttle.setF3(False)    # turn off whistle
        self.waitMsec(1000)           # wait for 1 second

        print "Set Loco Reverse"
        self.throttle.setIsForward(False)
        self.waitMsec(1000)                 # wait 1 second for Xpressnet to catch up
        print "Set Speed"
        self.throttle.setSpeedSetting(0.40)

        # wait for middle sensor (Station Stop) in reverse direction to trigger, then stop
        print "Wait for Middle Reverse Sensor"
        self.waitSensorActive(self.revstnSensor)
        print "Set Speed Stop - Station Stop"
        self.throttle.setSpeedSetting(0)

        # wait for Station stop, then set speed
        self.waitMsec(20000)          # wait for 20 seconds
        print "Set Speed"
        self.throttle.setSpeedSetting(0.40)

        # wait for sensor in reverse direction to trigger
        print "Wait for Reverse Sensor"
        self.waitSensorActive(self.revSensor)
        print "Set Speed Stop"
        self.throttle.setSpeedSetting(0)

        # delay for a time (remember loco could still be moving
        # due to simulated or actual inertia). Time is in milliseconds
        print "wait 20 seconds"
        self.waitMsec(20000)          # wait for 20 seconds

        # turn on whistle, set direction to forward, set speed
        self.throttle.setF3(True)     # turn on whistle
        self.waitMsec(1000)           # wait for 1 seconds
        self.throttle.setF3(False)    # turn off whistle
        self.waitMsec(1000)           # wait for 1 second

        # and continue around again
        print "End of Loop"
        return 1
        # (requires JMRI to be terminated to stop - caution
        # doing so could leave loco running if not careful)

# end of class definition

# start one of these up
FireValleyTest().start()

