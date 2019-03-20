#  set up listener(s) for a sensor to control the "Held" state of a signal
#    sensor active sets signal to released, inactive sets to held
#    only fires on sensor change
#
#    we use this at MWOT to allow a push-button to release a train and 
#    start an AutoDispatcher2 run
#    based on SignalHeadFromSensors.py
#
# Author: Steve Todd, copyright 2014
# Part of the JMRI distribution

import jmri
import java
import java.beans

from org.apache.log4j import Logger

# Define one sensor listener. 
class HoldSignalForSensorListener(java.beans.PropertyChangeListener):
  def setup(self, sensor, signal) :
    if (sensor == None) : return
    logger.debug("setting " + sensor + " to hold/release " + signal)
    self.signal = signals.getSignalHead(signal)
    sensors.provideSensor(sensor).addPropertyChangeListener(self)
    return
  def propertyChange(self, event):
    if (event.newValue == ACTIVE) :
        logger.debug("release " + self.signal.getSystemName())
        self.signal.setHeld(False)  #release when sensor goes high
    else :
        logger.debug("hold " + self.signal.getSystemName())
        self.signal.setHeld(True)  # hold when sensor goes low        
    return

####################################################################
logger = Logger.getLogger("jmri.jmrit.jython.exec.HoldSignalForSensor")
   
#set up each sensor to signalhead connection, repeat as needed.   
#  note: signalhead must already exist
HoldSignalForSensorListener().setup("LS1001","HIY152cw")
HoldSignalForSensorListener().setup("LS1002","HIY155cw")
 
