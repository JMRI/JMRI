# This script will send several of the most command loconet type messages
# These include switch, feedback and sensor type messages
# DCC signal packets can also be sent via loconet
#
# Messages are configurated using radio buttons and combo boxes
#
# Portions of this script are taken from LnPowerButton.py by Bob Jacobsen
# Author: Bill Robinson with help from Bob Jacobsen
#
# 5/02/06 - Bill Robinson
# This adds several buttons to the example
# Switch, feedback and sensor type LocoNet messages can be sent
# The original PM4 button and action are not used
#
# version 1.3 - 2/6/13 Changed DCC Signal packet to match JMRI definition

import jmri

import java
import java.awt
import java.awt.event
import javax.swing

typePacket = 0
# set the intended LocoNet connection by its index; when you have just 1 connection index = 0
connectionIndex = 0

def whenSendButtonClicked(event) :
     # Based on user selection, prepare the arg for the specific LocoNet message
     # Loconet message form - opcode,ARG1,ARG2,CHK - the checksum is not calculated here
     lnAddress = int(lAddress.text) - 1    # get address from entry field and adjust for LocoNet
     ARG1 = lnAddress - ((lnAddress / 128) * 128)
     ARG2 = lnAddress / 128
     ARG3 = ARG4 = ARG5 = ARG6 = ARG7 = ARG8 = ARG9 = 0
     msgLength = 4      # number of bytes in the LocoNet message - includes checksum

     if  msgTypeBox.getSelectedItem() == "Switch" :
         opcode = 176        # 0xB0
         if msgActBox.getSelectedIndex() == 0 :  # if close
             ARG2 = ARG2 + 32
         if msgOutBox.getSelectedIndex() == 0 :  # if output on
             ARG2 = ARG2 + 16

     elif msgTypeBox.getSelectedItem() == "Feedback" :
         opcode = 177        # 0xB1
         ARG2 = ARG2 + 64     # set bit 6 in B1 or B2 type messages
         if msgActBox.getSelectedIndex() == 0 :
             ARG2 = ARG2 + 16                # set bit 4
         if msgOutBox.getSelectedIndex() == 0 :  # if aux
             ARG2 = ARG2 + 32                # set bit 5

     else :
         opcode = 178        # 0xB2
         # address of B2 is mapped to 4K space ie one more bit
         remainder = lnAddress % 2
         lnAddress = lnAddress / 2
         ARG1 = lnAddress - ((lnAddress / 128) * 128)
         ARG2 = lnAddress / 128
         ARG2 = ARG2 + 64                    # set bit 6 in B1 or B2 type messages
         if remainder == 1 :                 # determine lsb based on remainder of divide
             ARG2 = ARG2 + 32                # set bit 5
         if msgActBox.getSelectedIndex() == 0 : # if Hi
             ARG2 = ARG2 + 16                # set bit 4

     sendLoconetMsg(msgLength,opcode,ARG1,ARG2,ARG3,ARG4,ARG5,ARG6,ARG7,ARG8,ARG9)
     return

def whenSetButtonClicked(event) :
     # Based on user selection, prepare the LocoNet interrogation message
     msgTypeBox.setSelectedIndex(0)
     msgOutBox.setSelectedIndex(1)

     if   interBox.getSelectedItem() == "1017c" :
         lAddress.setText("1017")     # put address in field
         msgActBox.setSelectedIndex(0)
     elif  interBox.getSelectedItem() == "1017t" :
         lAddress.setText("1017")     # put address in field
         msgActBox.setSelectedIndex(1)
     elif  interBox.getSelectedItem() == "1018c" :
         lAddress.setText("1018")     # put address in field
         msgActBox.setSelectedIndex(0)
     elif  interBox.getSelectedItem() == "1018t" :
         lAddress.setText("1018")     # put address in field
         msgActBox.setSelectedIndex(1)
     elif  interBox.getSelectedItem() == "1019c" :
         lAddress.setText("1019")     # put address in field
         msgActBox.setSelectedIndex(0)
     elif  interBox.getSelectedItem() == "1019t" :
         lAddress.setText("1019")     # put address in field
         msgActBox.setSelectedIndex(1)
     elif  interBox.getSelectedItem() == "1020c" :
         lAddress.setText("1020")     # put address in field
         msgActBox.setSelectedIndex(0)
     else :
         interBox.getSelectedItem() == "1020t"
         lAddress.setText("1020")     # put address in field
         msgActBox.setSelectedIndex(1)
     return

