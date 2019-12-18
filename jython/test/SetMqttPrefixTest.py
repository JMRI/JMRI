# Test the SetMqttPrefix.py script
import jmri

m = jmri.jmrix.mqtt.MqttTurnoutManager(jmri.jmrix.mqtt.MqttSystemConnectionMemo())
jmri.InstanceManager.setDefault(jmri.jmrix.mqtt.MqttTurnoutManager, m)

execfile("jython/SetMqttPrefix.py")

