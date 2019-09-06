# Sample script to set all turnouts in Unknown state to Closed
#
# After this script is turn, there should be no Turnouts in the
# UNKNOWN state; any that were at the start should have been set to CLOSED.
#
# By skipping over Turnouts that have already been set, this minimizes
# the chance of disturbing e.g. Turnouts driving signals that have already been set 
# by signal logic.
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


