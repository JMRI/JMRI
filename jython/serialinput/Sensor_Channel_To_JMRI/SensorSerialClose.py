
# Capture Sensor Data from an Arduino Serial Transmission
# Author: Geoff Bunza 2018 based in part on a script by
# Bob Jacobsen as part of the JMRI distribution
# Version 1.2

import jarray
import jmri
import purejavacomm

class SerialCloseMux(jmri.jmrit.automat.AbstractAutomaton) :
    
    # ctor starts up the serial port
    def __init__(self, portname) :
        global extport
        self.portID = purejavacomm.CommPortIdentifier.getPortIdentifier(portname)
        extport.close()
        return
    
    # init() is the place for your initialization
    def init(self) : 
        
        return
    
    # handle() is called repeatedly until it returns false.
    def handle(self) : 
          
          return 0
    
    def write(self, data) : 

        return
    
    def flush(self) : 
        self.outputStream.flush()
        return

# create one of these; provide the name of the serial port
a = SerialCloseMux("COM5")

# set the thread name, so easy to cancel if needed
a.setName("SerialCloseMux script")

# start running
a.start();

