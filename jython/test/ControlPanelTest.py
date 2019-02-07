# Test the ControlPanel.py script - make sure it runs, then close its window
import java
import java.awt

if ( not java.awt.GraphicsEnvironment.isHeadless()) :
    # just confirm that this runs OK in headed mode
    execfile("jython/ControlPanel.py")
    f.dispose()

