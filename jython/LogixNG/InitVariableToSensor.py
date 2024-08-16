# Sample script that is used to initialize a local variable
# or a global variable in LogixNG.

import jmri
mySensor = sensors.provide("MySensor")
variable.set(mySensor)
