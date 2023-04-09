# Sample script to set all un-THROWN turnouts to THROWN
#
# After this script is turn, all the Turnouts defined in JMRI
# should be in the THROWN state.
#
# Note that some "Turnouts" may actually drive other things on the layout,
# such as signal heads. This script should be run _before_ anything that
# sets those.
#
# Part of the JMRI distribution

import jmri

class SetAllTurnoutsThrown(jmri.jmrit.automat.AbstractAutomaton) :
        # handle() is called just once when it returns false.
        def handle(self):
            # loop thru all defined turnouts
            toCnt = 0
            chgCnt = 0
            for to in turnouts.getNamedBeanSet():
              toCnt += 1
              cs = to.getState()
              if (cs != THROWN) :
                chgCnt += 1
                to.setState(THROWN)
                self.waitMsec(100) # pause for 1/10 second between commands
            print str(toCnt) + " turnouts found, " + str(chgCnt) + " changed to THROWN"
            return False

SetAllTurnoutsThrown().start()



