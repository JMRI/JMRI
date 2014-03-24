# Try to send and receive some bytes via a remote serial
# port connected to an XBee module.
#
# If the remote XBee module has the transmit and receive pins connected
# together, this script can be used to perform a loopback test. 
#
# Derived from XBeeRemoteSerialPortTest.py
#
# Author: Bob Jacobsen, copyright 2009
# Author: Paul Bender, copyright 2014
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision$


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
        self.xbeestream = jmri.jmrix.ieee802154.xbee.XBeeIOStream(self.Xbee,self.tc)

        # set up an XPressNet connection as a test.
        self.xnetstreamport = jmri.jmrix.lenz.XNetStreamPortController(self.xbeestream.getInputStream(), self.xbeestream.getOutputStream(), "test")
        self.xnetstreamport.configure()
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
