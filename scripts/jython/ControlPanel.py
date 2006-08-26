# Sample script to show a set of JButtons that
# show/hide display panels when clicked.
#
# When this script is run, it finds all open panels
# and creates a small window with a button for each panel.
# The buttons are labelled with the names of the panels, 
# and it's required that those names be unique.
#
# Author: Bob Jacobsen, copyright 2006
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

import java
import javax.swing

# create a frame to hold the button(s), put button in it, and display
f = javax.swing.JFrame("Panel Controls")
f.getContentPane().setLayout(java.awt.FlowLayout())

# define action routine for button click
def whenMyButtonClicked(event) :
        name  = event.getActionCommand()
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

# initialize loop to create buttons
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
    

# The "action" in each button is the name of the panel
# file.  When clicked, it provides that name.  We 
# then use a map between that name and the actual 
# window produced from the file, so we can show/hide the 
# proper panel window.

# create the button, and add an action routine to it

# after creating the panel initially, search for an object
# of the right type with the right name

# show the control panel frame
f.pack()
f.setVisible(True)

