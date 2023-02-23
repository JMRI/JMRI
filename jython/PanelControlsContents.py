# Used to locate a Panel Editor or Control Panel Editor panel and set
# it to control or not control the layout.
#
# Bob Jacobsen 2023

# The first two lines determine what will be done

panelName = "My Panel"
controlsLayout = False

# get the logger
import org.slf4j
log = org.slf4j.LoggerFactory.getLogger("PanelControlsContents.py")

# Find the panel frame's Editor frame by name
import jmri
frame = jmri.util.JmriJFrame.getFrame(panelName+" Editor")
if frame == None :
    log.error("Frame not found")
else :
    print frame
    # set whether frame contents control layout for all objects
    for content in frame.getContents() :
        # add test for a specific icon name(s) if desired to just do part
        content.setControlling(controlsLayout)


