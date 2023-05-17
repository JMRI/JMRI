# Staring with JMRI 5.3.7, the default behavior or CBus Reporters was
# changed to _not_ clear themselves after a short delay.

# You can restore the prior behavior by running this script.
# CBus Reporters will then clear themselves after a short delay.

import jmri

jmri.jmrix.can.cbus.CbusReporter.eraseOnTimeoutAll = True

# You can also turn on the old behavior for just one reporter with e.g.
#
# reporters.getReporter("NameHere").eraseOnTimeoutThisReporter = True
#


