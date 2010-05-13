
print "In USBDriver - UDB Classic Gamepad 2"

class USBDriver :
    def __init__(self):
        self.componentThrottleFrame = "x"  # Component for throttle frames browsing
        self.valueNextThrottleFrame = 1
        self.valuePreviousThrottleFrame = -1
        
        # From there available only when no throttle is active in current window  
        self.componentRosterBrowse = "y"  # Component for roster browsing
        self.valueNextRoster = 1
        self.valuePreviousRoster = -1
        
        self.componentRosterSelect = "5"  # Component to select a roster
        self.valueRosterSelect = 1
        
        # From there available only when a throttle is active in current window        
        self.componentThrottleRelease = "5"  # Component to release current throttle
        self.valueThrottleRelease = 1

        self.componentSpeed = "y"  # Analog axis component for curent throttle speed
        self.valueSpeedTrigger = 0.1
        self.componentSpeedMultiplier = -0.5

        self.componentDirection = "" # Analog axis component for curent throttle direction
        self.valueDirectionForward = 1
        self.valueDirectionBackward = -1

        self.componentDirectionForward = "0" # Analog axis component for curent throttle direction
        self.valueDirectionForwardCmp = 1
        
        self.componentDirectionBackward = "1" # Analog axis component for curent throttle direction
        self.valueDirectionBackwardCmp = 1
        
        self.componentStopSpeed = "2x2" # Preset speed button stop
        self.valueStopSpeed = 1
    
        self.componentSlowSpeed = "2" # Preset speed button slow
        self.valueSlowSpeed = 1 
    
        self.componentCruiseSpeed = "3" # Preset speed button cruise
        self.valueCruiseSpeed = 1
    
        self.componentMaxSpeed = "3x2" # Preset speed button max
        self.valueMaxSpeed = 1

        self.componentEStopSpeed = "0+1" # Preset speed button E stop
        self.valueMaxSpeed = 1
        
        self.componentF0 = "4" # Function button
        self.valueF0 = 1

        self.componentF1 = "" # Function button
        self.valueF1 = 1 

        self.componentF2 = "" # Function button
        self.valueF2 = 1

        self.componentF3 = "" # Function button
        self.valueF3 = 1

        self.componentF4 = "" # Function button
        self.valueF4 = 1
        
        self.componentF5 = "" # Function button
        self.valueF5 = 1

        self.componentF6 = "" # Function button
        self.valueF6 = 1

        self.componentF7 = "" # Function button
        self.valueF7 = 1
        
        self.componentF8 = "" # Function button
        self.valueF8 = 1
