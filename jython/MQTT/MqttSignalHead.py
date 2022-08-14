#
# Listens to existing SignalHeads and sends their state via MQTT
#
# Typically used with VirtualSignalHeads, but will work with any.
#
# The topic sent is given by the `topicPrefix` variable set by the user
# followed by the SignalHeads system name.
#
# The data content is the new appearance as a string:  GREEN, FLASHRED, etc
#
# See example at bottom for how to use.
#

import jmri
import java

class MqttSignalHeadMapper (java.beans.PropertyChangeListener):
    def start(self) :
        # create MQTT Link
        self.mqtt = jmri.InstanceManager.getDefault( jmri.jmrix.mqtt.MqttSystemConnectionMemo ).getMqttAdapter()
        # set up listeners
        for name in self.heads :
            print("listening to", name)
            head = signals.getSignalHead(name)
            head.addPropertyChangeListener(self)
        return

    def propertyChange(self, event):
        if (event.propertyName == "Appearance") :
            object = event.source
            data = object.getAppearanceName()
            topic = self.topicPrefix+object.systemName
            print("Sending", topic, data)
            self.mqtt.publish(topic, data)
        return

a = MqttSignalHeadMapper()
a.topicPrefix = "mylayout/SignalHeads/"
a.heads = []
a.heads.append("IH1")
a.heads.append("IH2")
a.start()

