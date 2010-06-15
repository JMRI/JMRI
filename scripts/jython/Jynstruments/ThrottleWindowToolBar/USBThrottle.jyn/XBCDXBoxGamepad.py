# Use an Xbox (original) controller as throttle(s)                                    
#
# Author: Andrew Berridge, Lionel Jeanson 2010 - Based on USBThrottle.py, Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#
# Requires:
# XBCD Xbox Controller Driver (from http://www.redcl0ud.com/xbcd.html)
# Original Xbox (not 360) controller
# 
# Currently tested on Windows only

print "Loading USBDriver : XBCD XBox Gamepad"

class USBDriver :
    def __init__(self):
        self.componentThrottleFrame = "Hat Switch"  # Component for throttle frames browsing
        self.valueNextThrottleFrame = 0.5
        self.valuePreviousThrottleFrame = 1
        
        # From there available only when no throttle is active in current window  
        self.componentRosterBrowse = "Hat Switch"  # Component for roster browsing
        self.valueNextRoster = 0.75
        self.valuePreviousRoster = 0.25
        
        self.componentRosterSelect = "7"  # Component to select a roster
        self.valueRosterSelect = 1
        
        # From there available only when a throttle is active in current window        
        self.componentThrottleRelease = "8"  # Component to release current throttle
        self.valueThrottleRelease = 1

        self.componentSpeed = "x"  # Analog axis component for curent throttle speed
        self.valueSpeedTrigger = 0.07

        self.componentDirection = "z" # Analog axis component for curent throttle direction
        self.valueDirectionForward = 1
        self.valueDirectionBackward = -1

        self.componentStopSpeed = "9" # Preset speed button stop (0) (-1 on double tap = EStop)
        self.valueStopSpeed = 1
    
        self.componentSlowSpeed = "" # Preset speed button slow (0.3)
        self.valueSlowSpeed = 1 
    
        self.componentCruiseSpeed = "" # Preset speed button cruise (0.8)
        self.valueCruiseSpeed = 1
    
        self.componentMaxSpeed = "" # Preset speed button max (1)
        self.valueMaxSpeed = 1

        self.componentF0 = "6" # Function button
        self.valueF0 = 1
        self.valueF0Off = 0  # off event for non lockable functions

        self.componentF1 = "2" # Function button
        self.valueF1 = 1 
        self.valueF1Off = 0  # off event for non lockable functions

        self.componentF2 = "1" # Function button
        self.valueF2 = 1
        self.valueF2Off = 0  # off event for non lockable functions

        self.componentF3 = "0" # Function button
        self.valueF3 = 1
        self.valueF3Off = 0  # off event for non lockable functions

        self.componentF4 = "5" # Function button
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
