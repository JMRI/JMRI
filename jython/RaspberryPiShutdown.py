# Shut down an Raspberry Pi or other Linux machine cleanly
#
# Run this from e.g. a Logix to end JMRI and make it save to turn off the machine
#
# From a sequence by Dave Sand


import java
import jmri
import jmri.InstanceManager

# start the machine going down in one minute (requires that you can "sudo" without a password)
java.lang.Runtime.getRuntime().exec("sudo shutdown -h +1")

# do an immediate graceful shutdown of JMRI
jmri.InstanceManager.shutDownManagerInstance().shutdown()
