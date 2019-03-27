# Example of how to install a new parser for the MQTT Turnouts
#
# Ideally, this would be a sample JSON coder/decoder
# but right now it's not, but it is getting there...
# 
# Author: Bob Jacobsen, copyright 2019
# Author: Gert Jim Muller copyright 2019

import jmri;
import java;

# First, create the parser class
class ParserReplacement( jmri.jmrix.mqtt.MqttContentParser ):
    def beanFromPayload( self, bean, payload, topic ):
        # this needs code
        print "from Broker. Bean", bean
        print "with payload", payload
        print "and topic", topic
        if ( payload == "THROWN" ):
            bean.newKnownState( THROWN )
        elif ( payload == "CLOSED" ):
            bean.newKnownState( CLOSED )
        else: bean.newKnownState( INCONSISTENT )
        return

    def payloadFromBean( self, bean, newState ) :
        # sort out states      
        print "from Bean", bean
        print "with state", newState
        if ( ( newState & jmri.Turnout.CLOSED ) != 0 ^ bean.getInverted( ) ):
            return "CLOSED";
        else :
            return "THROWN";

# now find the MqttTurnoutManager
m = jmri.InstanceManager.getNullableDefault( jmri.jmrix.mqtt.MqttTurnoutManager )
print "From InstanceManager as MqttTurnoutManager: ", m
if( m is None ):
    m = jmri.InstanceManager.getDefault( jmri.TurnoutManager ).getManagerList( )
    m = m[1]  # 1 is only correct here if the MQTT connection is the first one
    print "From InstanceManager as TurnoutManager: ", m

# and install this parser
if( m is not None ):
    p = ParserReplacement( )
    
    # first install on TurnoutManager for all future-created Turnouts
    m.setParser( p )
    print( "ParserReplacement installed" )
    
    # next install on all existing Turnouts
    for turnout in m.getNamedBeanSet( ):
        turnout.setParser( p )
else:
    print( "ParserReplacement not installed" )
