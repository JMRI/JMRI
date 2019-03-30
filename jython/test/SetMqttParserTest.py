# Test the SetMqttParser.py script
import jmri

m = jmri.jmrix.mqtt.MqttTurnoutManager(None, "M");
jmri.InstanceManager.setDefault(jmri.jmrix.mqtt.MqttTurnoutManager, m)

execfile("jython/SetMqttParser.py")

