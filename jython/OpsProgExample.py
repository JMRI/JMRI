# This sample Automaton watches a Sensor, and adjusts
# the momentum of a locomotive using ops-mode programming
# when the sensor state changes.
# 
# The sensor and decoder are hardcoded, as this is
# an example of just the Automaton function.  Adding a GUI
# to configure these would be straight-forward. The values
# could be passed via the constructor, or the constructor
# (which can run in any required thread) could invoke
# a dialog.

# Author: Bob Jacobsen, copyright 2004, 2005
# Part of the JMRI distribution

import jmri

class OpsProgExample(jmri.jmrit.automat.AbstractAutomaton) :

    #
    # By default, monitors sensor "32" and controls locomotive 1234(long address).
    #
    #
    def init(self) :
    
        # set some default values
        self.sensorName = "32"
        self.locoNumber = 1234
        self.longAddress = True
        
        # get references to sample layout objects
        self.sensor = sensors.provideSensor(self.sensorName)
        self.programmer = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager).getAddressedProgrammer(self.longAddress, self.locoNumber)

        # get initial state and set the CV as needed
        self.now = self.sensor.getKnownState()
        self.setMomentum()
        
        return

    #
    # Watch "sensor", and when it changes the momentum CV to match.
    # Always returns true to continue operation
    def handle(self) :

        # wait until the sensor changes state
        self.waitSensorChange(self.now, self.sensor)

        # get new value
        self.now = self.sensor.getKnownState()

        # match the decoder's momentum
        self.setMomentum()

        return True;   # never terminate voluntarily

    # Set CV3, acceleration momentum, to match the sensor state.
    # When the sensor is active, set the momentum to 30;
    # when inactive, set the momentum to 0.
    def setMomentum(self) :
        if (self.now == ACTIVE) :
            self.programmer.writeCV("3", 30, None)
        else :
            self.programmer.writeCV("3", 0, None)

        return
        
# end of class definition

# create one of these
a = OpsProgExample()

# set the name, as a example of configuring it
a.setName("ops programming sample script")

# and start it running
a.start()

