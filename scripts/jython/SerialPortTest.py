# Try to send and receive some bytes to a parallel port
#
# Author: Bob Jacobsen, copyright 2009
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $


#
# We use an Automat object to create a separate thread
# that can sit there, waiting for each character to 
# arrive.  Sending characters, on the other hand, 
# happens immediately.
#

import jarray
import jmri
import javax.comm

class SerialPortTest(jmri.jmrit.automat.AbstractAutomaton) :
    
    # ctor starts up the serial port
    def __init__(self, portname) :
        
        # find the port info and open the port
        print "opening ",portname
        self.portID = javax.comm.CommPortIdentifier.getPortIdentifier(portname)
        self.port = self.portID.open("JMRI", 50)
        
        # set options on port
        baudrate = 9600
        self.port.setSerialPortParams(baudrate, javax.comm.SerialPort.DATABITS_8, 
                                    javax.comm.SerialPort.STOPBITS_1, javax.comm.SerialPort.PARITY_NONE)
        
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

# create one of these
a = SerialPortTest("/dev/cu.USA19H1d1P1.1")

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
