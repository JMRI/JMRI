
# Demonstrates the use Roster.SpeedProfile to consist 2 locomotives that have Speed Profiles, but are not speed matched. The speed profiles
# need to be accurate. 3 points will work. 100% throttle should be one of the points. One point should be close to slow.
# slowerLoco is the roster ID of the lead locomotive, its stop speed must be equal or less than the secondLoco.
# slowerloco is the Lead and must be controlled via a JMRI throttle/wifi throttle that has been selected by Roster entry, not DCC address.
# secondLoco is the roster ID of the other member of the consist.
# secondLocoGoingBackward must be True if the loco is running in the reverse direction to the slowerLoco, else it must be False
# stopScriptSensorName is the name of the sensor that will terminate the script when it goes Active. It will created if it doesnt exists.
#
# Author: Steve Gigiel, copyright 2020
#

import jmri

import java

# These 4 values need to be set for your layout.
slowerLoco = "CP 7401 EMD SW900"   # Roster ID of lead loco, must be slower top speed of all locos in the consist
secondLoco = "CN 6769 FPA4"   # Roster ID of second and faster loco
secondLocoGoingBackward = False   # false if direction is same as 1st loco 
stopScriptSensorName = "IS0101"

class ThrottleListener(java.beans.PropertyChangeListener):

    def propertyChange(self, event):
      if (event.propertyName == "SpeedSetting") :
         print "Set throttle2 ", event.newValue
         directionIsForward = throttle1.getIsForward()
         if (directionIsForward) :
            mms = speedProfile1.getForwardSpeed(event.newValue)
         else:
            mms = speedProfile1.getReverseSpeed(event.newValue)
         if (secondLocoGoingBackward) :
            throttleSet = speedProfile2.getThrottleSetting(mms, not directionIsForward)
         else:
            throttleSet = speedProfile2.getThrottleSetting(mms, directionIsForward)
         throttle2.setSpeedSetting(throttleSet)
      if (event.propertyName == "IsForward") :
         directionIsForward = event.newValue
         if ( secondLocoGoingBackward ) :
           throttle2.setIsForward( not directionIsForward)
         else:
           throttle2.setIsForward( directionIsForward)
      return

ThrottleListener1 = ThrottleListener()

class StopSensorListener(java.beans.PropertyChangeListener):


    def propertyChange(self, event):
      print event.propertyName
      if (event.propertyName == "KnownState") :
        print event.newValue
        if ( event.newValue == 2) :
          print "ActiveActive"
          throttle1.removePropertyChangeListener(ThrottleListener1)
          throttle2.release(None)
          throttle1.release(None)
      

StopListener = StopSensorListener();

class SoftConsist(jmri.jmrit.automat.AbstractAutomaton) :
  def init(self):

    # get roster.
    roster = jmri.jmrit.roster.Roster.getDefault()

    rosterEntry1 = roster.getEntryForId(slowerLoco)
    print rosterEntry1.getFileName()
    global speedProfile1
    speedProfile1 = rosterEntry1.getSpeedProfile()

    rosterEntry2 = roster.getEntryForId(secondLoco)
    print rosterEntry2.getFileName()
    global speedProfile2
    speedProfile2 = rosterEntry2.getSpeedProfile()

    # get throttles
    global throttle1
    throttle1 = self.getThrottle(rosterEntry1)
    global throttle2
    throttle2 = self.getThrottle(rosterEntry2)
    throttle1.addPropertyChangeListener(ThrottleListener1)
    global stopSensor
    stopSensor = sensors.provideSensor(stopScriptSensorName) 
    stopSensor.addPropertyChangeListener(StopListener)
    print "Initialized"
    return


  def handle(self):
    self.waitMsec(1000)
    if ( stopSensor.getState() == ACTIVE ) :
      print "ActiveActive"
      throttle1.removePropertyChangeListener(ThrottleListener1)
      throttle2.release(None)
      throttle1.release(None)
      return 0
    return 1

sc = SoftConsist()
sc.start()

        




