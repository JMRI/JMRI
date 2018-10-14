# Test the SetDefaultDelayedTurnoutDelayTest.py script

import jmri

# capture original value (also checks still accessible)
original = jmri.implementation.AbstractTurnout.DELAYED_FEEDBACK_INTERVAL

execfile("jython/SetDefaultDelayedTurnoutDelay.py")

# check that it changed
if (jmri.implementation.AbstractTurnout.DELAYED_FEEDBACK_INTERVAL == original) : raise AssertionError('Value not changed')

# restore
jmri.implementation.AbstractTurnout.DELAYED_FEEDBACK_INTERVAL = original
