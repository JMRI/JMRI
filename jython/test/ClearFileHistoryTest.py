# Test the ClearFileHistory.py script
import jmri
jmri.InstanceManager.getDefault(jmri.jmrit.revhistory.FileHistory).addOperation("Test", "", jmri.jmrit.revhistory.FileHistory())  # ensure at least one level

execfile("jython/ClearFileHistory.py")

# check that history is empty
if (jmri.InstanceManager.getDefault(jmri.jmrit.revhistory.FileHistory).getList()[0].history != None) : raise AssertionError('History not empty')
