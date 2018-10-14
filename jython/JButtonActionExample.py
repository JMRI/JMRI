# Slightly more advanced version of the 
# JButtonExample, with state and behavior in listener
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution

import jmri

import java
import java.awt
import java.awt.event
import javax.swing

# define a class that has some state
class MyButtonActionHandler(java.awt.event.ActionListener) : 
    def actionPerformed (self, event) :
            print self.name

# create a frame to hold the button, put button in it, and display
f = javax.swing.JFrame("custom button")
f.setLayout(java.awt.FlowLayout())

b = javax.swing.JButton("label 1")
h = MyButtonActionHandler()
h.name = "name 1"
b.addActionListener(h)
f.contentPane.add(b)

b = javax.swing.JButton("label 2")
h = MyButtonActionHandler()
h.name = "name 2"
b.addActionListener(h)
f.contentPane.add(b)

f.pack()
f.show()
