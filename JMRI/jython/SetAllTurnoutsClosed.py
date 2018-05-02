# Sample script to set all un-closed turnouts to Closed
#
# Part of the JMRI distribution

import jmri

from time import sleep

toCnt = 0
chgCnt = 0

# loop thru all defined turnouts
for toName in turnouts.getSystemNameList().toArray() :
  toCnt += 1
  to = turnouts.getTurnout(toName)
  cs = to.getState()
  if (cs != CLOSED) :
    chgCnt += 1
    to.setState(CLOSED)
    sleep(0.1) #pause for 1/10 second between commands
print str(toCnt) + " turnouts found, " + str(chgCnt) + " changed to CLOSED"



