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

import jmri

import java
import java.awt
import javax.swing

# create a frame to hold the button(s), put button in it, and display
f = javax.swing.JFrame("Panel Controls")
f.getContentPane().setLayout(java.awt.FlowLayout())

# define action routine for button click
def whenMyButtonClicked(event) :
        name  = event.getActionCommand()
        # find any PanelEditor panel(s) to show
        for panel in jmri.InstanceManager.getDefault(jmri.jmrit.display.PanelMenu).getEditorPanelList() :
            if (name == panel.getTitle()) : 
                panel.setVisible(not panel.isVisible())
                return            
        return
        
# Now loop to create buttons.
#
# The "action" in each button is the name of the panel.
# When clicked, it provides that name.  We 
# then use a map between that name and the actual 
# window produced from the file, so we can show/hide the 
# proper panel window.

# loop, creating a button for each panel
for panel in jmri.InstanceManager.getDefault(jmri.jmrit.display.PanelMenu).getEditorPanelList() :
    # create a button for this panel
    b = javax.swing.JButton(panel.getTitle())
    b.actionPerformed = whenMyButtonClicked
    f.contentPane.add(b)
    

# show the control panel frame
f.pack()
f.setVisible(True)
