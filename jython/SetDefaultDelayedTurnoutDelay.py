# Change the delay for DELAYED Turnout feedback.
#
# Note the delay is for _all_ turnouts; there's no 
# turnout-by-turnout operation currently.
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution

import jmri

# time is in milliseconds
jmri.implementation.AbstractTurnout.DELAYED_FEEDBACK_INTERVAL = 10000


