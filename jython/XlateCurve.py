# This script watches one value and drives another
# via a translation curve. The simplest curve requires
# two points.
#
# Author: Ken Cameron, copyright 2011
# Part of the JMRI distribution
#
# A PropertyChangeListener is used to get value changes.
#

import java
import java.beans
import jmri

class XlateCurve (java.beans.PropertyChangeListener) :
    # initialize variables
    isDebug = False
    isEnabled = False
    inPoints = []
    outPoints = []
    inObjectName = ""
    outObjectName = ""
    inObject = None
    outObject = None
    inIsLight = False
    inIsMemory = False
    outIsLight = False
    outIsMemory = False

    def init(self, watchObject, driveObject) :
        testObject = lights.getLight(watchObject)
        if ((self.inObject == None) and (testObject <> None)) :
            self.inObject = testObject
            self.inObjectName = watchObject
            self.inIsLight = True
        testObject = memories.getMemory(watchObject)
        if ((self.inObject == None) and (testObject <> None)) :
            self.inObject = testObject
            self.inObjectName = watchObject
            self.inIsMemory = True
        testObject = lights.getLight(driveObject)
        if ((self.outObject == None) and (testObject <> None)) :
            self.outObject = testObject
            self.outObjectName = driveObject
            self.outIsLight = True
        testObject = memories.getMemory(driveObject)
        if ((self.outObject == None) and (testObject <> None)) :
            self.outObject = testObject
            self.outObjectName = driveObject
            self.outIsMemory = True
        if ((self.inObject == None) or (self.outObject == None)) :
            return(0)
        self.inObject.addPropertyChangeListener(self)
        #self.setName("XlateCurve(" + self.inObjectName + ", " + self.outObjectName + ")")
        return(0)
        
    def enable(self, state) :
        if ((len(self.inPoints) >= 2) and (self.inObject <> None) and (self.outObject <> None)) :
            if (state == True) :
                self.isEnabled = True
            else :
                self.isEnabled = False
        return
        
    def debug(self, state) :
        if (state == True) :
            self.isDebug = True
        else :
            self.isDebug = False
        return
        
    def setCurvePoint(self, v1, v2) :
        ok1 = 0
        ok2 = 0
        try :
            ok1 = v1 * 1.0
        except ValueError :
            print "setCurvePoint - Invalid value for v1: " + v1
            return
        try :
            ok2 = v2 * 1.0
        except ValueError :
            print "setCurvePoint - Invalid value for v2: " + v2
            return
        newPt = 0
        while (newPt < len(self.inPoints)) :
            if (self.isDebug) :
                print "adding point: " + newPt.toString() + " ok1: " + ok1.toString() + " testing: " + self.inPoints[newPt].toString()
            if (ok1 < self.inPoints[newPt]) :
                self.inPoints.insert(newPt, ok1)
                self.outPoints.insert(newPt, ok2)
                return
            elif (ok1 == self.inPoints[newPt]) :
                self.outPoints[newPt] = ok2
                return
            newPt = newPt + 1
        self.inPoints.append(ok1)
        self.outPoints.append(ok2)
        return

    def dumpTable(self) :
        pt = 0
        while (pt < len(self.inPoints)) :
            print pt.toString() + " " + self.inPoints[pt].toString() + " " + self.outPoints[pt].toString()
            pt = pt + 1
        return

    def propertyChange(self, event) :
        # I don't bother with the event, I just check everything again
        if (self.isEnabled) :
            if (self.inIsLight) :
                v = self.inObject.getTargetIntensity()
            if (self.inIsMemory) :
                v = float(self.inObject.getValue())
            if (v < self.inPoints[0]) :
                v = self.inPoints[0]
            if (v > self.inPoints[len(self.inPoints) - 1]) :
                v = self.inPoints[len(self.inPoints) - 1]
            testPt = 1
            while (testPt < len(self.inPoints)) :
                if (self.isDebug) :
                    print "v: " + v.toString() + " [" + testPt.toString() + "] " + self.inPoints[testPt].toString()
                if (v <= self.inPoints[testPt]) :
                    break
                testPt = testPt + 1
            if (testPt >= len(self.inPoints)) :
                testPt = len(self.inPoints) - 1
            if (self.isDebug) :
                print "v: " + v.toString() + " testPt: " + testPt.toString()
            inRange = self.inPoints[testPt] - self.inPoints[testPt - 1]
            outRange = self.outPoints[testPt] - self.outPoints[testPt - 1]
            diff = v - self.inPoints[testPt - 1]
            factor = diff / inRange
            outV = self.outPoints[testPt - 1] + (outRange * factor)
            if (self.isDebug) :
                print "inRange: " + inRange.toString() + " outRange: " + outRange.toString()
                print "diff: " + diff.toString() + " factor: " + factor.toString()
            if (self.outIsLight) :
                self.outObject.setTargetIntensity(outV)
            if (self.outIsMemory) :
                self.outObject.setValue(outV)
            if (self.isDebug) :
                print "setting " + self.outObjectName + ": " + outV.toString()
        return
        
# invoke this script from the system directory
# then create a file (with your panels) that has
# lines like these (without the ## comments)
# Params:
#    value being watched for changes
#    value to set for changes in the watched value
#
# Sensor names are system names
# time values are floating point seconds

##a = XlateCurve() # create one of these 
##a.init('ILA1', 'PLA1') # invoke this for the object pair
##a.setCurvePoint(0, 0.3)   # set base starting point
##a.setCurvePoint(1, 0.9)   # set next point
##a.enable(True)    # enable it
##a.debug(True)
