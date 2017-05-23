# Test the jython/CmriNodeTool.py script to make sure it basically parses

import jmri
import java

# create and self-register
c = jmri.jmrix.cmri.CMRISystemConnectionMemo()

if (not java.awt.GraphicsEnvironment.isHeadless()) : 
    execfile("jython/CmriNodeTool.py")
    # need to kill the display
    a.scriptFrame.dispose()
