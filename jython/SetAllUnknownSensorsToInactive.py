# Script to set all sensors with state UNKNOWN to INACTIVE
# This is particularly useful in simulator mode, when hardware sensors aren't updated
#
# Part of the JMRI distribution

import jmri

# loop thru defined sensors, if unknown, set to inactive
list = sensors.getSystemNameList()
for i in range(list.size()) :
    s = sensors.getSensor(list.get(i))
    cs = s.getKnownState()
    if cs == UNKNOWN :
      s.setKnownState(INACTIVE)
      print s.systemName + " set to INACTIVE"

