# Sample script to show a JButton which prints a little
# message when clicked.  The print statement can be 
# changed to include whatever desired, e.g. throw a turnout,
# program a CV, etc.
#
# Author: Bob Jacobsen, copyright 2004
# Part of the JMRI distribution

import jmri

import java
import javax.swing

# create the button, and add an action routine to it
b = javax.swing.JButton("custom button")
def whenMyButtonClicked(event) :
        print "clicked!"
b.actionPerformed = whenMyButtonClicked

# create a frame to hold the button, put button in it, and display
f = javax.swing.JFrame("custom button")
f.contentPane.add(b)
f.pack()
f.show()

