import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.awt.CardLayout as CardLayout
import jmri.util.ResizableImagePanel as ResizableImagePanel
import java.beans.PropertyChangeListener as PropertyChangeListener
import jmri.jmrit.throttle.AddressListener as AddressListener
import javax.swing.Timer as Timer
import java.awt.event.ActionListener as ActionListener
import java.util.Calendar as Calendar
import thread

# Use a USB device as a throttle
#
# Author: Lionel Jeanson, adapted from Bob Jacobsen, copyright 2008/2009
# Part of the JMRI distribution

# Set the name of the controller you're using
desiredControllerName = "Thrustmaster dual analog 3.2"

componentThrottleFrame = "pov"  # Component for throttle frames browsing
valueNextThrottleFrame = 0.5
valuePreviousThrottleFrame = 1

componentSpeed = "x"  # Analog axis component for curent throttle speed
valueSpeedTrigger = 0.05
valueSpeedDivider = 10
valueSpeedTimerRepeat = 125 # repeat time in ms for speed set task

componentDirection = "rz" # Analog axis component for curent throttle direction
valueDirectionForward = 1
valueDirectionBackward = -1

componentStopSpeed = "0" # Preset speed button stop
valueStopSpeed = 1
speedStopSpeed = 0
delay4EStop = 250 # max delay in ms for double tap => EStop
EStopSpeed = -1

componentSlowSpeed = "1" # Preset speed button slow
valueSlowSpeed = 1 
speedSlowSpeed = 0.3

componentCruiseSpeed = "2" # Preset speed button cruise
valueCruiseSpeed = 1
speedCruiseSpeed = 0.8

componentMaxSpeed = "3" # Preset speed button max
valueMaxSpeed = 1
speedMaxSpeed = 1

componentF0 = "4" # Function button
valueF0 = 1

componentF1 = "5" # Function button
valueF1 = 1 

componentF2 = "6" # Function button
valueF2 = 1

componentF3 = "7" # Function button
valueF3 = 1

componentF4 = "8" # Function button
valueF2 = 1

componentF5 = "9" # Function button
valueF3 = 1

class USBThrottle(Jynstrument, PropertyChangeListener, AddressListener):
#Property listener part: USB value
    def propertyChange(self, event):
        # Customize bellow for controls
        if (event.propertyName == "Value") :  # USB
            if (event.oldValue.getController().getName() == desiredControllerName ) :
                component = event.oldValue.getComponent().toString()
                value = event.newValue
                # Change curent ThrottleFrame
                if (component == componentThrottleFrame) :
                    if (value == valueNextThrottleFrame) : #NEXT
                        self.getContext().nextThrottleFrame()
                    if (value == valuePreviousThrottleFrame) : #PREVIOUS
                        self.getContext().previousThrottleFrame()
                # From there; curent throttle control, hence require a throttle
                if (self.throttle != None) :
                    # Speed
                    if (component == componentSpeed) :
                        self.speedAction.setSpeedIncrement(value / valueSpeedDivider)
                        if ( abs(value) > valueSpeedTrigger ) :
                            self.speedTimer.start()
                        else :
                            self.speedTimer.stop()
                    else :
                        self.speedTimer.stop() # just in case, stop it
                        
                    # Direction
                    if (component == componentDirection) :
                        if (value == valueDirectionForward) :
                            self.throttle.setIsForward(True)
                        if (value == valueDirectionBackward) :
                            self.throttle.setIsForward(False)
                            
                    # Speed presets
                    if ((component == componentStopSpeed) and (value == valueStopSpeed)) :
                        self.throttle.setSpeedSetting(speedStopSpeed)
                        if ( Calendar.getInstance().getTimeInMillis() - self.lastTimeStopButton < delay4EStop ) : # EStop on double tap
                            self.throttle.setSpeedSetting(EStopSpeed)
                        self.lastTimeStopButton = Calendar.getInstance().getTimeInMillis()
                    if ((component == componentSlowSpeed) and (value == valueSlowSpeed)) :
                        self.throttle.setSpeedSetting(speedSlowSpeed)
                    if ((component == componentCruiseSpeed) and (value == valueCruiseSpeed)) :
                        self.throttle.setSpeedSetting(speedCruiseSpeed)
                    if ((component == componentMaxSpeed) and (value == valueMaxSpeed)) :
                        self.throttle.setSpeedSetting(speedMaxSpeed)
                        
                    # Functions
                    if ((component == componentF0) and (value == valueF0)) :
                        self.throttle.setF0( not self.throttle.getF0() )
                    if ((component == componentF1) and (value == valueF1)) :
                        self.throttle.setF1( not self.throttle.getF1() )
                    if ((component == componentF2) and (value == valueF2)) :
                        self.throttle.setF2( not self.throttle.getF2() )
                    if ((component == componentF3) and (value == valueF3)) :
                        self.throttle.setF3( not self.throttle.getF3() )
                    if ((component == componentF4) and (value == valueF4)) :
                        self.throttle.setF4( not self.throttle.getF4() )
                    if ((component == componentF5) and (value == valueF5)) :
                        self.throttle.setF5( not self.throttle.getF5() )
                        
        # Nothing to customize bellow this point
        if (event.propertyName == "ThrottleFrame") :  # Curent throttle frame changed
            self.speedTimer.stop()
            event.oldValue.getAddressPanel().removeAddressListener(self)
            self.throttle = event.newValue.getAddressPanel().getThrottle()
            self.speedAction.setThrottle( self.throttle )
            event.newValue.getAddressPanel().addAddressListener(self)

