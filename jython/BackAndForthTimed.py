# This is an example script for a JMRI "Automat" in Python
# It is based on the AutomatonExample.
#
# It runs a locomotive back and forth using time delays.
#
# Times are in milliseconds
#
# Author: Bob Jacobsen, July 2008
# Based on BackAndForth.py
# Author: Howard Watkins, January 2007
# Part of the JMRI distribution

import jarray
import jmri

class BackAndForthTimed(jmri.jmrit.automat.AbstractAutomaton) :

    def init(self):
        # init() is called exactly once at the beginning to do
        # any necessary configuration.
        print "Inside init(self)"

        # get loco address. For long address change "False" to "True"
        self.throttle = self.getThrottle(14, False)  # short address 14

        return

    def handle(self):
        # handle() is called repeatedly until it returns false.
        print "Inside handle(self)"

        # set loco to forward
        print "Set Loco Forward"
        self.throttle.setIsForward(True)

        # wait 1 second for engine to be stopped, then set speed
        self.waitMsec(1000)     
        print "Set Speed"
        self.throttle.setSpeedSetting(0.7)

        # wait for run time in forward direction
        print "Wait for forward time"
        self.waitMsec(10000)

        # stop the engine
        print "Set Speed Stop"
        self.throttle.setSpeedSetting(0)

        # delay for a time (remember loco could still be moving
        # due to simulated or actual inertia).
        print "wait 3 seconds"
        self.waitMsec(3000)

        # set direction to reverse, set speed
        print "Set Loco Reverse"
        self.throttle.setIsForward(False)
        self.waitMsec(1000)                 # wait 1 second for Xpressnet to catch up
        print "Set Speed"
        self.throttle.setSpeedSetting(0.7)

        # wait for run time in reverse direction
        print "Wait for reverse time"
        self.waitMsec(10000)
        print "Set Speed Stop"
        self.throttle.setSpeedSetting(0)

        # delay for a time (remember loco could still be moving
        # due to simulated or actual inertia). Time is in milliseconds
        print "wait 3 seconds"
        self.waitMsec(3000)

        # and continue around again
        print "End of Loop"
        return 1
        # (requires JMRI to be terminated to stop - caution
        # doing so could leave loco running if not careful)

# end of class definition

# start one of these up
BackAndForthTimed().start()

