# This is an example script for a JMRI "Siglet" in Python
#
# It listens for changes to two sensors and a turnout,
# and then recalculates a signal aspect based on the values
#
# Author: Bob Jacobsen, copyright 2004
# Part of the JMRI distribution

import jarray
import jmri

class SigletExample(jmri.jmrit.automat.Siglet) :
    # defineIO() is called exactly once at the beginning, and is
    # required to load the "inputs" array with the turnouts, sensors
    # and signal heads that are used. Any changes in these will result
    # in setOutput being called to recalculate the result.
    #
    # Modify this to define all of your turnouts, sensors and
    # signal heads.
    def defineIO(self):
   
        self.to12 = turnouts.provideTurnout("12")
   
        self.bo21 = sensors.provideSensor("21")
        self.bo22 = sensors.provideSensor("22")
   
        # The signalhead should have been previously defined. Since this
        # is an example, we detect whether it was already defined, and
        # if not we create a particular signal head.
        self.si35 = signals.getSignalHead("35")
        if (self.si35 == None) :
            self.si35 = jmri.TripleTurnoutSignalHead("si35", "",
                    turnouts.provideTurnout("101"),
                    turnouts.provideTurnout("102"),
                    turnouts.provideTurnout("103"))
            signals.register(self.si35)
   
        # Register the inputs so setOutput will be called when needed.
        # Note that the output si35 should _not_ in included as an input.
        self.setInputs(jarray.array([self.to12, self.bo21, self.bo22], jmri.NamedBean))

        return

    # setOutput is called when one of the inputs changes, and is
    # responsible for setting the correct output
    #
    # Modify this to do your calculation.
    def setOutput(self):
        newvalue = RED
   
        if self.to12.commandedState==THROWN:
            if self.bo21.knownState==INACTIVE:
                newvalue = GREEN
        else:
            if self.bo22.knownState==INACTIVE:
                newvalue = GREEN
   
        # set the signal aspect to the new value
        self.si35.appearance = newvalue;

        # print the value for diagnostic purposes
        print "output set to ", newvalue
   
        return

# end of class definition

# start one of these up
SigletExample().start()
