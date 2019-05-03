# Sample script to show put a button on the screen that
# will enable or disable local control of AD4 accessory decoders.
#
#
# This script has been superceded by the "Lock" capability in 
# JMRI 1.9.3 and later.  You can directly request that JMRI lock/unlock
# a turnout via the Turnout Table, Routes and Logi
#
# Author: Bob Jacobsen, copyright 2007
# Part of the JMRI distribution

import jmri

import java
import javax.swing

# create the button, and add an action routine to it
b = javax.swing.JCheckBox("Local Control Disabled")
def whenMyButtonClicked(event) :
        if (event.getSource().isSelected()) :
            programmer = addressedProgrammers.getAddressedProgrammer(True, 2041)
            programmer.writeCV("514", 0, None)  
        else :
            programmer = addressedProgrammers.getAddressedProgrammer(True, 2041)
            programmer.writeCV("514", 85, None)  
        return
b.actionPerformed = whenMyButtonClicked

# create a frame to hold the button, put button in it, and display
f = jmri.util.JmriJFrame("Turnout Control")
f.contentPane.add(b)
f.setLocation(50,100)   # pixels from left, pixels from top
f.pack()
f.show()

