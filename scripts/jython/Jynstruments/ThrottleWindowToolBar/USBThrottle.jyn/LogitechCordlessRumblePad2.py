print "Loading USBDriver : Logitech Cordless RumblePad 2"

class USBDriver :
    def __init__(self):
        self.componentThrottleFrame = "Hat Switch"  # Component for throttle frames browsing
        self.valueNextThrottleFrame = 0.5
        self.valuePreviousThrottleFrame = 1
        
        # From there available only when no throttle is active in current window  
        self.componentRosterBrowse = "Hat Switch"  # Component for roster browsing
        self.valueNextRoster = 0.75
        self.valuePreviousRoster = 0.25
        
        self.componentRosterSelect = "Button 4"  # Component to select a roster
        self.valueRosterSelect = 1
        
        # From there available only when a throttle is active in current window        
        self.componentThrottleRelease = "Button 5"  # Component to release current throttle
        self.valueThrottleRelease = 1
        
        self.componentSpeed = "X Axis"  # Analog axis component for curent throttle speed
        self.valueSpeedTrigger = 0.05 # ignore values lower than
        self.componentSpeedMultiplier = .5 # multiplier for pad value (negative values to reveerse)

        self.componentDirection = "Z Rotation" # Analog axis component for curent throttle direction
        self.valueDirectionForward = -1
        self.valueDirectionBackward = 1    
  
        self.componentStopSpeed = "Button 7" # Preset speed button stop, double tap will Estop
        self.valueStopSpeed = 1
    
        self.componentSlowSpeed = "" # Preset speed button slow
        self.valueSlowSpeed = 1 
    
        self.componentCruiseSpeed = "" # Preset speed button cruise, double tap will max speed
        self.valueCruiseSpeed = 1
    
        self.componentMaxSpeed = "" # Preset speed button max
        self.valueMaxSpeed = 1

        self.componentF0 = "Button 0" # Function button
        self.valueF0 = 1
        self.valueF0Off = 0  # off event for non lockable functions

        self.componentF1 = "Button 1" # Function button
        self.valueF1 = 1 
        self.valueF1Off = 0

        self.componentF2 = "Button 2" # Function button
        self.valueF2 = 1
        self.valueF2Off = 0

        self.componentF3 = "Button 3" # Function button
        self.valueF3 = 1
        self.valueF3Off = 0

        self.componentF4 = "8" # Function button
        self.valueF4 = 1
        self.valueF4Off = 0
        