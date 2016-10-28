# Turns layout power off.
#
# Invoke this from a Logix in response to your desired e-stop command,
# e.g. a panel button that operates an internal sensor
#
# Author: Bob Jacobsen, copyright 2013
# Part of the JMRI distribution

import jmri

powermanager.setPower(jmri.PowerManager.OFF)
