#
# Sample to find and enable/disable CMRI card polling
#
# Author: Ken Cameron, copyright 2009
# Part of the JMRI distribution

import jmri

# get the nodes in the system
node0 = jmri.jmrix.cmri.serial.SerialAddress.getNodeFromSystemName("CS1")
node1 = jmri.jmrix.cmri.serial.SerialAddress.getNodeFromSystemName("CS1001")
node2 = jmri.jmrix.cmri.serial.SerialAddress.getNodeFromSystemName("CS2001")
node3 = jmri.jmrix.cmri.serial.SerialAddress.getNodeFromSystemName("CS3001")

# display current status
print "was node0: ", node0.getSensorsActive()
print "was node1: ", node1.getSensorsActive()
print "was node2: ", node2.getSensorsActive()
print "was node3: ", node3.getSensorsActive()

# enable/disable polling per card
node0.setSensorsActive(False)
node1.setSensorsActive(True)
node2.setSensorsActive(False)
node3.setSensorsActive(False)

# display updated status
print "now node0: ", node0.getSensorsActive()
print "now node1: ", node1.getSensorsActive()
print "now node2: ", node2.getSensorsActive()
print "now node3: ", node3.getSensorsActive()
