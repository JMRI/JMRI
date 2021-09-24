# Test the jmrit.uss package jython/ctc/TwoColumnMachine.py sample script
import java
import jmri

cm = jmri.InstanceManager.getDefault(jmri.ConfigureManager)
# load a version of the example fix that has no panel, so can run headless
cm.load(java.io.File(jmri.util.FileUtil.getExternalFilename("program:jython/ctc/TwoColumnMachineTest.xml")))

# run a version without a computer bell
execfile("jython/ctc/TwoColumnMachineTest.py")

# press Code 1
sensors.getSensor("Sta 1 Code").setState(ACTIVE)

# check results (has to run immediately)
if (turnouts.getTurnout("Sta 1 Code").state != THROWN) : raise AssertionError('Code 1 not set')
if (turnouts.getTurnout("Sta 2 Code").state == THROWN) : raise AssertionError('Code 2 should not be set')
