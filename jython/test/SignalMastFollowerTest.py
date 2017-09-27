# Test the SignalMastFollower.py script
import jmri

# test structure setup
v = jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-low($0001)", "Mast 1")
jmri.InstanceManager.getDefault(jmri.SignalMastManager).register(v)
v = jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-low($0002)", "Mast 1 R")
jmri.InstanceManager.getDefault(jmri.SignalMastManager).register(v)

execfile("jython/SignalMastFollower.py")

SignalMastFollowerListener().set("Mast 1","Mast 1 R")

masts.getSignalMast("Mast 1").setAspect("Clear")
if (masts.getSignalMast("Mast 1 R").getAspect() != "Clear") : raise AssertionError('Set Clear did not follow')

masts.getSignalMast("Mast 1").setAspect("Stop")
if (masts.getSignalMast("Mast 1 R").getAspect() != "Stop") : raise AssertionError('Set Stop did not follow')

masts.getSignalMast("Mast 1").setAspect("Approach")
if (masts.getSignalMast("Mast 1 R").getAspect() != "Approach") : raise AssertionError('Set Approach did not follow')
