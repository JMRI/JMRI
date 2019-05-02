# This is an example script for a JMRI "Automat" in Python
#
# It builds a consist out of two locomotives, moves the consist at 50% speed
# until a stop sensor is encountered, when it stops the consist and then
# breaks the consist appart.
#
# Note: This script does not currently check for errors
#
# Author: Paul Bender, copyright 2007-2011
# Part of the JMRI distribution

import jarray
import jmri

class ConsistExample(jmri.jmrit.automat.AbstractAutomaton) :

    # init() is called exactly once at the beginning to do
    # any necessary configuration.
        #
    def init(self):

        # get the throttle objects for both locomotives and the
                # consist
        self.throttle1 = self.getThrottle(1234,True) # long address 1234
        self.throttle2 = self.getThrottle(5678,True) # long address 5678
        self.throttle3 = self.getThrottle(24,False) # short address 24

        self.stopSensor = sensors.provideSensor("13")

        # Wait to give the throttles a chance to initialize
        self.waitMsec(1000)

        # set the speed of both throttles and the consist to 0

        self.throttle1.setSpeedSetting(0)
        self.throttle2.setSpeedSetting(0)
        self.throttle3.setSpeedSetting(0)

        # create the consist
        self.myConsist=jmri.InstanceManager.getDefault(jmri.ConsistManager).getConsist(jmri.DccLocoAddress(24,False))

        # Add locomotive 1234 to the consist with it's forward as the
        # consist forward direction, and 5678 to the consist with its           # normal forward direction as the reverse direction.

        self.myConsist.add(jmri.DccLocoAddress(1234,True),True)
        self.myConsist.add(jmri.DccLocoAddress(5678,True),False)

        return

    # handle() is called repeatedly until it returns false.
    #
    # Modify this to do your calculation.
    def handle(self):

        # set consist to forward
        self.throttle3.setIsForward(True)

        # set consist speed to 50%
        self.throttle3.setSpeedSetting(0.50)

        # wait for stop sensor to trigger
        self.waitSensorActive(self.stopSensor)

        # stop consist
        self.throttle3.setSpeedSetting(0)

        # and remove the two locomotives from the consist

        self.myConsist.remove(jmri.DccLocoAddress(1234,True))
        self.myConsist.remove(jmri.DccLocoAddress(5678,True))

        # and for good measure, delete the consist
        self.myConsist=jmri.InstanceManager.getDefault(jmri.ConsistManager).delConsist(jmri.DccLocoAddress(24,False))

        return 0    # to stop

# end of class definition

# create one of these
a = ConsistExample()

# set the name, as a example of configuring it
a.setName("Consist example script")

# and start it running
a.start()

