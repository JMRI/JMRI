# Open a small window. When a DCC address (E.g. 4123) is typed and return hit, 
# a Transponding Find request for that address is sent
#
# Author: Bob Jacobsen, copyright 2006, 2014
# Part of the JMRI distribution

import jmri

import java
import javax.swing

# set the intended LocoNet connection by its index; when you have just 1 connection index = 0
connectionIndex = 0

# create a frame to hold the button, set up for nice layout
f = javax.swing.JFrame("Find Transponder")       # argument is the frames title
f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))

# Create the address field
# Sized to show 5 characters, initially empty
# To make the field a different size, change the (5) to the desired size
address = javax.swing.JTextField(5)    

# put the text field on a line preceded by a label
temppanel1 = javax.swing.JPanel()
temppanel1.add(javax.swing.JLabel("Address"))
temppanel1.add(address)
        
# have that text field send the message when entered
def whenAddressChanged(event) : 
    addr = (int)(event.source.getText())
    t = jmri.InstanceManager.getDefault(jmri.ThrottleManager)
    long = t.canBeLongAddress(addr)            
    m = jmri.jmrix.loconet.LocoNetMessage(9)
    m.setOpCode(   0xE5)
    m.setElement(1,0x09)
    m.setElement(2,0x40)
    if (long) :
        m.setElement(3,addr/128)
        m.setElement(4,addr&0x7F)    
    else :
        m.setElement(3,0x7D)
        m.setElement(4,addr)    
    m.setElement(5,0x00)
    m.setElement(6,0x00)
    m.setElement(7,0x00)
    jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(m)
    
    # now force sending of null packets to same DCC address
    c = jmri.InstanceManager.getDefault(jmri.CommandStation)
    c.sendPacket(jmri.NmraPacket.oneBytePacket(addr, long, 0x08),4)  # reserved instruction
    return
    
address.actionPerformed = whenAddressChanged   # if user hit return or enter
address.focusLost = whenAddressChanged         # if user tabs away

# Put contents in frame and display
f.contentPane.add(temppanel1)
f.pack()
f.show()

