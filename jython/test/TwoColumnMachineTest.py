# Test the ctc/TwoColumnMachine.py script
import jmri

execfile("jython/ctc/TwoColumnMachine.py")

# press Code 1
sensors.getSensor("Sta 1 Code").setState(ACTIVE)

# check results
if (turnouts.getTurnout("Sta 1 Code").state != THROWN) : raise AssertionError('Code 1 not set')
if (turnouts.getTurnout("Sta 2 Code").state == CLOSED) : raise AssertionError('Code 2 should not be set')
