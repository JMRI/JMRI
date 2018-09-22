#
# Manage a set of outputs for a stepper motor, interactive
#
# Author: Ken Cameron, copyright 2011
# Part of the JMRI distribution
#
# This class lets you create a light that is driven by a 4 pole
#   stepper motor connected to a normal lamp dimmer. A novel solution
#   to having your layout lighting tied to your fast clock.
#   But if you have to drive stepper motors (slowly) this works.
#   Yes there is an issue about the true order of the bits getting
#   to the motor but it seems for a CMRI setup, this hasn't been
#   an issue. The layout really using this is Jim Heidt's O&N
#   in Central New York.
#

import jmri

# setup interface to manage turnout outputs for a stepper
class StepperDriver(jmri.jmrit.automat.AbstractAutomaton) :
    lightList = []
    lightNameList = []
    bit0List = []
    bit1List = []
    bit2List = []
    bit3List = []
    lastChanged = []
    currentValueList = []
    maxIndex = 25 * 8
    coilSequence = [1, 5, 4, 6, 2, 10, 8, 9]
    currentDelay = 500
    showMsg = False

    def init(self) :
        #print "init()"
        return

    # handle() is called repeatedly until it returns false.
    def handle(self) :
        msg = "handle:"
        hasMoreToDo = False
        idx = 0
        while idx < len(self.lightList) :
            if (self.handleLite(idx) == True) :
                hasMoreToDo = True
            idx = idx + 1
        self.turnOffOutputs()
        if (hasMoreToDo == True) :
            msg = msg + "\ntimed wait"
            if (self.showMsg) :
                print msg
            self.waitMsec(self.currentDelay)
        elif (len(self.lightList) == 0) :
            msg = msg + "\nidle wait"
            if (self.showMsg) :
                print msg
            self.waitMsec(self.currentDelay * 10)
        else :
            msg = msg + "\nchange wait"
            if (self.showMsg) :
                print msg
            self.waitChange(self.lightList)
        return 1

    # turn off all outputs
    def turnOffOutputs(self) :
        idx = 0
        while idx < len(self.lightList) :
            if (self.lastChanged[idx] > 1) :
                self.lastChanged[idx] = self.lastChanged[idx] - 1
            elif (self.lastChanged[idx] == 1) :
                self.bit3List[idx].setCommandedState(CLOSED)
                self.bit2List[idx].setCommandedState(CLOSED)
                self.bit1List[idx].setCommandedState(CLOSED)
                self.bit0List[idx].setCommandedState(CLOSED)
                self.lastChanged[idx] = 0
                if (self.showMsg) :
                    print "turning off " + self.lightNameList[idx]
            idx = idx + 1
        return

    # make changes if needed
    def handleLite(self, idx) :
        lite = self.lightList[idx]
        req = int(lite.getCurrentIntensity() * self.maxIndex)
        if req > self.maxIndex :
            req = self.maxIndex
        cur = self.currentValueList[idx]
        retState = False
        if (cur != req) :
            #print "handleLite: " + self.lightList[idx].getSystemName()
            #print " cur: " + cur.toString()
            #print " req: " + req.toString()
            thisIdx = cur % 8
            thisOutput = self.coilSequence[thisIdx]
            #print "Light: " + self.lightNameList[idx] + " coilSequence[" + thisIdx.toString() + "]: " + thisOutput.toString()
            if (thisOutput & 0x08 != 0) :
                self.bit3List[idx].setCommandedState(THROWN)
            else :
                self.bit3List[idx].setCommandedState(CLOSED)
            if (thisOutput & 0x04 != 0) :
                self.bit2List[idx].setCommandedState(THROWN)
            else :
                self.bit2List[idx].setCommandedState(CLOSED)
            if (thisOutput & 0x02 != 0) :
                self.bit1List[idx].setCommandedState(THROWN)
            else :
                self.bit1List[idx].setCommandedState(CLOSED)
            if (thisOutput & 0x01 != 0) :
                self.bit0List[idx].setCommandedState(THROWN)
            else :
                self.bit0List[idx].setCommandedState(CLOSED)
            if (cur < req) :
                self.currentValueList[idx] = cur + 1
            else :
                self.currentValueList[idx] = cur - 1
            self.lastChanged[idx] = 2
            retState = True
        return retState

    # add lights to service list
    def addLight(self, lite, bit0, bit1, bit2, bit3) :
        bit0Bean = turnouts.provideTurnout(bit0)
        if (bit0Bean == None) :
            print "invalid bit0 provided: " + bit0
            return
        bit1Bean = turnouts.provideTurnout(bit1)
        if (bit1Bean == None) :
            print "invalid bit1 provided: " + bit1
            return
        bit2Bean = turnouts.provideTurnout(bit2)
        if (bit2Bean == None) :
            print "invalid bit2 provided: " + bit2
            return
        bit3Bean = turnouts.provideTurnout(bit3)
        if (bit3Bean == None) :
            print "invalid bit3 provided: " + bit3
            return
        lightBean = lights.provideLight(lite)
        if (lightBean == None) :
            print "invalid light provided: " + lite
            return
        self.bit3List.append(bit3Bean)
        self.bit2List.append(bit2Bean)
        self.bit1List.append(bit1Bean)
        self.bit0List.append(bit0Bean)
        self.currentValueList.append(0)
        self.lastChanged.append(1)
        self.lightList.append(lightBean)
        self.lightNameList.append(lite)
        #print "addLight done for " + lite
        return

# create one of these
print "Creating Stepper Driver"
stepperDriver = StepperDriver()
#print "Adding IL1 with CT3001, CT3002, CT3003, CT3004"
#stepperDriver.addLight("IL1", "CT3001", "CT3002", "CT3003", "CT3004")
#print "Calling start() for Stepper Driver"
#stepperDriver.start()
