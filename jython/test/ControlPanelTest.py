# Test the ControlPanel.py script - make sure it runs, then close its window
import java
if (java.awt.GraphicsEnvironment.isHeadless()) :
    # just confirm that this runs OK in headless mode (when graphical, it prompts)
    execfile("jython/ControlPanel.py")
    f.dispose()

