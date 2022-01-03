# Test the InstanceManager accessor forms
import jmri

m1 = jmri.InstanceManager.getDefault(jmri.TurnoutManager)
m2 = jmri.InstanceManager.getDefault("jmri.TurnoutManager")

if (m1 != m2) : raise AssertionError("accessor results don't match")

