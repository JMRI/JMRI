# Test the jython/CmriNodeTool.py script to make sure it basically parses

import jmri
import java
import java.awt

# create and self-register
c = jmri.jmrix.cmri.CMRISystemConnectionMemo()

if (not java.awt.GraphicsEnvironment.isHeadless()) : 
    execfile("jython/CmriNodeTool.py")
    # need to kill the display
    a.scriptFrame.dispose()

jmri.util.JUnitUtil.clearShutDownManager()
