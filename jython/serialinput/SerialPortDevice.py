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
            # skip anything listed as available now
            count = self.inputStream.available()
            self.inputStream.skip(count)
            print "Skipped character count:", count
            count = self.inputStream.available()
            self.inputStream.skip(count)
            print "Skipped character count:", count
            count = self.inputStream.available()
            self.inputStream.skip(count)
            print "Skipped character count:", count
            # now skip 10 lines in hope to flush buffers of partial lines
            count = 0
            while (count < 10) :
                next = 0
                while (next != 13) : next = self.inputStream.read()  # 13 is Newline
                count = count+1
            self.flush = False
            print "Ready to process"
            
        # get next line
        self.line = ""
        next = self.inputStream.read()       
        while (next != 13) :  # unless CR, return - if CR, process line
            if (next != 10) : self.line += chr(next)  # ignore LF 10
            next = self.inputStream.read()
                
        # split line into array of string values
        values = self.parse(self.line)

        # send that array to be processed        
        self.process(values);
        
        # flush buffer and skip line if falling behind
        count = self.inputStream.available()
        if (count > 60) : # falling behind, flush
            self.inputStream.skip(count)
            next = self.inputStream.read()       
            while (next != 13) :  # loop until consume CR
                next = self.inputStream.read()

        # and continue around again
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







