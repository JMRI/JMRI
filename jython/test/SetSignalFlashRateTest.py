# Test the SetSignalFlashRate.py script
import jmri

execfile("jython/SetSignalFlashRate.py")

if ( jmri.implementation.DefaultSignalHead.masterDelay != 1000) :
    raise AssertionError("Time not changed")

