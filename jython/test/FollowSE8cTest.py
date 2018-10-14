# Test the DebounceSensor.py script
import jmri

# test structure setup
v = jmri.implementation.VirtualSignalHead("IH100")
jmri.InstanceManager.getDefault(jmri.SignalHeadManager).register(v)

# example to be tested
execfile("jython/FollowSE8c.py")
FollowSE8c().set("IH100", "IT1", "IT2")

# drive test cases and confirm
turnouts.getTurnout("IT1").state = CLOSED
if (signals.getSignalHead('IH100').appearance != GREEN ) : raise AssertionError('GREEN not driven')

turnouts.getTurnout("IT1").state = THROWN
if (signals.getSignalHead('IH100').appearance != RED ) : raise AssertionError('RED not driven')

turnouts.getTurnout("IT2").state = CLOSED
if (signals.getSignalHead('IH100').appearance != DARK ) : raise AssertionError('DARK not driven')

turnouts.getTurnout("IT2").state = THROWN
if (signals.getSignalHead('IH100').appearance != YELLOW ) : raise AssertionError('YELLOW not driven')

