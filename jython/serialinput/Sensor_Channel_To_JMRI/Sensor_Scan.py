# Capture Sensor Data from an Arduino Serial Transmission
# In the form: Character "A" followed by 
# 1 byte: bit 7 Sensor ON/OFF bit 6-0 sensor # 1-127
# Author: Geoff Bunza 2018 based in part on a script by
# Bob Jacobsen as part of the JMRI distribution
# Version 1.2
# An Automat object to create a separate thread
# that can sit there, waiting for each character to arrive

import jarray
import jmri
import purejavacomm

class SerialSensorMux(jmri.jmrit.automat.AbstractAutomaton) :
    # ctor starts up the serial port
    def __init__(self, portname) :
        global extport
        self.portID = purejavacomm.CommPortIdentifier.getPortIdentifier(portname)
        try:
            self.port = self.portID.open("JMRI", 50)
        except purejavacomm.PortInUseException:
            self.port = extport
        extport = self.port
        # set options on port
        baudrate = 19200
        self.port.setSerialPortParams(baudrate, 
            purejavacomm.SerialPort.DATABITS_8, 
            purejavacomm.SerialPort.STOPBITS_1, 
            purejavacomm.SerialPort.PARITY_NONE)
        # Anticipate the Port Opening will restart the Arduino
        self.waitMsec(2000)
        # get I/O connections for later
        self.inputStream = self.port.getInputStream()
        self.outputStream = self.port.getOutputStream()
        return

    # init() is the place for your initialization
    def init(self) :
        return

    # handle() is called repeatedly until it returns false.
    def handle(self) :
        global ttest
        if ttest == 1 :
                self.outputStream.write('!')
                self.outputStream.write('!')
                self.outputStream.write('!')
                self.outputStream.write(0x0D)
                ttest = 0
        # get next character    
        if self.inputStream.read() != 65 :
                return 1
        sensor_num = self.inputStream.read()
        sensor_state = ( sensor_num>>7 ) & 1
        sensor_num = sensor_num & 0x7f
        mesg = "AR:%d" % (sensor_num)
        if sensor_num == 1 and sensor_state == 1 :
            print "Sensor Read Ends"
            self.inputStream.close()
            self.outputStream.close()
            self.port.close()
            return 0
        s = sensors.getByUserName( mesg )
        if s is None :
                print mesg, " Not Available"
                return 1
        if sensor_state == 1 :
                s.setKnownState(ACTIVE)
        if sensor_state == 0 :
                s.setKnownState(INACTIVE)

        return 1    # to continue 0 to Kill Script

    def write(self, data) : 
        # now send
        self.outputStream.write(data)
        return

    def flush(self) : 
        self.outputStream.flush()
        return
ttest=1
# create one of these; provide the name of the serial port
a = SerialSensorMux("COM5")

# set the thread name, so easy to cancel if needed
a.setName("SerialSensorMux script")

# start running
a.start();

# setup  complete
a.flush()
