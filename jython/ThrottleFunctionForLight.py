# Map a Light's state to a specified Function on a specified loco address
#
# From jython/ThrottleFunctionForSensor.py by Steve Todd, copyright 2014
# Author: Bob Jacobsen, copyright 2016
# Part of the JMRI distribution
#

import jmri
import java
import java.beans

import jmri.Light.ON         as ON
import jmri.Light.OFF        as OFF

from org.apache.log4j import Logger

# Define one listener. 
class ThrottleFunctionForLightListener(java.beans.PropertyChangeListener):
  throttle = None  #make a spot to remember the throttle object that was passed in
  fnKey = None
  def setup(self, light, throttle, fnKey) :
    if (light == None) : return
    self.throttle = throttle  #store for use later
    self.fnKey = fnKey  #store for use later
    lights.provideLight(light).addPropertyChangeListener(self)
    return
  def propertyChange(self, event):
    funcName = "set" + self.fnKey  #dynamically determine name for setFxx function to call
    func = getattr(self.throttle, funcName)
    if (event.newValue == jmri.Light.ON) :
        logger.debug("set "+self.fnKey+" ON  for " + str(self.throttle.getLocoAddress()))
        func(True)  #turn on function when light goes thrown
    else :
        logger.debug("set "+self.fnKey+" OFF for " + str(self.throttle.getLocoAddress()))
        func(False)  #turn off function when light goes closed (or anything but thrown)
    return

class ThrottleLightAutomaton(jmri.jmrit.automat.AbstractAutomaton) :
        #perform actions that need to be in a thread, such as loco acquisition 
        def init(self):
#             logger.debug("Inside ThrottleLightAutomaton.init("+self.lightName+","+str(self.throttleAddress)+","+self.fnKeyName+")")
            self.throttle = self.getThrottle(self.throttleAddress, True)
            # actually attach the light to the loco
            ThrottleFunctionForLightListener().setup(self.lightName, self.throttle, self.fnKeyName)
            return    

        #pass and store needed values for this instance, then start the thread
        def setup(self, lightName, throttleAddress, fnKeyName):
            self.lightName = lightName
            self.throttleAddress = throttleAddress           
            self.fnKeyName = fnKeyName           
            self.start()
            self.waitMsec(100)  #give it a chance to happen

####################################################################
logger = Logger.getLogger("jmri.jmrit.jython.exec.ThrottleFunctionForLight")

# Connect each light to its loco and function, repeat as needed
# (These are samples, change as needed for your layout; use whatever pattern helps you remember)
ThrottleLightAutomaton().setup("IL401", 40, "F1")  # Light 401 controls F1 on address 40
ThrottleLightAutomaton().setup("IL402", 40, "F2")  # Light 402 controls F2 on address 40
ThrottleLightAutomaton().setup("IL403", 40, "F3")
ThrottleLightAutomaton().setup("IL404", 40, "F4")
ThrottleLightAutomaton().setup("IL405", 40, "F5")

