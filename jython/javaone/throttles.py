#
# Set global variable to a Throttle
#

import jmri

class LoadThrottle(jmri.jmrit.automat.AbstractAutomaton) :
        
    # init gets and saves the throttle object
    def init(self):
        
        global throttleA
        throttleA = self.getThrottle(addressA, longA)
        if throttleA == None :
            print "Couldn't assign throttleA!"

        global throttleB
        throttleB = self.getThrottle(addressB, longB)
        if throttleB == None :
            print "Couldn't assign throttleB!"

        if throttleB == None or throttleA == None :
            warn().display("could not allocate throttles; reset LocoNet and restart")
            # crash off the layout
            jmri.InstanceManager.getDefault(jmri.PowerManager).setPower(jmri.PowerManager.OFF)
            return
        else : return

    # handle() is not needed for this function
    def handle(self):
        return False

# end of class definition

# do it
LoadThrottle().start()

