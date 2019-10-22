# Test the SetSignalFlashRate.py script
import jmri

execfile("jython/SetSignalFlashRate.py")

initial = jmri.implementation.DefaultSignalHead.masterDelay

jmri.implementation.DefaultSignalHead.masterDelay = 2*initial+1

if ( jmri.implementation.DefaultSignalHead.masterDelay != 2*initial+1) :
    raise AssertionError("Time not changed")
    
jmri.implementation.DefaultSignalHead.masterDelay = initial

if ( jmri.implementation.DefaultSignalHead.masterDelay != initial) :
    raise AssertionError("Time not reset")

