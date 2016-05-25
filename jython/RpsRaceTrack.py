# RaceTrack using RPS position measurement
#
# loco1 is faster, loco2 is slower.
# When loco1 comes within dmin=24 inches
# of loco2, it will:
#   slow to 1/4 of it's speed
#   wait 6 seconds
#   return to it's original speed
# and check again
#
# This is time-based (check every 200 msec), rather
# than measurement based (listeners) for ease of debugging.
# Changing it over later on will be good.
#
# Author:  Bob Jacobsen (C) Copyright 2008
# Part of the JMRI distribution

import jarray
import jmri

class RpsRaceTrack(jmri.jmrit.automat.AbstractAutomaton) :

    def init(self):
        loco1 = 42
        loco2 = 15
        # set up transmitter and throttle numbers
        self.rps = jmri.jmrix.rps.Engine.instance()
        self.t1 = self.rps.getTransmitterByAddress(loco1)
        print "t1 address",self.t1.getAddress()
        self.t2 = self.rps.getTransmitterByAddress(loco2)
        print "t2 address",self.t2.getAddress()
        
        # get loco address. For long address change "False" to "True" 
        long = False
        if (self.t1.getAddress()>100) :
            long = True
        self.throttle = self.getThrottle(self.t1.getAddress(), long)
        
        return

    def handle(self):
    
        # tuning
        dmin = 24
        
        # each turn of the loop, get positions
        
        self.p1 = self.t1.getLastMeasurement().getPoint()
        #print "t1 position ",self.p1

        self.p2 = self.t2.getLastMeasurement().getPoint()
        #print "t2 position ",self.p2
        
        self.d = self.p1.distance(self.p2)
        #print "distance",self.d
        
        if (self.d<dmin) :
            # slow down
            spd = self.throttle.getSpeedSetting()
            nspd = spd*0.25
            print "set speed from",nspd," to ",spd
            self.throttle.setSpeedSetting(nspd)
            self.waitMsec(6000)
            self.throttle.setSpeedSetting(spd)
            return 1
        # OK, so let run
        # wait for layout to catch up
        self.waitMsec(200)                 

        # and continue around again
        print "End of Loop"
        return 1    
        # (requires JMRI to be terminated to stop - caution
        # doing so could leave loco running if not careful)

# end of class definition

# start one of these up
RpsRaceTrack().start()

