
print "In USBDriver - Logitech Dual Action"

class USBDriver :
    def __init__(self):
        self.componentNextThrottleFrame = "pov"  # Component for throttle frames browsing
        self.valueNextThrottleFrame = 0.5
        self.componentPreviousThrottleFrame = "pov"
        self.valuePreviousThrottleFrame = 1
        
        self.componentNextRunningThrottleFrame = "pov"  # Component for running throttle frames browsing
        self.valueNextRunningThrottleFrame = 0.75
        self.componentPreviousRunningThrottleFrame = "pov"
        self.valuePreviousRunningThrottleFrame = 0.25
        
        # From there available only when no throttle is active in current window  
        self.componentNextRosterBrowse = ""  # Component for roster browsing
        self.valueNextRoster = 0.75
        self.componentPreviousRosterBrowse = ""
        self.valuePreviousRoster = 0.25
        
        self.componentRosterSelect = "9"  # Component to select a roster
        self.valueRosterSelect = 1
        
        # From there available only when a throttle is active in current window        
        self.componentThrottleRelease = "9"  # Component to release current throttle
        self.valueThrottleRelease = 1

        self.componentSpeed = "x"  # Analog axis component for curent throttle speed
        self.valueSpeedTrigger = 0.07
        
        self.componentSpeedIncrease = "" 
        self.valueSpeedIncrease = 1 
        
        self.componentSpeedDecrease = "" 
        self.valueSpeedDecrease = 1 
                
        self.componentDirectionForward = "z" # Analog axis component for curent throttle direction
        self.valueDirectionForward = 1
        
        self.componentDirectionBackward = "z"
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
        self.valueF0Off = 0

        self.componentF1 = "5" # Function button
        self.valueF1 = 1 
        self.valueF1Off = 0

        self.componentF2 = "6" # Function button
        self.valueF2 = 1
        self.valueF2Off = 0

        self.componentF3 = "7" # Function button
        self.valueF3 = 1
        self.valueF3Off = 0

        self.componentF4 = "8" # Function button
        self.valueF4 = 1
        self.valueF4Off = 0

        self.componentF5 = "" # Function button
        self.valueF5 = 1
        self.valueF5Off = 0

        self.componentF6 = "" # Function button
        self.valueF6 = 1
        self.valueF6Off = 0

        self.componentF7 = "" # Function button
        self.valueF7 = 1
        self.valueF7Off = 0

        self.componentF8 = "" # Function button
        self.valueF8 = 1
        self.valueF8Off = 0

        self.componentF9 = "" # Function button
        self.valueF9 = 1
        self.valueF9Off = 0

        self.componentF10 = "" # Function button
        self.valueF10 = 1
        self.valueF10Off = 0

        self.componentF11 = "" # Function button
        self.valueF11 = 1 
        self.valueF11Off = 0

        self.componentF12 = "" # Function button
        self.valueF12 = 1
        self.valueF12Off = 0

        self.componentF13 = "" # Function button
        self.valueF13 = 1
        self.valueF13Off = 0

        self.componentF14 = "" # Function button
        self.valueF14 = 1
        self.valueF14Off = 0
        
        self.componentF15 = "" # Function button
        self.valueF15 = 1
        self.valueF15Off = 0

        self.componentF16 = "" # Function button
        self.valueF16 = 1
        self.valueF16Off = 0

        self.componentF17 = "" # Function button
        self.valueF17 = 1
        self.valueF17Off = 0
        
        self.componentF18 = "" # Function button
        self.valueF18 = 1
        self.valueF18Off = 0
        
        self.componentF19 = "" # Function button
        self.valueF19 = 1
        self.valueF19Off = 0

        self.componentF20 = "" # Function button
        self.valueF20 = 1
        self.valueF20Off = 0

        self.componentF21 = "" # Function button
        self.valueF21 = 1 
        self.valueF21Off = 0

        self.componentF22 = "" # Function button
        self.valueF22 = 1
        self.valueF22Off = 0

        self.componentF23 = "" # Function button
        self.valueF23 = 1
        self.valueF23Off = 0

        self.componentF24 = "" # Function button
        self.valueF24 = 1
        self.valueF24Off = 0
        
        self.componentF25 = "" # Function button
        self.valueF25 = 1
        self.valueF25Off = 0

        self.componentF26 = "" # Function button
        self.valueF26 = 1
        self.valueF26Off = 0

        self.componentF27 = "" # Function button
        self.valueF27 = 1
        self.valueF27Off = 0
        
        self.componentF28 = "" # Function button
        self.valueF28 = 1
        self.valueF28Off = 0
        
        self.componentF29 = "" # Function button
        self.valueF29 = 1
        self.valueF29Off = 0        
