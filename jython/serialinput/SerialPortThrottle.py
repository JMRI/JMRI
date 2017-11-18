# Accept data from a SerialInputDevice and use it to run a
# JMRI throttle
#
# Author: Bob Jacobsen, copyright 2016
# Part of the JMRI distribution
#

import jarray
import jmri
import purejavacomm

class SerialPortThrottle(SerialPortDevice) :
    
    def __init__(self) :
        SerialPortDevice.__init__(self)
        # these can be overloaded before start()

        # throttle address and settings
        self.address = 3
        self.long = False
                
        # define the offset of each input value
        self.reverseIndex = 9
        self.forwardIndex = 10
        self.speedIndex = 0
        
        self.hornIndex = 8   # for F1
        self.bellIndex = 11  # for F2
        
        return

    def init(self) :
        # connect the throttle
        self.throttle = self.getThrottle(self.address, self.long)

        # initial setup 
        self.throttle.setIsForward(True)
        
    def process(self, values) : 
        # User handles each array of string values
        print "root: "", values
        
        # get numbers
        reverse = ( values[self.reverseIndex] == '0')
        forward = ( values[self.forwardIndex] == '0')
        speed   = int(values[self.speedIndex]) /1023. 
        horn = ( values[self.hornIndex] == '0')  # for F1
        bell = ( values[self.bellIndex] == '0')  # for F2
        
        #print "   process", reverse, forward, speed, "from", values[self.reverseIndex], values[self.forwardIndex], values[self.speedIndex]
        
        if (reverse and self.throttle.getIsForward() ) :
            self.throttle.setIsForward(False)
        
        if (forward and not self.throttle.getIsForward() ) :
            self.throttle.setIsForward(True)
        
        if ( abs(self.throttle.getSpeedSetting() - speed) > 0.015 ) :
            self.throttle.setSpeedSetting(speed)

        if (forward and not self.throttle.getIsForward() ) :
            self.throttle.setIsForward(True)
            
        if (horn != self.throttle.getF1() ) :
            self.throttle.setF1(horn)
            
        if (bell != self.throttle.getF2() ) :
            self.throttle.setF2(bell)
            
        return


# end of class definition

# Example

#execfile("jython/serialinput/SerialPortDevice.py")
#execfile("jython/serialinput/SerialPortThrottle.py")
#a = SerialPortThrottle()
#a.open("/dev/cu.usbserial-A7006QP9")
#a.long = True
#a.address = 4123
#a.start()







