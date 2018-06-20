# Test that a simple Python automaton can be stopped cleanly
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution

import java
import jmri
import time

# The following line is to set up a programmer for a test without a system connection, not needed usually
jmri.InstanceManager.setDefault(jmri.AddressedProgrammerManager, jmri.progdebugger.DebugProgrammerManager())

# run the script
execfile("jython/OpsProgExample.py")

name = "ops programming sample script"

# wait a bit to operate
time.sleep(1.0) # wait 1 second

# tell it to stop
jmri.jmrit.automat.AutomatSummary.instance().get(name).stop()

time.sleep(0.5) # wait 0.5 second
# confirm that it has stopped
for t in java.lang.Thread.getAllStackTraces().keySet() :
    if (t.getName() == name) :
        if (t.getState() != java.lang.Thread.State.TERMINATED) : raise AssertionError('thread did not TERMINATE')
 
