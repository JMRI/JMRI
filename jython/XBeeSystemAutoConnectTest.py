# This script demonstrates the ability to connect a stream port
# controller to an XBee node.  The port controller then behaves
# like any other connection, but it's data is tunneled via the 
# XBee network.
#
# This version uses tools provided by the node to automatically
# configure the port from the class name of the connection type.
#
# Derived from XBeeSystemConnectTest.py
#
# Author: Bob Jacobsen, copyright 2009
# Author: Paul Bender, copyright 2014
# Part of the JMRI distribution
#

# We use an Automat object to create a separate thread
# that can sit there, waiting for each character to 
# arrive.  Sending characters, on the other hand, 
# happens immediately.
#

import jarray
import jmri

class XBeeSystemConnectionTest(jmri.jmrit.automat.AbstractAutomaton) :
    
    # ctor starts up the serial port
    def __init__(self) :
        
        # find the XBee Module
        self.cm = jmri.InstanceManager.getDefault(jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo)
        self.tc = self.cm.getTrafficController()
        self.Xbee = self.tc.getNodeFromAddress(3) # change the address to that of a suitable node.
        # set up an XPressNet connection as a test.
        self.Xbee.connectPortController(jmri.jmrix.lenz.XNetStreamPortController)
        print "Port opened OK"
        return
    
    # init() is the place for your initialization
    def init(self) : 
        return
        
    # handle() is called repeatedly until it returns false.
    #
    # Modify this to do your calculation.
    def handle(self) : 
    return 0    # only needs to be called once.
        
# end of class definition

# create one of these; provide the name of the serial port
a = XBeeSystemConnectionTest()

# set the thread name, so easy to cancel if needed
a.setName("XBeeSystemConnectionTest sample script")

# start running
a.start();

print "End of Script"
