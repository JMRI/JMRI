# To receive some data via a serial RFID concentrator
#
# Author: Nigel Cliffe, Copyright 30th June 2009.
# Derived from SerialPortTest by Bob Jacobsen

# Reads serial port input and writes the results to one of eight memory variables.
# The results can be used to alter layout behaviour, such as switching turnouts, signals
# and trackside devices through Logix, or driving the locomotive (or locomotive
# functions) through a (jython script) robot throttle.
#
# The MERG RFID concentrator takes input from eight tag readers (A to H), and sends
# a line of serial data containing the tag reader and the tag value.
# The line of data is in form   >A1235678G123  where the > is the first character, the second
# character is the reader identity, and the remainder of the line the tag identity.
# This script will put the tag value into a memory variable associated with each reader.
#
# Various extensions are possible, such as a lookup table to convert tag identity to a more
# meaningful name, such as locomotive name or DCC address.
#
# An alternative approach (not in this script) would be to record the location data
# (tag reader) against each item of stock,
#

import jarray
import jmri
import purejavacomm

class SerialPortRFID(jmri.jmrit.automat.AbstractAutomaton) :
    # starts up the serial port
    # no changes to Bob J's code for this section
    def __init__(self, portname) :

        # find the port info and open the port
        print "opening ",portname
        self.portID = purejavacomm.CommPortIdentifier.getPortIdentifier(portname)
        self.port = self.portID.open("JMRI", 50)

        # set options on port
        baudrate = 9600
        self.port.setSerialPortParams(baudrate, purejavacomm.SerialPort.DATABITS_8,
                                    purejavacomm.SerialPort.STOPBITS_1, purejavacomm.SerialPort.PARITY_NONE)

        # the MERG Mk2 RFID concentrator uses RTS/CTS (hardware) flow control
        self.port.setFlowControlMode(purejavacomm.SerialPort.FLOWCONTROL_RTSCTS_IN)

        # get I/O connections for later
        self.inputStream = self.port.getInputStream()
        self.outputStream = self.port.getOutputStream()

        print "Port opened OK"
        return

    # init() is the place for your initialization
    def init(self) :
        # define the temporary string used to store incoming characters and RFID station..
        self.readRFID = ""
        self.readStation = ""
        return

    # handle() is called repeatedly until it returns false.
    #
    # Modify this to do your calculation.
    def handle(self) :
        # get next character
        next = a.inputStream.read()
        # handy for debugging, but commented out in running code
        # print "rcv", next

        # if its an end of line, then we have the tag and readstation.
        if (next == 13 ) :
            # Print a status message (comment out if not required)
            print "end of line: Tag String = " , self.readRFID
            # if tag is already in a memory variable, remove from the old memory variable,
            for mem_var in all_mems:
                if (mem_var.value == self.readRFID) : mem_var.value = ""
            # now write the tag to the current reader...
            self.readStation.value = self.readRFID
            # Reset variables for next step round the loop...
            self.readStation = ""
            self.readRFID = ""
            print "listening for next reading \n"
            return 1  # stop the loop here...

        # ignore the first '>' character
        if (next == 62) :
            return 1

        #  The first character will be the readstation label, A, B, C, etc.
        if (self.readStation == "") :
            if (next == 65) : self.readStation = mem_a
            if (next == 66) : self.readStation = mem_b
            if (next == 67) : self.readStation = mem_c
            if (next == 68) : self.readStation = mem_d
            if (next == 69) : self.readStation = mem_e
            if (next == 70) : self.readStation = mem_f
            if (next == 71) : self.readStation = mem_g
            if (next == 72) : self.readStation = mem_h
        # subsequent characters are the tag string, one character at a time
        else :
            self.readRFID = self.readRFID + chr(next)

        # and continue around again
        return 1    # to continue

    def write(self, data) :
        # we probably don't need a write capability, but left it there from Bob J's original
        # now send
        self.outputStream.write(data)
        return

    def flush(self) :
        # also don't need flush, but again left it there from Bob J's original
        self.outputStream.flush()
        return

# end of class definition

# create memory variables
mem_a = memories.provideMemory("RFID_A")
mem_b = memories.provideMemory("RFID_B")
mem_c = memories.provideMemory("RFID_C")
mem_d = memories.provideMemory("RFID_D")
mem_e = memories.provideMemory("RFID_E")
mem_f = memories.provideMemory("RFID_F")
mem_g = memories.provideMemory("RFID_G")
mem_h = memories.provideMemory("RFID_H")

# create an array holding all the memory variables
all_mems = [ mem_a, mem_b, mem_c, mem_d, mem_e, mem_f, mem_g, mem_h ]

# create one of these; provide the name of the serial port
a = SerialPortRFID("COM1")

# set the thread name, so easy to cancel if needed
a.setName("SerialPortRFID sample script")

# start running
a.start();

