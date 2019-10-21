# Test the SetSignalFlashRate.py script
import jmri

if ( jmri.implementation.DefaultSignalHead.masterDelay == 1000) :
    raise AssertionError("We cannot test and verify the change if the original value is the same as in the script")

execfile("jython/SetSignalFlashRate.py")

if ( jmri.implementation.DefaultSignalHead.masterDelay != 1000) :
    raise AssertionError("Time not changed")

