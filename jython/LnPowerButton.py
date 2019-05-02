# Sample script to show a JButton which sends a LocoNet
# message when clicked.
#
# In this case, the LocoNet message alternates between "on"
# and "off" for a PM4 relay.  The particular relay is addressed
# by the board and zone variables set in the code below.
#
# Author: Bob Jacobsen, copyright 2005
# Part of the JMRI distribution
#
# Note that the message sent to the PM4 has fixed contents for the
# other three (of four) channels on the card.  Will this cause a 
# problem in normal operation?

import jmri

import java
import javax.swing

# LocoNet PM4 message is (hex)
# 0xD0 <ARG1>,<ARG2>,<ARG3>,<ARG4>, <CKSUM>
# <ARG1> = 0x60 + (5 upper bits of section address)
# <ARG2> = zone*2 + 16*(3 lower bits of section address)
# <ARG3> = 0x30 (or 0x10?) 

# set the intended LocoNet connection by its index; when you have just 1 connection index = 0
connectionIndex = 0

# create the button, and add an action routine to it
b = javax.swing.JButton("Set On")
def whenMyButtonClicked(event) :
        # find out whether to set the output on or off; change button label
        if (b.getText() == "Set On") :
            b.setText("Set Off")
            on = 1
        else :
            b.setText("Set On")
            on = 0
        # change the next two lines to select the right output
        zone = 2            # 0 to 3, corresponding to A - D
        board = 12          # PM4 board number
        
        # format and send the specific LocoNet message
        packet = jmri.jmrix.loconet.LocoNetMessage(6)
        packet.setElement(0, 0xD0)
        packet.setElement(1, 0x72+(((board-1)/128)&0x1))
        packet.setElement(2, ((board-1)&0x7F))
        packet.setElement(3, 0x3F)  # F in lower bits means all AR
        packet.setElement(4, 0x10+(on << zone)) # set the control bit for just the channel
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(packet)
        return
        
b.actionPerformed = whenMyButtonClicked

# create a frame to hold the button, put button in it, and display
f = javax.swing.JFrame("Section Power")
f.contentPane.add(b)
f.pack()
f.show()

