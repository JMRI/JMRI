# Example of how to define how to receive MQTT messages
#
# To see the result run the ReceiveMqttMessage.py script 
# or use e.g. (your broker and channel may vary)
#     mosquitto_sub -v -h 'test.mosquitto.org' -t '/trains/#'
#
# Author: Bob Jacobsen, copyright 2020

import jmri
import java

# Find the MqttAdapter
mqttAdapter = jmri.InstanceManager.getDefault( jmri.jmrix.mqtt.MqttSystemConnectionMemo ).getMqttAdapter()

# listener class
class myMqttListener(jmri.jmrix.mqtt.MqttEventListener) :
    def notifyMqttMessage(self, topic, message) :
        # all this listener does is print
        print("Received \""+topic+"\" \""+message+"\"")
    
# register a topic
topic = "jmri/test/topic"  # note this will be prefixed by configured channel, i.e. "/trains/"
mqttAdapter.subscribe(topic, myMqttListener())

