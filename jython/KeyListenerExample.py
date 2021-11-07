# Sample script to how to listen to the keyboard.
# prints a little message when a key is clicked.  The print statement can be
# changed to include whatever desired, e.g. throw a turnout,
# program a CV, etc.
#
# Author: Bob Jacobsen, copyright 2004, 2021
# Part of the JMRI distribution

import jmri

import java
import javax.swing

# create the button, and add an action routine to it
b = javax.swing.JLabel("Type With This Window Active")

class ListenToKey(java.awt.event.KeyAdapter):
    def keyPressed(self, event) : # event is KeyEVent
        print (event.keyCode)
        # if you want to find a specific one
        if(event.keyCode == java.awt.event.KeyEvent.VK_SPACE) :
                 print ("Space bar!")

# create a frame, put label in it, and display
f = javax.swing.JFrame("KeyListenerExample")
f.addKeyListener( ListenToKey() )
b.addKeyListener( ListenToKey() )
f.contentPane.addKeyListener( ListenToKey() )

f.contentPane.add(b)
f.pack()
f.show()

