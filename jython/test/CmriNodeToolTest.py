# Test the jython/CmriNodeTool.py script to make sure it basically parses

import jmri

# create and self-register
c = jmri.jmrix.cmri.CMRISystemConnectionMemo()

execfile("jython/CmriNodeTool.py")

# need to kill the display
a.scriptFrame.dispose()
