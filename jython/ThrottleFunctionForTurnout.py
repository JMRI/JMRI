# Map a Turnout's state to a specified Function on a specified loco address
#
# From jython/ThrottleFunctionForSensor.py by Steve Todd, copyright 2014
# Author: Bob Jacobsen, copyright 2016
# Part of the JMRI distribution
#

import jmri
import java
import java.beans

from org.apache.log4j import Logger

# Define one listener. 
class ThrottleFunctionForTurnoutListener(java.beans.PropertyChangeListener):
  throttle = None  #make a spot to remember the throttle object that was passed in
  fnKey = None
  def setup(self, turnout, throttle, fnKey) :
    if (turnout == None) : return
    self.throttle = throttle  #store for use later
    self.fnKey = fnKey  #store for use later
    turnouts.provideTurnout(turnout).addPropertyChangeListener(self)
    return
  def propertyChange(self, event):
    funcName = "set" + self.fnKey  #dynamically determine name for setFxx function to call
    func = getattr(self.throttle, funcName)
    if (event.newValue == THROWN) :
        logger.debug("set "+self.fnKey+" ON  for " + str(self.throttle.getLocoAddress()))
        func(True)  #turn on function when turnout goes thrown
    else :
        logger.debug("set "+self.fnKey+" OFF for " + str(self.throttle.getLocoAddress()))
        func(False)  #turn off function when turnout goes closed (or anything but thrown)
    return

class ThrottleTurnoutAutomaton(jmri.jmrit.automat.AbstractAutomaton) :
        #perform actions that need to be in a thread, such as loco acquisition 
        def init(self):
#             logger.debug("Inside ThrottleTurnoutAutomaton.init("+self.turnoutName+","+str(self.throttleAddress)+","+self.fnKeyName+")")
            self.throttle = self.getThrottle(self.throttleAddress, True)
            # actually attach the turnout to the loco
            ThrottleFunctionForTurnoutListener().setup(self.turnoutName, self.throttle, self.fnKeyName)
            return    

        #pass and store needed values for this instance, then start the thread
        def setup(self, turnoutName, throttleAddress, fnKeyName):
            self.turnoutName = turnoutName
            self.throttleAddress = throttleAddress           
            self.fnKeyName = fnKeyName           
            self.start()
            self.waitMsec(100)  #give it a chance to happen

####################################################################
logger = Logger.getLogger("jmri.jmrit.jython.exec.ThrottleFunctionForTurnout")

#connect each turnout to its loco and function, repeat as needed
# (These are samples, change as needed for your layout; use whatever pattern helps you remember)
ThrottleTurnoutAutomaton().setup("IT401", 40, "F1")  # Turnout 401 controls F1 on address 40
ThrottleTurnoutAutomaton().setup("IT402", 40, "F2")  # Turnout 402 controls F2 on address 40
ThrottleTurnoutAutomaton().setup("IT403", 40, "F3")
ThrottleTurnoutAutomaton().setup("IT404", 40, "F4")
ThrottleTurnoutAutomaton().setup("IT405", 40, "F5")

