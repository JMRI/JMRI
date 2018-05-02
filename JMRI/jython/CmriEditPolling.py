#
# Sample to find and enable/disable CMRI card polling
#
# Author: Ken Cameron, copyright 2009
# Part of the JMRI distribution

import jmri

# get the nodes in the system
memo = jmri.InstanceManager.getList(jmri.jmrix.cmri.CMRISystemConnectionMemo).get(0)
node0 = memo.getNodeFromSystemName("CS1", memo.getTrafficController())
node1 = memo.getNodeFromSystemName("CS1001", memo.getTrafficController())
node2 = memo.getNodeFromSystemName("CS2001", memo.getTrafficController())
node3 = memo.getNodeFromSystemName("CS3001", memo.getTrafficController())

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
