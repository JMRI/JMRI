# This is a simple script to enumerate the communication ports
# available on the computer.
#
# Author: Matthew Harris, copyright (C) 2010.
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

import gnu.io

def portType(type):
    if type == gnu.io.CommPortIdentifier.PORT_PARALLEL:
        return "Parallel"
    elif type == gnu.io.CommPortIdentifier.PORT_SERIAL:
        return "Serial"
    elif type == gnu.io.CommPortIdentifier.PORT_I2C:
        return "I2C"
    elif type == gnu.io.CommPortIdentifier.PORT_RS485:
        return "RS485"
    elif type == gnu.io.CommPortIdentifier.PORT_RAW:
        return "Raw"
    else:
        return "Unknown type"

portnames = gnu.io.CommPortIdentifier.getPortIdentifiers()

print "Enumerating available com ports"
print "-------------------------------"

for portname in portnames:
    print "Port: ", portname.name, " type: ", portType(portname.getPortType())

print "-------------------------------"
print "Done"