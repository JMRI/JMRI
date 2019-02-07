# Test that a simple Python automaton can be stopped cleanly
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution

import java
import jmri
import time

class AutomatonStopTest(jmri.jmrit.automat.AbstractAutomaton) :

    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):
        return

    # sample subroutine
    def mycode(self) :
        # wait for some time
        self.waitMsec(1000)
        return

    # handle() is called repeatedly until it returns false.
    #
    # Modify this to do your calculation.
    def handle(self):
        # do the work
        self.mycode()
        # and continue around again forever
        return 1    # to continue

# end of class definition

# create one of these
a = AutomatonStopTest()

# set the name, as a example of configuring it
name = "AutomatonStopTest test thread"
a.setName(name)

# start it running
a.start()

# tell it to stop
jmri.jmrit.automat.AutomatSummary.instance().get(name).stop()

# confirm that it has stopped (might have to wait a here?)
time.sleep(1.0) # wait 1 second
for t in java.lang.Thread.getAllStackTraces().keySet() :
    if (t.getName() == name) :
        if (t.getState() != java.lang.Thread.State.TERMINATED) : raise AssertionError('thread did not TERMINATE')

