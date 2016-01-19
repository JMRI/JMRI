# Connect a AAR105 Control Stand (USB device) to a throttle.
#
# ATTENTION: Special version for HidRawEnvironmentPlugin of JInput
# ATTENTION: Currently works in Windows only
#
# See <http://jmri.sf.net/help/en/html/hardware/raildriver/index.shtml>
#
# Author: Lionel Jeanson, copyright 2011
# - Based on Joan Carranc� AAR105.py, copyright 2010
# - Based on the original RailDriver.py, Bob Jacobsen, copyright 2008
# - Throttle window management and roster selection based on xboxThrottle.py, Andrew Berridge, copyright 2010
# Part of the JMRI distribution
#
# This is still experimental code and is under development. Currently, it supports:
# 2. All blue buttons assigned to functions (0-27)
# 3. Leftmost lever as reverser - front moves forwards, back reverses
# 4. Second from left lever as throttle - back increases speed
# 5. Selection of locos using the "zoom" rocker switch. Up selects a loco, Down dispatches
#      current loco.
# 6. Selection of loco addresses / throttle panels using the "pov" four-way switch.
#    Using up and down on the pov switch will move between locos in the roster
#      (as long as the last loco has been "dispatched")
#    Using left and right on the pov switch will select between throttle panels... Click the
#      plus (+) button on the throttle window to add another panel.
# 7. Emergency stop! Push the corresponding button(s). This will e-stop the current throttle.
# 8. Gear buttons: High/Low range can be selected via these buttons, mapped to "shuntFn" function
# 9. Cab buttons: alerter, sander, pantograph and bell buttons can be mapped to a function
#10. Horn momentary lever: mapped to "hornFn" function
#
# Future development ideas:
# 1. Think of functionalities for the other levers
# 2. Other uses for the display ??
# 3. Calibration ??
#
##
# IMPORTANT Warnings:
# 1. Make sure you calibrate and set a "Dead zone" for each of the analogue levers in the
# Calibration window! If you don't there will be too many events triggered
# and everything will slow right down....

print "Loading USBDriver : AAR105 Control Stand"

