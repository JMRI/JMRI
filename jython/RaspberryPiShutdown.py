# Shutdown a Raspberry Pi or other Linux machine cleanly
#
# Run this from a Logix or button to safely restart the machine
#
# From a sequence by Dave Sand

import java
import jmri

# start the machine going down in 1 minute (requires that you can "sudo" without a password)
java.lang.Runtime.getRuntime().exec("sudo shutdown -h +1")

# do an immediate graceful shutdown of JMRI
shutdown.shutdown()