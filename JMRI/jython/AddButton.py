# Sample script to add a button to the main
# JMRI application window that loads a script file
#
# Author: Bob Jacobsen, copyright 2007
# Part of the JMRI distribution

#
# NOTE: The recommended way to add a script button to JMRI is to
# use the Startup Items preferences to add that button.
#
# NOTE: This script does not support DecoderPro. Use the recommended
# method if you need to add a script to a button in DecoderPro.
#

import jmri

import javax.swing.JButton
import apps

# create the button, and add an action routine to it
b = javax.swing.JButton("Run my script")
def whenMyButtonClicked(event) :
    # run a script file
    execfile("jython/JButtonExample.py");
    return
b.actionPerformed = whenMyButtonClicked

# add the button to the main screen
apps.Apps.buttonSpace().add(b)

# force redisplay
apps.Apps.buttonSpace().revalidate()
