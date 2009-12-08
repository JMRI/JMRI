
print "In USBDriver - Logitech Dual Action"

class USBDriver :
    def __init__(self):
        self.componentThrottleFrame = "pov"  # Component for throttle frames browsing
        self.valueNextThrottleFrame = 0.5
        self.valuePreviousThrottleFrame = 1

        self.componentSpeed = "x"  # Analog axis component for curent throttle speed
        self.valueSpeedTrigger = 0.07

        self.componentDirection = "z" # Analog axis component for curent throttle direction
        self.valueDirectionForward = 1
        self.valueDirectionBackward = -1

        self.componentStopSpeed = "2" # Preset speed button stop
        self.valueStopSpeed = 1
    
        self.componentSlowSpeed = "1" # Preset speed button slow
        self.valueSlowSpeed = 1 
    
        self.componentCruiseSpeed = "0" # Preset speed button cruise
        self.valueCruiseSpeed = 1
    
        self.componentMaxSpeed = "3" # Preset speed button max
        self.valueMaxSpeed = 1

        self.componentF0 = "4" # Function button
        self.valueF0 = 1

        self.componentF1 = "5" # Function button
        self.valueF1 = 1 

        self.componentF2 = "6" # Function button
        self.valueF2 = 1

        self.componentF3 = "7" # Function button
        self.valueF3 = 1

        self.componentF4 = "8" # Function button
        self.valueF4 = 1
        
        self.componentF5 = "9" # Function button
        self.valueF5 = 1

        self.componentF6 = "10" # Function button
        self.valueF6 = 1

        self.componentF7 = "11" # Function button
        self.valueF7 = 1
        
        self.componentF8 = "12" # Function button
        self.valueF8 = 1
