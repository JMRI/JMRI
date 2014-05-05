# Script to change all turnouts to Direct (no feedback)
# This is particularly useful in simulator mode, when feedback is not available
#
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 17977 $

def setTurnoutToDirect(turnout):
    to = turnouts.getTurnout(turnout)
    fm = to.getFeedbackMode()
    if (fm != jmri.Turnout.DIRECT) :
        to.setFeedbackMode(jmri.Turnout.DIRECT)
    return

# invoke for all defined turnouts
for x in turnouts.getSystemNameList().toArray() :
  setTurnoutToDirect(x)

