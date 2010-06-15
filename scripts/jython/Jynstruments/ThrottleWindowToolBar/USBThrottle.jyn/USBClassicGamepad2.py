
print "Loading USBDriver : USB Classic Gamepad 2"

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

        self.componentDirectionSwitch = "4" # A single component to switch speed
        self.valueDirectionSwitch = 1        
        
        self.componentStopSpeed = "0" # Preset speed button stop
        self.valueStopSpeed = 1
    
        self.componentSlowSpeed = "1" # Preset speed button slow
        self.valueSlowSpeed = 1 
    
        self.componentCruiseSpeed = "2" # Preset speed button cruise
        self.valueCruiseSpeed = 1
    
        self.componentMaxSpeed = "" # Preset speed button max
        self.valueMaxSpeed = 1
        
        self.componentF0 = "3" # Function button
        self.valueF0 = 1
        self.valueF0Off = 0  # off event for non lockable functions

        self.componentF1 = "" # Function button
        self.valueF1 = 1 
        self.valueF1Off = 0  # off event for non lockable functions

        self.componentF2 = "" # Function button
        self.valueF2 = 1
        self.valueF2Off = 0  # off event for non lockable functions

        self.componentF3 = "" # Function button
        self.valueF3 = 1
        self.valueF3Off = 0  # off event for non lockable functions

        self.componentF4 = "" # Function button
        self.valueF4 = 1
        self.valueF4Off = 0  # off event for non lockable functions
        
        self.componentF5 = "" # Function button
        self.valueF5 = 1
        self.valueF5Off = 0  # off event for non lockable functions

        self.componentF6 = "" # Function button
        self.valueF6 = 1
        self.valueF6Off = 0  # off event for non lockable functions

        self.componentF7 = "" # Function button
        self.valueF7 = 1
        self.valueF7Off = 0  # off event for non lockable functions
        
        self.componentF8 = "" # Function button
        self.valueF8 = 1
        self.valueF8Off = 0  # off event for non lockable functions
