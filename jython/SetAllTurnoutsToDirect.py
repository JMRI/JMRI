# Script to change all turnouts to Direct (no feedback)
# This is particularly useful in simulator mode, when feedback is not available
#
# Part of the JMRI distribution

import jmri

toCnt = 0
chgCnt = 0
# loop thru all defined turnouts, setting each to DIRECT if not already DIRECT
for to in turnouts.getNamedBeanSet():
    toCnt += 1
    fm = to.getFeedbackMode()
    if (fm != jmri.Turnout.DIRECT) :
        to.setFeedbackMode(jmri.Turnout.DIRECT)
        chgCnt += 1
print str(toCnt) + " turnouts found, " + str(chgCnt) + " changed to DIRECT"

