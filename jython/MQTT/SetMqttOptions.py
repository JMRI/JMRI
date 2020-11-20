# Example of how to set options in an MqttAdapter
#
# Author: Bob Jacobsen, copyright 2020

import jmri
import java
from org.python.core.util import StringUtil

# Find the MqttAdapter
mqttAdapter = jmri.InstanceManager.getDefault( jmri.jmrix.mqtt.MqttSystemConnectionMemo ).getMqttAdapter()

# set the retain option for transmissions (true is the JMRI default value)
mqttAdapter.retain = true

# set the Quality Of Service for transmissions (2 is the JMRI default value, 0-2 valid)
mqttAdapter.qosflag = 2