def whenSendDccButtonClicked(event) :
     # Based on user selection, prepare the arg for the specific LocoNet message
     # OPC_IMM_PACKET      0xED    ;SEND n-byte packet immediate    LACK
     # <0xED>,<0B>,<7F>,<REPS>,<DHI>,<IM1>,<IM2>,<IM3>,<IM4>,<IM5>,<CHK>
     # <DHI>=<0,0,1,IM5.7-IM4.7,IM3.7,IM2.7,IM1.7>
     # <REPS> D4,5,6=#IM bytes,D3=0(reserved); D2,1,0=repeat CNT
     global typePacket
     repeatCount = 1    # causes two DCC packets to be sent from command station
     numOfBytes = 2     # number of bytes in the DCC packet - not including checksum
     msgLength = 11     # number of bytes in the LocoNet message - includes checksum
     opcode = 237       # 0XED
     opcode2 = 11       # 0X0B
     opcode3 = 127      # 0X7F
     dhi = 32
     im1 = im3 = im4 = im5 = 0

     if  typePacket == 0 :
         im1 = 0
         im2 = 0
     elif  typePacket == 1 :   # Decoder Idle
         im1 = 127
         im2 = 0
         dhi = 1 + 32
         numOfBytes = 1
     elif  typePacket == 2 :   # Broadcast Stop
         im1 = 0
         im2 = 96
     else :     # Signal packet - 10AAAAAA 0 0AAA0aa1 0 000XXXXX 0 EEEEEEEE
                # a - two low bit of the address
                # A - address, X - aspect number
         addr = int(sgAddress.text) - 1 # change text to a number and subtrac one
         # print "raw address", addr
         toLsb = addr & 3       # extract bits 1,0 for second byte 0xxx0AA1
         addr = addr / 4        # shift down by two
         # print "mod address", addr
         dhi = 1                # bit 7 of im1 is a one (10AAAAAA)
         im1 = addr & 0x3F      # extract six bits 7-2 for first byte 10AAAAAA
         im2 = addr / 64         # shift down by 6 bits
         # print "im1, im2", im1, im2
         im2 =  7 - im2     # invert the three bits by subtracting from seven
         # print "im2 inverted", im2
         im2 = im2 * 16     # shift up by 5 bits
         im2 = im2 + (toLsb * 2) + 1
         # print "im2 final", im2
         im3 = int(aspect.text)
         numOfBytes = 3

     reps = repeatCount + (numOfBytes * 16)
     sendLoconetMsg(msgLength,opcode,opcode2,opcode3,reps,dhi,im1,im2,im3,im4,im5)
     return

def sendLoconetMsg(msgLength,opcode,ARG1,ARG2,ARG3,ARG4,ARG5,ARG6,ARG7,ARG8,ARG9) :
     # format and send the specific LocoNet message
     # send up to 11 bytes in the message - includes checksum
     packet = jmri.jmrix.loconet.LocoNetMessage(msgLength)
     if msgLength == 4 :
        packet.setElement(0, opcode)
        packet.setElement(1, ARG1)
        packet.setElement(2, ARG2)
     else :
        packet.setElement(0, opcode)
        packet.setElement(1, ARG1)
        packet.setElement(2, ARG2)
        packet.setElement(3, ARG3)
        packet.setElement(4, ARG4)
        packet.setElement(5, ARG5)
        packet.setElement(6, ARG6)
        packet.setElement(7, ARG7)
        packet.setElement(8, ARG8)
        packet.setElement(9, ARG9)

     jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(packet)
     print "Packet", packet           # print packet to Script Output window
     prevMsg.setText(str(packet))     # put packet in hex in field
     return

#  Actions for radio buttons
def whenSwButtonClicked(event) :
     global typePacket
     typePacket = 0
     signalButtonFalse()
     return

def whenFbButtonClicked(event) :
     global typePacket
     typePacket = 1
     signalButtonFalse()
     return

def whenSnButtonClicked(event) :
     global typePacket
     typePacket = 2
     signalButtonFalse()
     return

