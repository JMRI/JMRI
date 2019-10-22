# Test the SetSignalFlashRate.py script
import jmri

if ( jmri.implementation.DefaultSignalHead.masterDelay == 1000) :
    raise AssertionError("We cannot test and verify the change if the original value is the same as in the script")

execfile("jython/SetSignalFlashRate.py")

initial = jmri.implementation.DefaultSignalHead.masterDelay

jmri.implementation.DefaultSignalHead.masterDelay = 2*initial+1

if ( jmri.implementation.DefaultSignalHead.masterDelay != 2*initial+1) :
    raise AssertionError("Time not changed")
    
jmri.implementation.DefaultSignalHead.masterDelay = initial

if ( jmri.implementation.DefaultSignalHead.masterDelay != initial) :
    raise AssertionError("Time not reset")

