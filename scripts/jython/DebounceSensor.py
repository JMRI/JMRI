# This script manages an internal sensor as a debounced
# version of another sensor. Both the on and off delays
# may be specified.
#
# Author: Ken Cameron, copyright 2008
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.5 $
##
# A ActionListener is used to get timeout events.
#
# A PropertyChangeListener is used to get the sensor changes.
#

import java
import javax.swing

# declare things
class DebounceSensor(java.beans.PropertyChangeListener) :
    # initialize variables
    watchedSensor = None
    watchedSensorName = None
    resultSensor = None
    resultSensorName = None
    delayTimer = None
    offTimeout = 0
    onTimeout = 0
    priorInput = None
    currentState = None
    isChangingOn = False
    isChangingOff = False
    timeoutListener = None
    hasDisplay = False

    def init(self, inSensor, outSensor, onDelay, offDelay):
        self.watchedSensorName = inSensor
        self.resultSensorName = outSensor
        self.watchedSensor = sensors.getSensor(inSensor)
        if (self.watchedSensor == None) :
           print("Couldn't assign inSensor: " + inSensor + " - Run stopped\n")
           return
        self.resultSensor = sensors.getSensor(outSensor)
        if (self.resultSensor == None) :
           print("Couldn't assign outSensor: " + outSensor + " - Run stopped\n")
           return
        self.onTimeout = int(onDelay * 1000.0)
        self.offTimeout = int(offDelay * 1000.0)
        self.timeoutListener = self.TimeoutReceiver()
        self.timeoutListener.setCallBack(self.timeoutHandler)
        self.delayTimer = javax.swing.Timer(10000, self.timeoutListener)
        self.delayTimer.stop()
        self.delayTimer.setRepeats(False);
        self.currentState = self.watchedSensor.getKnownState()
        self.priorInput = self.currentState
        self.resultSensor.setKnownState(self.currentState)
        self.watchedSensor.addPropertyChangeListener(self)
        print("debounce init " + self.resultSensorName + " watching " + self.watchedSensorName)
        return
        
    def propertyChange(self, event) :
        # I don't bother with the event, I just check everything again
        self.delayTimer.stop()
        newState = self.watchedSensor.getKnownState()
        if (self.isChangingOn == False & self.isChangingOff == False) :
            # nothing changing
            if (newState == ACTIVE) :
                self.changeOffToOn()
            else :
                self.changeOnToOff()
        elif (self.isChangingOn & self.isChangingOff == False) :
            # we were changing to on, but got another change
            if (newState == INACTIVE) :
                # must have been a spike before the on timeout
                self.isChangingOn = False
        elif (self.isChangingOn == False & self.isChangingOff) :
            # we were changing to off, but got another change
            if (newState == ACTIVE) :
                # must have been a spike before off timeout
                self.isChangingOff = False
        else :
            # shouldn't get here, so reset everything
            self.resetSensors(newState)
        return

    def changeOnToOff(self) :
        if (self.offTimeout != 0) :
            self.isChangingOff = True
            self.delayTimer.setInitialDelay(self.offTimeout)
            self.delayTimer.start()
        else :
            self.timeoutOnToOff()
        return

    def changeOffToOn(self) :
        # the source went from off to on
        if (self.onTimeout != 0) :
            self.isChangingOn = True
            self.delayTimer.setInitialDelay(self.onTimeout)
            self.delayTimer.start()
        else :
            self.timeoutOffToOn()
        return

    def resetSensors(self, newState) :
        self.isChangingOn = False
        self.isChangingOff = False
        self.currentState = newState
        self.resultSensor.setKnownState(newState)
        return
        
    class TimeoutReceiver(java.awt.event.ActionListener):
        cb = None

        def actionPerformed(self, event) :
            if (self.cb != None) :
                self.cb(event)
            return
        
        def setCallBack(self, cbf) :
            self.cb = cbf
            return

    def timeoutHandler(self, event) :
        # see which phase we think we are in
        self.delayTimer.stop()
        newState = self.watchedSensor.getKnownState()
        if (self.isChangingOn == False & self.isChangingOff == False) :
            # nothing changing, this shouldn't happen, 
            self.resetSensors(newState)
        elif (self.isChangingOn & self.isChangingOff == False) :
            # we were changing to on, but got another change
            self.timeoutOnToOff()
        elif (self.isChangingOn == False & self.isChangingOff) :
            # we were changing to off, but got another change
            self.timeoutOffToOn()
        else :
            # shouldn't get here, so reset everything
            self.resetSensors()
        return

    def timeoutOnToOff(self) :
        self.resultSensor.setKnownState(INACTIVE)
        self.currentState = INACTIVE
        self.isChangingOff = False
        return

    def timeoutOffToOn(self) :
        self.resultSensor.setKnownState(ACTIVE)
        self.currentState = ACTIVE
        self.isChangingOn = False
        return
    
# invoke this script from the system directory
# then create a file (with your panels) that has
# lines like these (without the ## comments)
# Params:
#    real sensor being watched for changes
#    sensor showing the debounced state of real sensor
#    on delay, off-on-off cycles less than this will stay off
#    off delay, on-off-on cycles less than this will stay on
#
# Sensor names are system names
# time values are floating point seconds

##a = DebounceSensor() # create one of these 
##a.init('NS775', 'ISNS775', 0.5, 3) # invoke this for the sensor pair