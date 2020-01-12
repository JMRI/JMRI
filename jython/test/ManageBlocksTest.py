# Test the ManageBlocks.py script - make sure it runs
import java
import java.awt

if ( not java.awt.GraphicsEnvironment.isHeadless()) :
    # just confirm that this runs OK in headed mode
    execfile("jython/ManageBlocks.py")

