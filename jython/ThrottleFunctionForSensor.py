# Map a sensor state to a specified Function on a specified loco address
#
# used at MWOT to let visitors control the horn and lights on locos, via fascia buttons 
#
# Author: Steve Todd, copyright 2014
# Part of the JMRI distribution
#

import jmri
import java
import java.beans

from org.apache.log4j import Logger

# Define one sensor listener. 
class ThrottleFunctionForSensorListener(java.beans.PropertyChangeListener):
  throttle = None  #make a spot to remember the throttle object that was passed in
  fnKey = None
  def setup(self, sensor, throttle, fnKey) :
    if (sensor == None) : return
    self.throttle = throttle  #store for use later
    self.fnKey = fnKey  #store for use later
    sensors.provideSensor(sensor).addPropertyChangeListener(self)
    return
  def propertyChange(self, event):
    funcName = "set" + self.fnKey  #dynamically determine name for setFxx function to call
    func = getattr(self.throttle, funcName)
    if (event.newValue == ACTIVE) :
        logger.debug("set "+self.fnKey+" ON  for " + str(self.throttle.getLocoAddress()))
        func(True)  #turn on function when sensor goes high
    else :
        logger.debug("set "+self.fnKey+" OFF for " + str(self.throttle.getLocoAddress()))
        func(False)  #turn off function when sensor goes low
    return

class Automaton(jmri.jmrit.automat.AbstractAutomaton) :
        #perform actions that need to be in a thread, such as loco acquisition 
        def init(self):
#             logger.debug("Inside Automaton.init("+self.sensorName+","+str(self.throttleAddress)+","+self.fnKeyName+")")
            self.throttle = self.getThrottle(self.throttleAddress, True)
            # actually attach the sensor to the loco
            ThrottleFunctionForSensorListener().setup(self.sensorName, self.throttle, self.fnKeyName)
            return    

        #pass and store needed values for this instance, then start the thread
        def setup(self, sensorName, throttleAddress, fnKeyName):
            self.sensorName = sensorName
            self.throttleAddress = throttleAddress           
            self.fnKeyName = fnKeyName           
            self.start()
            self.waitMsec(500)  #give it a chance to happen

####################################################################
logger = Logger.getLogger("jmri.jmrit.jython.exec.ThrottleFunctionForSensor")

#connect each sensor to its loco and function, repeat as needed
Automaton().setup("LS1003", 909, "F1")  #horn for address 909
Automaton().setup("LS1004", 909, "F0")  #lights for same loco

