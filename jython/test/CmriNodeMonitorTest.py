# Test the jython/CmriNodeMonitor.py script to make sure it basically parses

import jmri
import java
import java.awt

# create and self-register
c = jmri.jmrix.cmri.CMRISystemConnectionMemo()

if (not java.awt.GraphicsEnvironment.isHeadless()) : 
    execfile("jython/CmriNodeMonitor.py")
    # need to kill the thread
    jmri.jmrit.automat.AutomatSummary.instance().get("CmriNodeMonitor").stop()

jmri.util.JUnitUtil.clearShutDownManager()
