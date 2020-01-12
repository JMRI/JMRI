# Sample script showing how to initialize
# turnouts based on the state of their input
# sensors (e.g. feedback)
#
# This is particularly useful for a C/MRI system, where
# the turnouts need to be set to a particular state quickly
#
#
# Part of the JMRI distribution

import jmri

def initTurnout(to):
    to.setState(to.getKnownState())
    return

# invoke for all defined turnouts
for to in turnouts.getNamedBeanSet() :
  initTurnout(to)

