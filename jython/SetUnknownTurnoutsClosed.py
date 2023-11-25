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

# This script kicks off an independent series of actions that will take some time to 
# complete once the script itself completes.  If you are running this script as a 
# startup action, you might need to include a pause startup action before you run any
# additional scripts later.

import jmri

class SetUnknownTurnoutsClosed(jmri.jmrit.automat.AbstractAutomaton) :
        # handle() is called just once when it returns false.
        def handle(self):
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
                self.waitMsec(100) # pause for 1/10 second between commands
            print str(toCnt) + " turnouts checked, " + str(chgCnt) + " found in UNKNOWN state and changed to CLOSED"
            return False

SetUnknownTurnoutsClosed().start()
