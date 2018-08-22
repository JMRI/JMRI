# Send a DCC packet
#
# Author: Bob Jacobsen, copyright 2009
# Part of the JMRI distribution

import jmri

# Send a packet; the argument includes _all_ the bytes
def send(pkt):
    dcc.sendPacket(pkt, 1)

# Send a packet; an XOR'd checksum is appended
def sendXOR(pkt):
    xor = 0
    for x in pkt:
        xor = xor^x
    newpkt = pkt+[xor]
    send(newpkt)

# define and send the accessory packet to
# program CV 514 to 0
# on accessory decoder 2041

Adr = 2041
Cv = 514
Val = 0

# Define the packet bytes.
# The following uses "basic addressing" and "all outputs on decoder"
b1 = 128+Adr/8
b2 = 128+((Adr&0x7)*16)+8
b3 = 0xEC+(Cv/256)
b4 = Cv&0xFF
b5 = Val

# send it with an error correction byte
sendXOR([b1 b2 b3 b4 b5])


