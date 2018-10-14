#
#  Outline cab signalling methods for JMRI
#  Works with a Tethered DT402D (v14 firmware), but not in duplex wireless mode.
#  Info sought on whether a change to op-code is required to get radio transmission.
#
#  Copyright (c) 2011 by Nigel Cliffe,  23rd August 2011

import jmri

# set the intended LocoNet connection by its index; when you have just 1 connection index = 0
connectionIndex = 0

def locoNetCabSigMesg(loco, mast, vertical, diagonal, horizontal, blink):
        # Message variables are Loco = Decimal loco address,  others are binary 1 or 0
        #  mast = 1 means show mast,  mast = 0 means hide mast.
        #  vertical, diagonal, horizontal are the arms (1=show, 0=hide)
        #  blink (1=blink, 0=no blink)
        # Calculate the two byte loco address value
        locoD1 = loco / 128  # integer division, automatically rounds
        locoD2 = loco % 128  # modulo division, gets the remainder.
        signalD3 = mast*16 + vertical*8 + diagonal*4 + horizontal*2 + blink*1
        # values are in Decimal at this stage, some are clearer in Hex!
        # thanks to BillyBob (on LocoNetHackers) for Op-code message format
        sendLocoNetMsg(16,229,16,127,00,00,00, locoD1, locoD2, signalD3, 00,112,00,00,00,00,00)
        return

    # sendLocoNetMsg copied from elsewhere, extended to 15 ARGs
def sendLocoNetMsg(msgLength,opcode,ARG1,ARG2,ARG3,ARG4,ARG5,ARG6,ARG7,ARG8,ARG9,ARG10,ARG11,ARG12,ARG13,ARG14,ARG15) :
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
        packet.setElement(10, ARG10)
        packet.setElement(11, ARG11)
        packet.setElement(12, ARG12)
        packet.setElement(13, ARG13)
        packet.setElement(14, ARG14)
        packet.setElement(15, ARG15)

     jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(packet)
     print "Packet", packet           # print packet to Script Output window
     return


# User Interface follows

import javax.swing

def sendCabSignal(event):
    locoNetCabSigMesg(int(locoAddr.text), showSignal.selected, vertSignal.selected, diagSignal.selected, horiSignal.selected, blinkSignal.selected)
    return

f = javax.swing.JFrame("DT402 Cab Signalling Tool")       # argument is the frames title
f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))
locoAddr = javax.swing.JTextField(5)    # sized to hold 5 characters, initially empty
temppanel1 = javax.swing.JPanel()
temppanel1.add(javax.swing.JLabel("Loco Address: "))
temppanel1.add(locoAddr)
temppanel2 = javax.swing.JPanel()
temppanel2.add(javax.swing.JLabel("Signal state"))
showSignal = javax.swing.JCheckBox("Show")
showSignal.setSelected(False)
temppanel2.add(showSignal)
vertSignal = javax.swing.JCheckBox("Vertical")
vertSignal.setSelected(False)
temppanel2.add(vertSignal)
diagSignal = javax.swing.JCheckBox("Diagonal")
diagSignal.setSelected(False)
temppanel2.add(diagSignal)
horiSignal = javax.swing.JCheckBox("Horizontal")
horiSignal.setSelected(False)
temppanel2.add(horiSignal)
blinkSignal = javax.swing.JCheckBox("Blink")
blinkSignal.setSelected(False)
temppanel2.add(blinkSignal)
temppanel3 = javax.swing.JPanel()
sendButton = javax.swing.JButton("Send Cab Signal")
sendButton.actionPerformed = sendCabSignal
temppanel3.add(sendButton)

f.contentPane.add(temppanel1)
f.contentPane.add(temppanel2)
f.contentPane.add(temppanel3)
f.pack()
f.show()
