# Test that a simple Python automaton can be stopped cleanly
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution

import java
import jmri
import time

# run the script
execfile("jython/OpsProgExample.py")

name = "ops programming sample script"

# tell it to stop
jmri.jmrit.automat.AutomatSummary.instance().get(name).stop()

# confirm that it has stopped (might have to wait a here?)
time.sleep(1.0) # wait 1 second
for t in java.lang.Thread.getAllStackTraces().keySet() :
    if (t.getName() == name) :
        if (t.getState() != java.lang.Thread.State.TERMINATED) : raise AssertionError('thread did not TERMINATE')
 
