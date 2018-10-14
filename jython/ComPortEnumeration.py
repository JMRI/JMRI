# This is a simple script to enumerate the communication ports
# available on the computer.
#
# Author: Matthew Harris, copyright (C) 2010.
# Part of the JMRI distribution

import jmri

import purejavacomm
#import ch.ntb.usb

def portType(type):
    if type == purejavacomm.CommPortIdentifier.PORT_PARALLEL:
        return "Parallel"
    elif type == purejavacomm.CommPortIdentifier.PORT_SERIAL:
        return "Serial"
    elif type == purejavacomm.CommPortIdentifier.PORT_I2C:
        return "I2C"
    elif type == purejavacomm.CommPortIdentifier.PORT_RS485:
        return "RS485"
    elif type == purejavacomm.CommPortIdentifier.PORT_RAW:
        return "Raw"
    else:
        return "Unknown type"

print "---------------------------------"
print "Enumerating available com ports"
print "---------------------------------"

portnames = purejavacomm.CommPortIdentifier.getPortIdentifiers()

for portname in portnames:
    print "Port: ", portname.name, " type: ", portType(portname.getPortType())

print "---------------------------------"
print "Enumerating available USB devices"
print "(via JInput)"
print "---------------------------------"

usbmodel = jmri.jmrix.jinput.TreeModel.instance()
usbdevices = usbmodel.controllers()

for usbdevice in usbdevices:
    print "Device: ", usbdevice.toString()

#print "---------------------------------"
#print "Enumerating available USB devices"
#print "(via Libusb)"
#print "---------------------------------"
#
#ch.ntb.usb.LibusbJava.usb_init()
#ch.ntb.usb.LibusbJava.usb_find_busses()
#ch.ntb.usb.LibusbJava.usb_find_devices()
#
#usbbus = ch.ntb.usb.LibusbJava.usb_get_busses()
#
#ch.ntb.usb.Utils.logBus(usbbus)

print "---------------------------------"
print "Done"
