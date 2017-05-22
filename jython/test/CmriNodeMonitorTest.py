# Test the jython/CmriNodeMonitor.py script to make sure it basically parses

import jmri

# create and self-register
c = jmri.jmrix.cmri.CMRISystemConnectionMemo()

execfile("jython/CmriNodeMonitor.py")

# need to kill the thread
jmri.jmrit.automat.AutomatSummary.instance().get("CmriNodeMonitor").stop()
