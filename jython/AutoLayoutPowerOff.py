# This script turns the off power to the layout after
# a specified time has passed with no activity. ('Activity'
# means a change in state of a sensor used as a block
# occupancy detector.) It's a sort of idle timeout for 
# track power. It is intended to be run from the startup 
# menu.
#
# The timeout is set at the top, in minutes.
#
# Author: Steve Rawlinson, 2016
# Part of the JMRI distribution

import time
import jmri

# Edit this:
timeOutInMinutes = 30
debug = False


class AutoLayoutPowerOff(jmri.jmrit.automat.AbstractAutomaton) :
        
        
    def init(self) :
        # set the timeout
        self.timeout = timeOutInMinutes
        # get the set of sensors in use as block occupancy detectors
        self.blockset = blocks.getNamedBeanSet()
        self.blockOccupancySensors = []
        for b in self.blockset :
            s = b.getSensor()
            if s != None :
                self.blockOccupancySensors.append(s)
        
                
    def handle(self):
        
        if debug :
            print "AutoLayoutPowerOff: starting up"

        # get the current power state
        self.powerState = powermanager.getPower()
        if debug :
            if self.powerState  != jmri.PowerManager.ON :
                print "AutoLayoutPowerOff: Layout power is off, sleeping until it comes on"

        # If the power is off, do nothing, checking every minute
        while powermanager.getPower() != jmri.PowerManager.ON :
            time.sleep(60)
        
        if debug :
            print "AutoLayoutPowerOff: Layout Power is on"

        while True : # we break out after the timeout
            if debug :
                print "AutoLayoutPowerOff: monitoring activity on", len(self.blockOccupancySensors), "sensors"
            startTime = time.time()
            self.waitChange(self.blockOccupancySensors, self.timeout * 60 * 1000)
            stopTime = time.time()
            if stopTime - startTime > self.timeout * 60 :
                print "AutoLayoutPowerOff: timeout waiting for activity, turning layout power OFF"
                powermanager.setPower(jmri.PowerManager.OFF)
                return True # start again
            else :
                if debug  :
                    print "AutoLayoutPowerOff: activity detected at", stopTime
            

AutoLayoutPowerOff().start()
