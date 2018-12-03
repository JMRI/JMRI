# This is an example script for a JMRI "Siglet" in Python
#
# It listens to a Memory object, and prints the new state to stdout
#
# Author: Bob Jacobsen, copyright 2004
# Part of the JMRI distribution

import jarray
import jmri

class MemorySiglet(jmri.jmrit.automat.Siglet) :

    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def defineIO(self):

        # get the sensor and throttle objects
        self.mem = memories.provideMemory("12")

        # Register the inputs so setOutput will be called when needed.
        # Note that the output si35 should _not_ in included as an input.
        self.setInputs(jarray.array([self.mem], jmri.NamedBean))

        return

    # handle() is called repeatedly until it returns false.
    #
    # Modify this to do your calculation.
    def setOutput(self):

        # this example doesn't actually do anything

        # and continue around again
        return 1    # to continue

# end of class definition

# create one of these
a = MemorySiglet()

# set the name, as a example of configuring it
a.setName("MemorySiglet example script")

# and start it running
a.start()
