# This script will monitor a list of sensors and record on/off timestamps.
# The file and sensors are passed from the caller, so this can be left as a
# static program and users should just copy the bottom part to pass the parameters.
#
# Author: Ken Cameron, copyright 2009
# Part of the JMRI distribution

import jmri

import java
import java.beans
import java.util
import jarray
import java.util.Calendar

# setup listeners for a list of sensors, record each change to a logfile
class LogSensors(java.beans.PropertyChangeListener) :
    # initialize variables
    sensorList = []
    logFileName = None

    def setFileName(self, fn) :
        print "log file: " + fn
        self.logFileName = fn
        return

    def addSensor(self, sName) :
        s = sensors.getSensor(sName)
        if (s == None) :
            print "Invalid sensor name: " + sName + "\n"
        else :
            self.sensorList.append(sName)
            s.addPropertyChangeListener(self)
            print("added " + sName + " to list.\n")
        return

    # it doesn't matter which changed, we record the state of each
    def propertyChange(self, event) :
        time = java.util.Calendar.getInstance()
        txt = '"' + self.formatDate(time) + '","' + self.formatTime(time) + '"'
        for s in self.sensorList :
            sPtr = sensors.getSensor(s)
            if sPtr == None :
                txt = txt + ',"Invalid"'
            else :
                txt = txt + ',"' + self.getKnownStateText(sPtr) + '"'
        f = open(self.logFileName, 'a')
        wasPt = f.tell()
        if (wasPt == 0) :
            hdr = '"Date","Time"'
            for i in self.sensorList :
                hdr = hdr + ',"' + i + '"'
            f.write(hdr + '\n')
        f.write(txt + "\n")
        isPt = f.tell()
        f.close()
        return

    # method for creating a TimeStamp.
    def formatTime(self, ts):
        ptHrs = str(ts.get(java.util.Calendar.HOUR_OF_DAY))
        if len(ptHrs) == 1 :
            ptHrs = "0" + ptHrs

        ptMins = str(ts.get(java.util.Calendar.MINUTE))
        if len(ptMins) == 1 :
            ptMins = "0" + ptMins

        ptSecs = str(ts.get(java.util.Calendar.SECOND))
        if len(ptSecs) == 1 :
            ptSecs = "0" + ptSecs
        ptMs = str(ts.get(java.util.Calendar.MILLISECOND))
        pTime = ptHrs + ":" + ptMins + ":" + ptSecs + "." + ptMs
        return pTime

    # method for creating a DateStamp.
    def formatDate(self, ts):
        ptDays = str(ts.get(java.util.Calendar.DATE))
        if len(ptDays) == 1 :
            ptDays = "0" + ptDays

        ptMonths = str(ts.get(java.util.Calendar.MONTH) + 1)
        if len(ptMonths) == 1 :
            ptMonths = "0" + ptMonths

        ptYears = str(ts.get(java.util.Calendar.YEAR))
        pDate = ptMonths + "/" + ptDays + "/" + ptYears
        return pDate

    # convert KnownState to a text
    def getKnownStateText(self, ptr) :
        state = ptr.getKnownState()
        x = '??'
        if (state == ACTIVE) :
            x = 'ACTIVE'
        elif (state == INACTIVE) :
            x = 'INACTIVE'
        else :
            x = 'UNKNOWN'
        return x

#
# USAGE:
#   1. Add this script to your system startup.
#   2. Copy the below lines into a new script in your local resources space,
#       like the same directory as your Config xml files.
#   3. remove the double comments ## and edit to your own needs
#
# create one of these
##a = LogSensors()

# and start it running
##a.setFileName("/c:/Temp/CnyModSensorData.csv")
##a.addSensor("LS281")
##a.addSensor("LS282")
##a.addSensor("LS283")
##a.addSensor("LS284")