class USBDriver :
    def __init__(self):
        self.componentNextThrottleFrame = "8"  # Component for throttle frames browsing
        self.valueNextThrottleFrame = 1
        self.componentPreviousThrottleFrame = "9"
        self.valuePreviousThrottleFrame = 1

        self.componentNextRunningThrottleFrame = ""  # Component for running throttle frames browsing
        self.valueNextRunningThrottleFrame = 0
        self.componentPreviousRunningThrottleFrame = ""
        self.valuePreviousRunningThrottleFrame = 0

        # From there available only when no throttle is active in current window
        self.componentNextRosterBrowse = "3"  # Component for roster browsing
        self.valueNextRoster = 1
        self.componentPreviousRosterBrowse = "7"
        self.valuePreviousRoster = 0

        self.componentRosterSelect = "1"  # Component to select a roster
        self.valueRosterSelect = 1

        # From there available only when a throttle is active in current window
        self.componentThrottleRelease = "2"  # Component to release current throttle
        self.valueThrottleRelease = 1

        self.componentSpeed = ""  # Analog axis component for curent throttle speed
        self.valueSpeedTrigger = 0 # ignore values lower than
        self.componentSpeedMultiplier = 1 # multiplier for pad value (negative values to reverse)

        self.componentSpeedSet = "Axis 0"
        self.valueSpeedSetMinValue = 0.05 # value of component for zero speed
        self.valueSpeedSetMaxValue = 1.00 # value of component for max speed

        self.componentSpeedIncrease = ""
        self.valueSpeedIncrease = 1

        self.componentSpeedDecrease = ""
        self.valueSpeedDecrease = 1

        self.componentDirectionForward = "6"
        self.valueDirectionForward = 1

        self.componentDirectionBackward = "5"
        self.valueDirectionBackward = 1

        self.componentDirectionSwitch = "" # A single component to switch speed
        self.valueDirectionSwitch = 1

        self.componentEStopSpeed = "10" # Emergency speed button
        self.valueEStopSpeed = 1
        self.componentEStopSpeedBis = "11"
        self.valueEStopSpeedBis = 1
        
        self.componentStopSpeed = "4" # Preset speed button stop, double tap will Estop
        self.valueStopSpeed = 1

        self.componentSlowSpeed = "" # Preset speed button slow
        self.valueSlowSpeed = 1

        self.componentCruiseSpeed = "" # Preset speed button cruise, double tap will max speed
        self.valueCruiseSpeed = 1

        self.componentMaxSpeed = "" # Preset speed button max
        self.valueMaxSpeed = 1

        self.componentF0 = "16" # Function button
        self.valueF0 = 1
        self.valueF0Off = 0  # off event for non lockable functions

        self.componentF1 = "15" # Function button
        self.valueF1 = 1
        self.valueF1Off = 0

        self.componentF2 = "12" # Function button
        self.valueF2 = 1
        self.valueF2Off = 0

        self.componentF3 = "13" # Function button
        self.valueF3 = 1
        self.valueF3Off = 0

        self.componentF4 = "17" # Function button
        self.valueF4 = 1
        self.valueF4Off = 0

        self.componentF5 = "18" # Function button
        self.valueF5 = 1
        self.valueF5Off = 0

        self.componentF6 = "19" # Function button
        self.valueF6 = 1
        self.valueF6Off = 0

        self.componentF7 = "14" # Function button
        self.valueF7 = 1
        self.valueF7Off = 0

        self.componentF8 = "20" # Function button
        self.valueF8 = 1
        self.valueF8Off = 0

        self.componentF9 = "21" # Function button
        self.valueF9 = 1
        self.valueF9Off = 0

        self.componentF10 = "22" # Function button
        self.valueF10 = 1
        self.valueF10Off = 0

        self.componentF11 = "23" # Function button
        self.valueF11 = 1
        self.valueF11Off = 0

        self.componentF12 = "24" # Function button
        self.valueF12 = 1
        self.valueF12Off = 0

        self.componentF13 = "25" # Function button
        self.valueF13 = 1
        self.valueF13Off = 0

        self.componentF14 = "26" # Function button
        self.valueF14 = 1
        self.valueF14Off = 0

        self.componentF15 = "27" # Function button
        self.valueF15 = 1
        self.valueF15Off = 0

        self.componentF16 = "28" # Function button
        self.valueF16 = 1
        self.valueF16Off = 0

        self.componentF17 = "29" # Function button
        self.valueF17 = 1
        self.valueF17Off = 0

        self.componentF18 = "30" # Function button
        self.valueF18 = 1
        self.valueF18Off = 0

        self.componentF19 = "31" # Function button
        self.valueF19 = 1
        self.valueF19Off = 0

        self.componentF20 = "32" # Function button
        self.valueF20 = 1
        self.valueF20Off = 0

        self.componentF21 = "33" # Function button
        self.valueF21 = 1
        self.valueF21Off = 0

        self.componentF22 = "34" # Function button
        self.valueF22 = 1
        self.valueF22Off = 0

        self.componentF23 = "35" # Function button
        self.valueF23 = 1
        self.valueF23Off = 0

        self.componentF24 = "36" # Function button
        self.valueF24 = 1
        self.valueF24Off = 0

        self.componentF25 = "37" # Function button
        self.valueF25 = 1
        self.valueF25Off = 0

        self.componentF26 = "38" # Function button
        self.valueF26 = 1
        self.valueF26Off = 0

        self.componentF27 = "39" # Function button
        self.valueF27 = 1
        self.valueF27Off = 0

        self.componentF28 = "40" # Function button
        self.valueF28 = 1
        self.valueF28Off = 0

        self.componentF29 = "41" # Function button
        self.valueF29 = 1
        self.valueF29Off = 0
