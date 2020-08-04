# Example of how to define how to send an arbitrary Mqtt message
#
# To see the result e.g. (your broker and channel may vary)
#     mosquitto_sub -v -h 'test.mosquitto.org' -t '/trains/#'
#
# Author: Bob Jacobsen, copyright 2020

import jmri
import java
import jarray
from org.python.core.util import StringUtil

# Find the MqttAdapter
mqqtAdapter = jmri.InstanceManager.getDefault( jmri.jmrix.mqtt.MqttSystemConnectionMemo ).getMqttAdapter()


# create some content
topic = "jmri/test/topic"  # note this will be prefixed by configured channel, i.e. "/trains/"
payload = "message content"

# send
mqqtAdapter.publish(topic, payload)
