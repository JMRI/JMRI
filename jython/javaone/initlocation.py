#
# set locomotive into blocks
#

import java
import javax.swing

import jarray
items = jarray.array(["150","151","152","153","154","155","156","157","158","159","160","161","200","201","202","203"], java.lang.Object)



def whenAButtonClicked(event) :
        print "Set throttle A in "+ja.getSelectedItem()
        cmd = "print IB"+ja.getSelectedItem()+".setValue(throttleA)"
        exec(cmd)

def whenBButtonClicked(event) :
        print "Set throttle B in "+jb.getSelectedItem()
        cmd = "print IB"+jb.getSelectedItem()+".setValue(throttleB)"
        exec(cmd)


# create a frame to hold the B button, put button in it, and display
jb= javax.swing.JComboBox(items)
b = javax.swing.JButton("Set Location")
b.actionPerformed = whenBButtonClicked
f = javax.swing.JFrame("Where is Loco B?")
f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.X_AXIS))
f.contentPane.add(jb)
f.contentPane.add(b)
f.pack()
f.setSize(200, 100)
f.setLocation(250,0)
f.show()

# create a frame to hold the A button, put button in it, and display
ja = javax.swing.JComboBox(items)
b = javax.swing.JButton("Set Location")
b.actionPerformed = whenAButtonClicked
f = javax.swing.JFrame("Where is Loco A?")
f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.X_AXIS))
f.contentPane.add(ja)
f.contentPane.add(b)
f.pack()
f.setSize(200, 100)
f.show()

