# An improved version of MqttSignalHead.py by GitHub user ggee.
# https://github.com/JMRI/JMRI/issues/12767
#
# This script does not require any manual configuration
# but works out of the box.

import java
import java.beans
import jmri
import apps

# 0  = Dark
# 1  = Red
# 2  = Flashing Red
# 4  = Yellow
# 8  = Flashing Yellow
# 16 = Green
# 32 = Flashing Green

mqttAdapter = jmri.InstanceManager.getDefault( jmri.jmrix.mqtt.MqttSystemConnectionMemo ).getMqttAdapter()
mqttSignalTopic = mqttAdapter.getOptionState("14")

class SignalHeadChanger( java.beans.PropertyChangeListener ):

  def propertyChange( self, event ):
    if (event.propertyName == "Appearance") :
      signalHead = event.source
      mqttAdapter.publish(mqttSignalTopic + signalHead.getSystemName(), signalHead.getAppearanceName())

listener = SignalHeadChanger( )
for signal in signals.getNamedBeanSet():
  signal.addPropertyChangeListener(listener)
