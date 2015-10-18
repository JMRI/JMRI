# Sample script to set all defined turnouts to Closed
#
# Part of the JMRI distribution

import jmri

from time import sleep

def closeTurnout(toName):
    to = turnouts.getTurnout(toName)
    print "setting " + toName + " to CLOSED"
    to.setState(CLOSED)
    return

# invoke for all defined turnouts
for tn in turnouts.getSystemNameList().toArray() :
  closeTurnout(tn)
  sleep(0.1) #pause for 1/10 second between commands


