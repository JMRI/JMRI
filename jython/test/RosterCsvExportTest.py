# Test the RosterCsvExport.py script

import java
import java.awt
import java.awt.GraphicsEnvironment

if (java.awt.GraphicsEnvironment.isHeadless()) :
    execfile("jython/RosterCsvExport.py")

# just confirm that this runs OK in headless mode (when graphical, it prompts)
