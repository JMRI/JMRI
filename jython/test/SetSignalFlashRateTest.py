# Test the SetSignalFlashRate.py script
import jmri


initial = jmri.implementation.DefaultSignalHead.masterDelay

# protect the test case
if ( initial == 1000) : jmri.implementation.DefaultSignalHead.masterDelay = 750

execfile("jython/SetSignalFlashRate.py")  # should set to 1000

if ( jmri.implementation.DefaultSignalHead.masterDelay != 1000) :
    raise AssertionError("Time not changed")
    
jmri.implementation.DefaultSignalHead.masterDelay = initial


