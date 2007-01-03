# Sample script showing how to initialize
# turnouts based on the state of their input
# sensors (e.g. feedback)
#
# This is particularly useful for a C/MRI system, where
# the turnouts need to be set to a particular state quickly
#

def initTurnout(turnout):
    to = turnouts.provideTurnout(turnout)
    to.setState(to.getKnownState())
    return

# invoke for all defined turnouts
for x in turnouts.getSystemNameList().toArray() :
  initTurnout(x)

