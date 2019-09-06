# Sample script to set all turnouts in Unknown state to Closed
#
# Note that some "Turnouts" may actually drive other things on the layout,
# such as signal heads. This script should be run _before_ anything that
# sets those.  
#
# Part of the JMRI distribution

import jmri

from time import sleep

toCnt = 0
chgCnt = 0

# loop thru all defined turnouts
print "Starting to set turnouts in UNKNOWN state to CLOSED"
for to in turnouts.getNamedBeanSet():
  toCnt += 1
  cs = to.getState()
  if (cs == UNKNOWN) :
    chgCnt += 1
    to.setState(CLOSED)
    sleep(0.1) #pause for 1/10 second between commands
print str(toCnt) + " turnouts checked, " + str(chgCnt) + " found in UNKNOWN state and changed to CLOSED"


