# Script to set all sensors with state UNKNOWN to INACTIVE
# This is particularly useful in simulator mode, when hardware sensors aren't updated
#
# Part of the JMRI distribution

import jmri

sCnt = 0
chgCnt = 0

# loop thru defined sensors, if UNKNOWN, set to INACTIVE
list = sensors.getNamedBeanSet()
for s in list :
    sCnt += 1
    cs = s.getKnownState()
    if cs == UNKNOWN :
      chgCnt += 1
      s.setKnownState(INACTIVE)
print str(sCnt) + " sensors found, " + str(chgCnt) + " changed to INACTIVE"

