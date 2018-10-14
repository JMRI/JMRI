# Try to send and receive some bytes via a serial port
#
# Author: Bob Jacobsen, copyright 2009
# Part of the JMRI distribution
#


#
# We use an Automat object to create a separate thread
# that can sit there, waiting for each character to 
# arrive.  Sending characters, on the other hand, 
# happens immediately.
#

import jarray
import jmri
import purejavacomm

class SerialPortTest(jmri.jmrit.automat.AbstractAutomaton) :
    
    # ctor starts up the serial port
    def __init__(self, portname) :
        
        # find the port info and open the port
        print "opening ",portname
        self.portID = purejavacomm.CommPortIdentifier.getPortIdentifier(portname)
        self.port = self.portID.open("JMRI", 50)
        
        # set options on port
        baudrate = 9600
        self.port.setSerialPortParams(baudrate, purejavacomm.SerialPort.DATABITS_8, 
                                    purejavacomm.SerialPort.STOPBITS_1, purejavacomm.SerialPort.PARITY_NONE)
        
        # get I/O connections for later
        self.inputStream = self.port.getInputStream()
        self.outputStream = self.port.getOutputStream()
        
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
        next = self.inputStream.read()
        
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
a = SerialPortTest("COM1")

# set the thread name, so easy to cancel if needed
a.setName("SerialPortTest sample script")

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
