# Custom script to decode multiple C/MRI inputs
# for Rich Ulaszek aspen4grich@gmail.com


import jmri

class DecodeSensorsToTurnouts(jmri.jmrit.automat.AbstractAutomaton) :

    # no init() routine, all covered in configure(..) and handle()

    def configure(self, triggerName, inputNames, outputNames) :
        # trigger is the name of the trigger sensors
        # inputNames is an array of the N input bits names, lowest to highest
        # outputNames is an array of turnout names to toggle, 0 to 2^N-1
        self.triggerName = triggerName
        self.inputNames = inputNames
        self.outputNames = outputNames

    def handle(self) :

        # gather references
        enable1 = sensors.provideSensor("CS1")
        enable2 = sensors.provideSensor("CS2")

        trigger = sensors.provideSensor(self.triggerName)

        # wait for either enable to be active
        while (enable1.getKnownState() != ACTIVE and enable2.getKnownState() != ACTIVE) :
            self.waitChange([enable1, enable2])

        #  wait for trigger to be active
        self.waitSensorActive(trigger)

        # compute the input
        outputIndex = 0
        bitValue = 1
        for name in self.inputNames :
            input = sensors.provideSensor(name)
            if (input.getKnownState() == ACTIVE) : outputIndex = outputIndex+bitValue
            bitValue = bitValue * 2

        # find and set the output
        turnout = turnouts.provideTurnout(self.outputNames[outputIndex])
        if (turnout.getCommandedState() == THROWN) :
            turnout.setCommandedState(CLOSED)
        else :
            turnout.setCommandedState(THROWN)

        # wait for trigger to be inactive
        self.waitSensorInactive(trigger)

        # go around again to handle the next one
        return True

# Create one of these operators, configure it to the original inputs and outputs, and start it
s = DecodeSensorsToTurnouts()
s.configure("CS6", ["CS3", "CS4", "CS5"], ["CT1","CT3","CT5","CT7","CT9","CT11","CT13","CT15"])
s.name = "For CS3 to CS6"
s.start()

# Create another one for a different set of inputs and outputs and start it running
# Do this on multiple lines to show how to do it with variables.
s = DecodeSensorsToTurnouts()
inputs = ["CS13", "CS14", "CS15"]
outputs = ["CT101","CT103","CT105","CT107","CT109","CT111","CT113","CT115"]
s.configure("CS16", inputs, outputs)
s.name = "For CS103 to CS106"
s.start()