def whenSgButtonClicked(event) :
     global typePacket
     typePacket = 3
     aspect.setEnabled(True)
     sgAddress.setEnabled(True)
     sgAddress.setToolTipText("Range 1-2040")
     aspect.setToolTipText("Range 0-31")
     return

def signalButtonFalse() :
     aspect.setEnabled(False)
     sgAddress.setEnabled(False)
     sgAddress.setToolTipText("Disabled because not in this mode")
     aspect.setToolTipText("Disabled because not in this mode")
     return

# Class to handle a listener event from JComboBox
class MsgTypeListener(java.awt.event.ItemListener):
     def itemStateChanged(self, event):
         if (event.getItem() == "Switch") :
             # print msgTypeBox.getSelectedIndex(), "Selected index"

             if (event.getStateChange() == java.awt.event.ItemEvent.SELECTED) :
                 # Switch selected
                 print event.getItem(), "Selected"
                 msgActBox.setEnabled(True)
                 msgActBox.setToolTipText(None)
                 msgOutBox.removeItemAt(0)
                 msgOutBox.insertItemAt("On",0)
                 msgOutBox.removeItemAt(1)
                 msgOutBox.insertItemAt("Off",1)
                 outputLabel.setText("                 Output")
             else :
                 # Switch deselected
                 print event.getItem(), "Deselected"

         elif (event.getItem() == "Feedback") :
             if (event.getStateChange() == java.awt.event.ItemEvent.SELECTED) :
                 print event.getItem(), "Selected"
                 msgOutBox.removeItemAt(0)
                 msgOutBox.insertItemAt("Sw",0)
                 msgOutBox.removeItemAt(1)
                 msgOutBox.insertItemAt("Aux",1)
                 outputLabel.setText("             Input Type")
             else :
                 print event.getItem(), "Deselected"

         elif (event.getItem() == "Sensor") :
             if (event.getStateChange() == java.awt.event.ItemEvent.SELECTED) :
                 print event.getItem(), "Selected"
                 msgActBox.removeItemAt(0)
                 msgActBox.insertItemAt("Hi",0)
                 msgActBox.removeItemAt(1)
                 msgActBox.insertItemAt("Lo",1)
                 msgOutBox.setEnabled(False)
                 msgOutBox.setToolTipText("Disabled because not in this mode")
             else :
                 print event.getItem(), "Deselected"
                 msgActBox.removeItemAt(0)
                 msgActBox.insertItemAt("Close",0)
                 msgActBox.removeItemAt(1)
                 msgActBox.insertItemAt("Throw",1)
                 msgOutBox.setEnabled(True)
                 msgOutBox.setToolTipText(None)

         else :
             print "Error, unexpected item:", event.getItem()
         return

# Start to initialize the GUI
# Create buttons and define action
sendButton = javax.swing.JButton("Send")
sendButton.actionPerformed = whenSendButtonClicked

setButton = javax.swing.JButton("Set")
setButton.actionPerformed = whenSetButtonClicked
setButton.setToolTipText("Set up the interrogation message")

sendDccButton = javax.swing.JButton("Send")
sendDccButton.actionPerformed = whenSendDccButtonClicked
sendDccButton.setAlignmentX(java.awt.Component.RIGHT_ALIGNMENT)

msgTypeBox = javax.swing.JComboBox()
msgTypeBox.addItem("Switch")
msgTypeBox.addItem("Feedback")
msgTypeBox.addItem("Sensor")
msgTypeBox.itemListener = MsgTypeListener()

msgActBox = javax.swing.JComboBox()
msgActBox.addItem("Close")
msgActBox.addItem("Throw")

msgOutBox = javax.swing.JComboBox()
msgOutBox.addItem("On")
msgOutBox.addItem("Off")

interBox = javax.swing.JComboBox()
interBox.addItem("1017c")
interBox.addItem("1017t")
interBox.addItem("1018c")
interBox.addItem("1018t")
interBox.addItem("1019c")
interBox.addItem("1019t")
interBox.addItem("1020c")
interBox.addItem("1020t")
interBox.setToolTipText("Select the interrogation command")

