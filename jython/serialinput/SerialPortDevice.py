# Receive lines of input from an Arduino (or other device)
# connected via a serial port
#
# Each input line is a sequence of a fixed number of unsigned (non-negative)
# integer values separated by comma, and ending in a NL
#
# Author: Bob Jacobsen, copyright 2016
# Part of the JMRI distribution
#
# This is mostly intended to be subclassed to do something interesting
# with a overriden "process()" method

#
# We use an Automat object to create a separate thread
# that can sit there, waiting for each character to 
# arrive.  Sending characters, on the other hand, 
# happens immediately.
#

import jarray
import jmri
import purejavacomm

class SerialPortDevice(jmri.jmrit.automat.AbstractAutomaton) :
    
    # ctor does nothing
    def __init__(self) :
        self.flush = False
        self.opened = False
        return
    
    # open(name) prepares the serial connection
    # this needs to be done before start()
    def open(self, portname) : 
        # find the port info and open the port
        print "Opening ",portname
        self.portID = purejavacomm.CommPortIdentifier.getPortIdentifier(portname)
        self.port = self.portID.open("JMRI", 50)
        
        # set options on port
        baudrate = 19200
        self.port.setSerialPortParams(baudrate, purejavacomm.SerialPort.DATABITS_8, 
                                    purejavacomm.SerialPort.STOPBITS_1, purejavacomm.SerialPort.PARITY_NONE)
        
        # get I/O connections for later
        self.inputStream = self.port.getInputStream()
        self.outputStream = self.port.getOutputStream()
        
        print "Port opened OK"
        
        self.line = ""
        self.flush = True
        self.opened = True
        return
        
    # init() is the place for your initialization
    def init(self) : 
        if (not self.opened) :
            print "Error in SerialPortDevice: must call open() before start()"
        return
        
    # handle() is called repeatedly until it returns false.
    #
    # Gets the input and drives process(values) when ready
    def handle(self) : 
        # if initial flush pending, do that just once
        if (self.flush) :
            next = 0
            while (next != 13) : next = self.inputStream.read()
            self.flush = False
            print "Ready to process"
            
        # get next byte
        next = self.inputStream.read()
        
        # this sample doesn't do anything with that character except echo it
        if (next == 10) : return 1 # ignore LF
 
        if (next != 13) :  # unless CR, return - if CR, process line
            self.line += chr(next)
            return 1    # to continue
        
        # split line into array of string values
        values = self.parse(self.line)

        # send that array to be processed        
        self.process(values);
        
        # and continue around again
        self.line = ""
        return 1

    def parse(self, line) : 
        # parse one line of input
        return line.split(",")

    def process(self, values) : 
        # User handles each array of string values
        # The default just prints it
        print values
        return
    
    def write(self, data) : 
        # Send some characters
        self.outputStream.write(data)
        return
        
    def flush(self) : 
        self.outputStream.flush()
        return 
        
# end of class definition

# Example of use

#execfile("jython/serialinput/SerialPortDevice.py")
#a = SerialPortDevice()
#a.open("/dev/cu.usbmodem1411")
#a.start()







