# Script to change all turnouts to OneSensor (feedback) if possible
# This is useful switching back from MERG simulator mode (where direct mode has been set)
# when OneSensor Turnout feedback is available
#
# Part of the JMRI distribution

import jmri

toCnt = 0
chgCnt = 0
# loop thru all defined turnouts, setting each to ONESENSOR if allowed
for to in turnouts.getNamedBeanSet():
    toCnt += 1
    fm = to.getFeedbackMode()
    if to.getFirstSensor() != None and \
        to.getSecondSensor() == None:
        to.setFeedbackMode(jmri.Turnout.ONESENSOR)
        chgCnt += 1
print str(toCnt) + " turnouts found, " + str(chgCnt) + " changed to ONESENSOR"
