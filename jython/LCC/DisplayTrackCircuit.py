# This script manages a set of sensors to follow
# a RR-CirKits track circuit. It uses 8 sensors in a
# state machine to update a memory for text display
# of the track circuit state.
#
# Author: Ken Cameron, copyright 2022
# Part of the JMRI distribution
#
# A PropertyChangeListener is used to get the sensor changes.
#
# invoke this script from the system directory
# then create a file (with your panels) that has
# lines like these (without the ## comments)
# Params:
#    base event for Track Circuit as a sensor
#    name of memory for displaying state
#
# Sensor name can be system name or user name
# Memory name must be user name
# If the sensors do not exist, they will be created with user names formed
#   from the memory name and TC states
# You can also put these lines into Logix or LogixNG script features, like an initialzation script

##a = DisplayTrackCircuit() # create one of these
##a.init('MS02.01.57.10.00.66.01.A0', 'CP21 South Main TC') # invoke this for the track circuit
## or using a user name
##a.init('CP 21 South Main TC - Stop', 'CP21 South Main TC') # invoke this for the track circuit
#
# then create more, like this:
##b = DisplayTrackCircuit() # create one of these
##b.init('MS02.01.57.10.00.66.01.C0', 'CP21 South Side TC') # invoke this for the track circuit
## or using a user name
##b.init('CP 21 South Side TC - Stop', 'CP21 South Side TC') # invoke this for the track circuit

import jmri

import java
import javax.swing
import sys

# declare things
class DisplayTrackCircuit(java.beans.PropertyChangeListener) :
    # initialize variables
    failed = False
    baseSensorSystemName = None
    memoryName = None
    memory = None
    sensorList = []
    sensorNameList = []
    sensorListeners = []
    sensorListenerNames = []
    stateNames = [ "Stop", "Restrict", "Slow",
        "Medium", "Limited", "Approach", "ApprMedium", "Clear" ]
    debugLevel = 0

    def init(self, inSensor, outMemory):
        self.baseSensorSystemName = None
        self.memoryName = None
        self.memory = None
        for x in range(len(self.sensorList) - 1, -1, -1) :
            s = self.sensorList.pop(x)
        for x in range(len(self.sensorNameList) - 1, -1, -1) :
            self.sensorNameList.pop(x)
        for x in range(len(self.sensorListeners) - 1, -1, -1) :
            self.sensorListeners.pop(x)
        for x in range(len(self.sensorListenerNames) - 1, -1, -1) :
            self.sensorListenerNames.pop(x)
        #print("size of stateNames: " + str(len(self.stateNames)))
        #for i in range(0,8) :
        #    print("state name[" + str(i) +"]: " + self.stateNames[i])
        # if sensors don't exist, create them
        self.createSensorSet(inSensor, outMemory)
        if (self.failed) :
            return
        # Create/get memory
        try :
            self.memory = memories.getByUserName(outMemory)
        except :
            print("TC: error getting memory")
            print("TC: unexpected error:", sys.exc_info()[0])
        if (self.memory == None) :
            try :
                self.memory = memories.newMemory(outMemory)
            except :
                print("TC: failed to get/create memory: " + outMemory)
                print("TC: unexpected error:", sys.exc_info()[0])
                return
        if (self.memory == None) :
            print("TC: failed getting memory")
            return
        # create/attach listener for sensors
        for s in self.sensorList :
            l = self.SensorListener()
            self.sensorListeners.append(l)
            l.setCallBack(self.sensorHandler)
            l.setSensor(s)
            idx = self.sensorList.index(s)
            #print("setting up listener [" + str(idx) + "]: " + s.getUserName())
            l.setText(self.stateNames[idx])
            s.addPropertyChangeListener(l)
        print("TC: " + outMemory + " setup complete")
        return

    # using sensor and memory name, find/create sensors
    def createSensorSet(self, inSensor, outMemory) :
        # test if sensor name is user name
        testSensor = None
        uName = outMemory + " - Stop"
        try :
            testSensor = sensors.getByUserName(inSensor)
        except :
            print("TC: Problem with sensor as user name: " + inSensor)
            print("TC: Unexpected error:", sys.exc_info()[0])
            self.failed = True
            return
        if (testSensor == None) :
            try :
                testSensor = sensors.getBySystemName(inSensor)
                #print("TC: found by system name: " + inSensor)
            except :
                print("TC: Problem with sensor by system name: " + inSensor)
                print("TC: Unexpected error:", sys.exc_info()[0])
                self.failed = True
                return
        if (testSensor == None) :
            try:
                testSensor = sensors.newSensor(inSensor, uName)
                print("TC: created: " + inSensor + " (" + uName + ")")
                testSensor.setAuthoritative(False)
            except:
                print("TC: Problem with creating sensor name: " + inSensor + " (" + uName + ")")
                print("TC: Unexpected error:", sys.exc_info()[0])
                self.failed = True
                return
        if (testSensor == None) :
            print("TC: Sensor not found/created: " + inSensor)
            self.failed = True
            return
        # save this sensor
        sysName = testSensor.getSystemName()
        self.sensorList.append(testSensor)
        self.sensorNameList.append(sysName)
        # make base string
        self.baseString = sysName[0:len(sysName)-1]
        for i in range(1, 8) :
            testSys = self.baseString + str(i)
            testUser = outMemory + " - " + self.stateNames[i]
            testSensor = None
            #print("building sensor list. At: " + testSys + "(" + testUser + ")")
            try:
                # try for system name
                testSensor = sensors.getBySystemName(testSys)
            except:
                print("TC: get by system name for: " + testSys + "(" + testUser + ") failed")
                self.failed = True
                return
            if (testSensor == None) :
                print("TC: not found by system name: : " + testSys)
                try:
                    # create entry
                    testSensor = sensors.newSensor(testSys, testUser)
                    if (testSensor != None) :
                        print("TC: created sensor: " + testSys + " (" + testUser + ")")
                        testSensor.setAuthoritative(False)
                except:
                    print("TC: create for: " + testSys + "(" + testUser + ") failed")
                    self.failed = True
                    return
            if (testSensor == None) :
                self.failed = True
                return
            testUser = testSensor.getUserName()
            #print("TC: adding sensor: " + testUser + " list size: " + str(testSensor))
            self.sensorList.append(testSensor)
            self.sensorNameList.append(testSys)
        return

    #To detect sensor status, first define the listener.
    class SensorListener(java.beans.PropertyChangeListener):
        cb = None
        thisSensor = None
        thisText = None

        def setSensor(self, sensor) :
            self.thisSensor = sensor
            return

        def setText(self, txt) :
            self.thisText = txt
            return

        def setCallBack(self, ptr) :
            self.cb = ptr
            return

        def propertyChange(self, event):
            if (self.cb != None) :
                self.cb(self.thisSensor, self.thisText, event)
            return

    def sensorHandler(self, sensor, text, event) :
        # read event to see which sensor
        self.memory.setValue(text)
        #print("sensorHandler(" + text + ")")
        return

    def setDebugLevel(self, value) :
        if (value >= 0 and value <= 2) :
            self.debugValue = value
        return

    def getDebugLevel(self) :
        return self.debugLevel
