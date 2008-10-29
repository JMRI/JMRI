# Sample script to show a set of JButtons that
# show/hide panel windows when clicked.
#
# When this script is run, it finds all open panels
# (for either PanelEditor or LayoutEditor)
# and creates a small window with a button for each panel.
# The buttons are labelled with the names of the panels, 
# and it's required that those names be unique.
#
# Author: Bob Jacobsen, copyright 2006,2008
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.2 $

import java
import javax.swing

# create a frame to hold the button(s), put button in it, and display
f = javax.swing.JFrame("Panel Controls")
f.getContentPane().setLayout(java.awt.FlowLayout())

# define action routine for button click
def whenMyButtonClicked(event) :
        name  = event.getActionCommand()
        # find any PanelEditor panel(s) to show
        i = 1
        panel = jmri.InstanceManager.configureManagerInstance().findInstance(
            java.lang.Class.forName("jmri.jmrit.display.PanelEditor"),
            i)
        while (panel != None) :
            if (name == panel.getFrame().getTitle()) : 
                panel.getFrame().setVisible(not panel.getFrame().isVisible())
                return            
            # loop again
            i = i + 1
            panel = jmri.InstanceManager.configureManagerInstance().findInstance(
                java.lang.Class.forName("jmri.jmrit.display.PanelEditor"),
                i)
        # find any LayoutEditor panel(s) to show
        i = 1
        panel = jmri.InstanceManager.configureManagerInstance().findInstance(
            java.lang.Class.forName("jmri.jmrit.display.LayoutEditor"),
            i)
        while (panel != None) :
            if (name == panel.getTitle()) : 
                panel.setVisible(not panel.isVisible())
                return            
            # loop again
            i = i + 1
            panel = jmri.InstanceManager.configureManagerInstance().findInstance(
                java.lang.Class.forName("jmri.jmrit.display.LayoutEditor"),
                i)

# Now loop to create buttons.
#
# The "action" in each button is the name of the panel.
# When clicked, it provides that name.  We 
# then use a map between that name and the actual 
# window produced from the file, so we can show/hide the 
# proper panel window.

# first, panel editor panels
i = 1
panel = jmri.InstanceManager.configureManagerInstance().findInstance(
    java.lang.Class.forName("jmri.jmrit.display.PanelEditor"),
    i)

#loop, creating a button for each panel
while (panel != None) :
    # create a button for this panel
    b = javax.swing.JButton(panel.getFrame().getTitle())
    b.actionPerformed = whenMyButtonClicked
    f.contentPane.add(b)
    
    # loop again
    i = i + 1
    panel = jmri.InstanceManager.configureManagerInstance().findInstance(
        java.lang.Class.forName("jmri.jmrit.display.PanelEditor"),
        i)
    
# second, layout editor panels
i = 1
panel = jmri.InstanceManager.configureManagerInstance().findInstance(
    java.lang.Class.forName("jmri.jmrit.display.LayoutEditor"),
    i)

#loop, creating a button for each panel
while (panel != None) :
    # create a button for this panel
    b = javax.swing.JButton(panel.getTitle())
    b.actionPerformed = whenMyButtonClicked
    f.contentPane.add(b)
    
    # loop again
    i = i + 1
    panel = jmri.InstanceManager.configureManagerInstance().findInstance(
        java.lang.Class.forName("jmri.jmrit.display.LayoutEditor"),
        i)
    

# show the control panel frame
f.pack()
f.setVisible(True)

