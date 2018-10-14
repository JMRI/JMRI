# Script to go through each open panel, walk down the object 
# hierarchy and search for all ReporterIcon objects.
#
# Every ReporterIcon will have its font size and colour set as  
# specified at the top of the script
#
# In addition to being a useful tool in itself, this is a good example
# of stepping through the object structure of a panel.
#
# Author: Dennis Miller
# Parts of this script are based on the ControlPanel.py 
# script written by Bob Jacobsen
#
# Part of the JMRI distribution

import jmri
import java
import java.awt

# set the desired colour and size in the two lines below.
# Many normal colour names can be used instead of WHITE 
# (e.g. RED, BLUE, GREEN)
reporterColor = java.awt.Color.WHITE
fontSize = 12


# initialize loop to find all panel editors
i = 1
editorList = []
editor = jmri.InstanceManager.getDefault(jmri.ConfigureManager).findInstance(
    java.lang.Class.forName("jmri.jmrit.display.PanelEditor"),
    i)

# loop, adding each editor found to the list
while (editor != None) : 
    editorList.append(editor)
    # loop again
    i = i + 1
    editor = jmri.InstanceManager.getDefault(jmri.ConfigureManager).findInstance(
        java.lang.Class.forName("jmri.jmrit.display.PanelEditor"),
        i)
    
# Now we have a list of editors.
# For each editor, get the related panel and walk down 
# its object hierarchy until the widgets themselves are reached    
for editor in editorList:
    panel = editor.getFrame()
    root = panel.getComponents()[0]
    pane = root.getComponents()[1]
    jpanel = pane.getComponents()[0]
    jpanel2 = jpanel.getComponents()[0]
    scrollpane = jpanel2.getComponents()[0]
    viewport = scrollpane.getComponents()[0]
    controlpanel = viewport.getComponents()[0]
    
    # now we can finally get at the widgets themselves
    widgetList = controlpanel.getComponents()
    
    # Check each widget to see if it is a ReporterIcon    
    for widget in widgetList:
        if (isinstance(widget, jmri.jmrit.display.ReporterIcon)):
            # This is a ReporterIcon, so change it
            widget.setForeground(reporterColor)
            widget.setFontSize(fontSize)

    



