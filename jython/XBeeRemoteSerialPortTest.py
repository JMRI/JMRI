# Try to send and receive some bytes via a remote serial
# port connected to an XBee module.
#
# If the remote XBee module has the transmit and receive pins connected
# together, this script can be used to perform a loopback test. 
#
# Derived from SerialPortTest.py
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

class XBeeRemoteSerialPortTest(jmri.jmrit.automat.AbstractAutomaton) :
    
    # ctor starts up the serial port
    def __init__(self) :
        
        # find the XBee Module
        self.cm = jmri.InstanceManager.getDefault(jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo)
        self.tc = self.cm.getTrafficController()
        self.Xbee = self.tc.getNodeFromAddress(3) # change the address to that of a suitable node.
        self.xbeestream = self.Xbee.getIOStream() 

        # get I/O connections for later
        self.inputStream = self.xbeestream.getInputStream()
        self.outputStream = self.xbeestream.getOutputStream()
        
        print "Port opened OK"
        return
    
    # init() is the place for your initialization
    def init(self) : 
        return
        
    # handle() is called repeatedly until it returns false.
    #
    # Modify this to do your calculation.
    def handle(self) : 
        
        # get next character
        next = a.inputStream.read()
        
        # this sample doesn't do anything with that character except echo it
        print "rcv", next
        
        # and continue around again
        return 1    # to continue
    
    def write(self, data) : 
        # now send
        self.outputStream.write(data)
        return
        
    def flush(self) : 
        self.outputStream.flush()
        return 
        
# end of class definition

# create one of these; provide the name of the serial port
a = XBeeRemoteSerialPortTest()

# set the thread name, so easy to cancel if needed
a.setName("XBeeRemoteSerialPortTest sample script")

# start running
a.start();

# setup now complete, try to send some bytes
a.write('H')
a.write('e')
a.write('l')
a.write('l')
a.write('o')
a.write('!')
a.write(0x0D)
a.flush()

print "End of Script"
