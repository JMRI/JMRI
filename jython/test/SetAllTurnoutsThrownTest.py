# Test SetAllTurnoutsThrown.py script
import jmri
from time import sleep

# test structure setup
turnouts.provideTurnout("IS1").setState(UNKNOWN)
turnouts.provideTurnout("IS2").setState(CLOSED)
turnouts.provideTurnout("IS3").setState(THROWN)
turnouts.provideTurnout("IS4").setState(INCONSISTENT)

# run script
execfile("jython/SetAllTurnoutsThrown.py")

sleep(1.5)
# test resuts
if (turnouts.provideTurnout("IS1").getState() != THROWN) : raise AssertionError('UNKNOWN not set to THROWN')
if (turnouts.provideTurnout("IS2").getState() != THROWN) : raise AssertionError('CLOSED not set to THROWN')
if (turnouts.provideTurnout("IS3").getState() != THROWN) : raise AssertionError('THROWN not left at THROWN')
if (turnouts.provideTurnout("IS4").getState() != THROWN) : raise AssertionError('INCONSISTENT not set to tHROWN')