#Jynstrument main and mandatory methods
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleWindow"
    
    def init(self):
        self.getContext().addPropertyChangeListener(self) #ThrottleFrame change
        self.getContext().getCurentThrottleFrame().getAddressPanel().addAddressListener(self) # change of throttle in curent frame
        self.throttle = self.getContext().getCurentThrottleFrame().getAddressPanel().getThrottle() # the throttle
        self.speedAction =  SpeedAction()  #Speed increase thread
        self.speedAction.setThrottle( self.throttle )
        self.speedTimer = Timer(valueSpeedTimerRepeat, self.speedAction ) # Very important to use swing Timer object (see Swing and multithreading doc)
        self.speedTimer.setRepeats(True)
        self.label = ResizableImagePanel(self.getFolder() + "/USBControl.png",20,20 ) #label
        self.add(self.label)
        self.model = jmri.jmrix.jinput.TreeModel.instance() # USB
        self.model.addPropertyChangeListener(self)
        self.lastTimeStopButton = Calendar.getInstance().getTimeInMillis()
       
    def quit(self):
        self.speedTimer.stop() 
        self.speedAction = None
        self.speedTimer = None
        self.throttle = None
        self.getContext().removePropertyChangeListener(self)
        self.model.removePropertyChangeListener(self)
        self.getContext().getCurentThrottleFrame().getAddressPanel().removeAddressListener(self)

#AddressListener part: to listen for address changes in address panel (release, acquired)
    def notifyAddressChosen(self, address, isLong):
        pass
        
    def notifyAddressThrottleFound(self, throttle):
        self.speedTimer.stop() 
        self.throttle = throttle
        self.speedAction.setThrottle( self.throttle )
            
    def notifyAddressReleased(self, address, isLong):
        self.speedTimer.stop()
        self.throttle = None
        self.speedAction.setThrottle( self.throttle )
                
# Speed timer class, to increase speed regularly once button pushed, thread stopped on button release
class SpeedAction(ActionListener):
    def __init__(self):
        self.sync = thread.allocate_lock() # Protects properties getter and setter
        self.speedIncrement = 0
        self.throttle = None

    def setSpeedIncrement(self, si):
        self.sync.acquire()
        self.speedIncrement = si
        self.sync.release()

    def getSpeedIncrement(self):
        self.sync.acquire()
        si = self.speedIncrement
        self.sync.release()
        return si

    def setThrottle(self, throt):
        self.sync.acquire()
        self.throttle = throt
        self.sync.release()
    
    def getThrottle(self):
        self.sync.acquire()
        throt = self.throttle
        self.sync.release()
        return throt

    def actionPerformed(self, e):
        throttle = self.getThrottle()
        spi = self.getSpeedIncrement()
        if (throttle != None) :
            ns =  throttle.getSpeedSetting() + spi
            if (ns < 0 ) :
                ns = 0
            if (ns > 1 ) :
                ns = 1
            throttle.setSpeedSetting( ns )


