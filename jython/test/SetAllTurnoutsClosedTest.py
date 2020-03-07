
# Test SetAllTurnoutsClosed.py script
import jmri

# test structure setup
turnouts.provideTurnout("IS1").setState(UNKNOWN)
turnouts.provideTurnout("IS2").setState(CLOSED)
turnouts.provideTurnout("IS3").setState(THROWN)
turnouts.provideTurnout("IS4").setState(INCONSISTENT)

# run script
execfile("jython/SetAllTurnoutsClosed.py")

# test resuts
if (turnouts.provideTurnout("IS1").getState() != CLOSED) : raise AssertionError('UNKNOWN not set to CLOSED')
if (turnouts.provideTurnout("IS2").getState() != CLOSED) : raise AssertionError('CLOSED not left at CLOSED')
if (turnouts.provideTurnout("IS3").getState() != CLOSED) : raise AssertionError('THROWN not set to CLOSED')
if (turnouts.provideTurnout("IS4").getState() != CLOSED) : raise AssertionError('INCONSISTENT not set to CLOSED')
