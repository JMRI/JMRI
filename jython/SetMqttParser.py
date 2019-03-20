# Example of how to install a new parser for the MQTT Turnouts
#
# Ideally, this would be a sample JSON coder/decoder
# but right now it's not
#

import jmri;
import java;


# First, create the parser class

class ParserReplacement (jmri.jmrix.mqtt.MqttContentParser) :
    def beanFromPayload(bean, payload, topic) :
        # this needs code
        return
    def payloadFromBean(bean, newState) :
        # sort out states
        if ((newState & jmri.Turnout.CLOSED) != 0 ^ bean.getInverted()) :
            return "Really Thrown";
        else :
            return "Really Closed";

# now find the MqttTurnoutManager and install one

m = jmri.InstanceManager.getNullableDefault(jmri.jmrix.mqtt.MqttTurnoutManager)
if (m is not None) : m.setParser(ParserReplacement())
