# Sample script to show put a button on the screen that
# will enable or disable local control of AD4 accessory decoders.
#
# Author: Bob Jacobsen, copyright 2007
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

import java
import javax.swing

# create the button, and add an action routine to it
b = javax.swing.JCheckBox("Local Control Disabled")
def whenMyButtonClicked(event) :
        if (event.getSource().isSelected()) :
            programmer = programmers.getOpsModeProgrammer(True, 2041)
            programmer.writeCV(514, 0, None)  
        else :
            programmer = programmers.getOpsModeProgrammer(True, 2041)
            programmer.writeCV(514, 85, None)  
        return
b.actionPerformed = whenMyButtonClicked

# create a frame to hold the button, put button in it, and display
f = jmri.util.JmriJFrame("Turnout Control")
f.contentPane.add(b)
f.setLocation(50,100)   # pixels from left, pixels from top
f.pack()
f.show()

