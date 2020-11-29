# Test SetUnknownTurnoutsClosed.py script
import jmri

# test structure setup
turnouts.provideTurnout("IS1").setState(UNKNOWN)
turnouts.provideTurnout("IS2").setState(CLOSED)
turnouts.provideTurnout("IS3").setState(THROWN)
turnouts.provideTurnout("IS4").setState(INCONSISTENT)

# run script
execfile("jython/SetUnknownTurnoutsClosed.py")

# test resuts
if (turnouts.provideTurnout("IS1").getState() != CLOSED) : raise AssertionError('UNKNOWN not set to CLOSED')
if (turnouts.provideTurnout("IS2").getState() != CLOSED) : raise AssertionError('CLOSED not left at CLOSED')
if (turnouts.provideTurnout("IS3").getState() != THROWN) : raise AssertionError('THROWN not left at THROWN')
if (turnouts.provideTurnout("IS4").getState() != INCONSISTENT) : raise AssertionError('INCONSISTENT not left at INCONSISTENT')
