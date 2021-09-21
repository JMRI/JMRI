# Test the ctc/TwoColumnMachine.py script
import java
import jmri

cm = jmri.InstanceManager.getDefault(jmri.ConfigureManager)
# load a version of the example fix that has no panel, so can run headless
cm.load(java.io.File(jmri.util.FileUtil.getExternalFilename("program:jython/test/TwoColumnMachineTest.xml")))

execfile("jython/ctc/TwoColumnMachine.py")

# press Code 1
sensors.getSensor("Sta 1 Code").setState(ACTIVE)

# check results (has to run immediately)
if (turnouts.getTurnout("Sta 1 Code").state != THROWN) : raise AssertionError('Code 1 not set')
if (turnouts.getTurnout("Sta 2 Code").state == THROWN) : raise AssertionError('Code 2 should not be set')
