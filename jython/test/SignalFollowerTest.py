# Test the SignalFollower.py script
import jmri

# test structure setup
v = jmri.implementation.VirtualSignalHead("IH100")
jmri.InstanceManager.getDefault(jmri.SignalHeadManager).register(v)
v = jmri.implementation.VirtualSignalHead("IH100R")
jmri.InstanceManager.getDefault(jmri.SignalHeadManager).register(v)

execfile("jython/SignalFollower.py")

SignalFollowerListener().set("IH100","IH100R")

signals.getSignalHead("IH100").setAppearance(RED)
if (signals.getSignalHead("IH100R").getAppearance() != RED) : raise AssertionError('Set RED did not follow')

signals.getSignalHead("IH100").setAppearance(GREEN)
if (signals.getSignalHead("IH100R").getAppearance() != GREEN) : raise AssertionError('Set GREEN did not follow')

signals.getSignalHead("IH100").setAppearance(FLASHYELLOW)
if (signals.getSignalHead("IH100R").getAppearance() != FLASHYELLOW) : raise AssertionError('Set FLASHYELLOW did not follow')
