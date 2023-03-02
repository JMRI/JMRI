# This script scans all LocoNet slots and creates report about the slot status, slots used for the consists
# including the details about the consist. It also flags all non-zero speed slots.
# The CSV report with all above information is created in the TEMP directory as well.

# Author: steambigboy, copyright 2022
# Part of the JMRI distribution

import jmri
import java
import java.util
import array
import csv
# change numSlots=number of slots accordingly DCS50=11; DCS51=21, DB150=23, DCS100=121
numSlots = 121
class SampleLnStats(jmri.jmrit.automat.AbstractAutomaton) :
    arr = []
    def init(self):
        myLocoNetConnection = jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0);
        self.slotManager = myLocoNetConnection.getSlotManager()
        nowSlotsUsed = self.slotManager.getInUseCount()
        MySystemPrefix = self.slotManager.getSystemPrefix()
        MyUserName = self.slotManager.getUserName()
        CS = self.slotManager.getCommandStationType()
        print "Command Station:", CS, "User Name:", MyUserName, "Prefix:", MySystemPrefix
        print "Number of slots in use: ", nowSlotsUsed
        for x in range(1, numSlots):
            slrec = []
            self.LocoNetSlot = self.slotManager.slot(x)
            LnSlot = self.LocoNetSlot.locoAddr()
            SlotN = self.slotManager.slot(x)
            LnSpeed = self.LocoNetSlot.speed()
            slrec.append(x)
            slrec.append(LnSlot)
            slrec.append(LnSpeed)
            self.arr.append(slrec)
            if(LnSpeed == 0):
                LnSflag = "OK"
            else:
                LnSflag = "Warning non zero speed"
            LnslotStatus = self.LocoNetSlot.slotStatus()
            if(LnslotStatus == 0):
                LnSStatus = "Free"
            elif(LnslotStatus == 16):
                LnSStatus = "Common"
            elif(LnslotStatus == 32):
                LnSStatus = "Idle"
            else:
                LnSStatus = "In Use"
            LnConsistStatus = self.LocoNetSlot.consistStatus()
            if(LnConsistStatus == 8):
                LnCStat = "TOP"
                LnCS = "check TOP Address of consist"
            elif(LnConsistStatus == 64):
                LnCStat = "Subconsist"
                LnCStatus = "check subconsist of slot #"
                LnCS = LnCStatus + str(LnSpeed)
            elif(LnConsistStatus == 72):
                LnCStat = "Midconsist"
                LnCStatus = "check subconsist of slot #"
                LnCS = LnCStatus + str(LnSpeed)
            else:
                LnCStat = "0"
                LnCS = ""
            LnSpeedStep = self.LocoNetSlot.decoderType()
            if(LnSpeedStep == 0):
                LnSStep = "28_SS"
            elif(LnSpeedStep == 1):
                LnSStep = "28_Trinary"
            elif(LnSpeedStep == 2):
                LnSStep = "14_SS"
            elif(LnSpeedStep == 3):
                LnSStep = "128_SS"
            elif(LnSpeedStep == 4):
                LnSStep = "28_SS_ADV"
            elif(LnSpeedStep == 7):
                LnSStep = "128_SS_ADV"
            slrec.append(LnSStatus)
            slrec.append(LnCStat)
            slrec.append(LnSStep)
            slrec.append(LnSflag)
            slrec.append(LnCS)
            print "Address in slot #", x, "is:", LnSlot, "at speed:", LnSpeed, "with slot status:", LnSStatus, "consist status:", LnCStat, "speed step mode", LnSStep, LnSflag, LnCS
        fields = ['slot', 'address', 'speed', 'status', 'consist', 'speed_steps', 'non zero warning', 'consist details']
        rows = self.arr
# please write your own file location
        filename = "C:\Temp\LocoNet_slot_status.csv"
        with open(filename, 'wb') as csvfile:
# creating a csv writer object
            csvwriter = csv.writer(csvfile)
# writing the fields
            csvwriter.writerow(fields)
# writing the data rows
            csvwriter.writerows(rows)
        return
a = SampleLnStats()
a.start()