swButton = javax.swing.JRadioButton("Decoder Reset")
fbButton = javax.swing.JRadioButton("Decoder Idle")
snButton = javax.swing.JRadioButton("Broadcast Stop")
sgButton = javax.swing.JRadioButton("Signal")
swButton.actionPerformed = whenSwButtonClicked
fbButton.actionPerformed = whenFbButtonClicked
snButton.actionPerformed = whenSnButtonClicked
sgButton.actionPerformed = whenSgButtonClicked
sgButton.setToolTipText("Extended Accessory Decoder Packet")
swButton.setSelected(True)   # initially set the group selection

# create fields
lAddress = javax.swing.JTextField(4)    # sized to hold 4 characters
lAddress.setText("1")                   # initialize field

sgAddress = javax.swing.JTextField(4)    # sized to hold 4 characters
sgAddress.setText("1")                   # initialize field
sgAddress.setEnabled(False)              # initially disabled until signal selected
sgAddress.setToolTipText("Disabled because not in this mode")

aspect = javax.swing.JTextField(4)    # sized to hold 4 characters
aspect.setText("0")                   # initialize field
aspect.setEnabled(False)              # initially disabled until signal selected
aspect.setToolTipText("Disabled because not in this mode")

prevMsg = javax.swing.JTextField(22)   # sized to hold 22 characters, initially empty
outputLabel = javax.swing.JLabel("             Output")  # insert spaces to help with display alignment

# create a frame to  display the buttons and panels
f = javax.swing.JFrame("LocoNet Send Tool")
f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))

group1 = javax.swing.ButtonGroup()  # create a radio button group
group1.add(swButton)
group1.add(fbButton)
group1.add(snButton)
group1.add(sgButton)

radioPanel1 = javax.swing.JPanel()  # put the radio buttons in a panel
radioPanel1.setLayout(javax.swing.BoxLayout(radioPanel1, javax.swing.BoxLayout.Y_AXIS))
radioPanel1.add(swButton)
radioPanel1.add(fbButton)
radioPanel1.add(snButton)
radioPanel1.add(sgButton)

panela = javax.swing.JPanel()   # panel to hold interrogate and set button
panela.add(interBox)
panela.add(setButton)
#    put a border around the interrogate selection box and set button
panela.setBorder(javax.swing.BorderFactory.createMatteBorder(1,1,1,1, java.awt.Color.gray))
panelb = javax.swing.JPanel()   # panel to hold send button
panelb.add(sendButton)
panelc = javax.swing.JPanel()   # panel to create a space
panel = javax.swing.JPanel()    # panel to hold the following panels
panel.add(panela)       # add the interrogate and set button panel
panel.add(panelc)       # add a panel to put a space between the other panels
panel.add(panelb)       # add the send button panel

panel0 = javax.swing.JPanel()   # use panel to display items horizontally
panel0.add(javax.swing.JLabel("Address"))   # put label discription before combobox
panel0.add(lAddress)

panel1 = javax.swing.JPanel()
panel1.add(javax.swing.JLabel("Type of message"))
panel1.add(msgTypeBox)

panel2 = javax.swing.JPanel()
panel2.add(javax.swing.JLabel("    Type of action"))
panel2.add(msgActBox)

panel3 = javax.swing.JPanel()
panel3.add(outputLabel)
panel3.add(msgOutBox)

panel4 = javax.swing.JPanel()
panel4.add(javax.swing.JLabel("           Signal Address"))
panel4.add(sgAddress)

panel5 = javax.swing.JPanel()
panel5.add(javax.swing.JLabel("Signal Aspect number"))
panel5.add(aspect)

panel6 = javax.swing.JPanel()
panel6.add(javax.swing.JLabel("Last Message sent"))
panel6.add(prevMsg)

panel7 = javax.swing.JPanel()
panel7.add(javax.swing.JLabel("------ DCC Packet ------"))

panel8 = javax.swing.JPanel()
panel8.add(sendDccButton)

f.contentPane.add(panel0)   # put the buttons and panels in the frame
f.contentPane.add(panel1)
f.contentPane.add(panel2)
f.contentPane.add(panel3)
f.contentPane.add(panel)
f.contentPane.add(javax.swing.JSeparator(javax.swing. JSeparator.HORIZONTAL))
f.contentPane.add(panel7)
f.contentPane.add(radioPanel1)
f.contentPane.add(panel4)
f.contentPane.add(panel5)
f.contentPane.add(panel8)
f.contentPane.add(panel6)
f.pack()
f.show()
