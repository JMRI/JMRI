# Example of how to install a new parser for the MQTT Turnouts
#
# This is also a sample JSON coder/decoder, just ensure PUBLISHJSON = True
#
# The script before should be run before loading the panels
#   (use RUNAFTERPANELS = False to gain some time)
# or after the panel(s) are loaded and then use RUNAFTERPANELS = True
# 
# Author: Bob Jacobsen, copyright 2019
# Author: Gert Jim Muller, copyright 2019
# Author: Dave McMorran, copyright 2019#

import jmri
import java
import json

PUBLISHJSON = True     # True:publish JSON, False:just the state
RUNAFTERPANELS = True  # False to speed up (ms), but need script before panels
                       # True needed if Tunrouts are already loadedin the table

# First, create the parser class
class ParserReplacement( jmri.jmrix.mqtt.MqttContentParser ):
    # Parse the payload as published by MQTT
    def beanFromPayload( self, bean, payload, topic ):
        # this needs code
        print "from Broker. Bean", bean
        print "with topic", topic

    # Try JSON first
        try:
            json_payload = json.loads( payload )
        except:
            print "and text payload", payload
            payload_state = payload
        else:
            print "and json payload", payload
            try:
                payload_state = json_payload["state"]
            except:
                payload_state = payload
        
        # THROWN/CLOSED is the default behavior, here so you can modify it as desired
        if   ( payload_state == "THROWN" ) or ( payload_state == "1" ) or ( payload_state == 1 ):
            bean.newKnownState( THROWN )
        elif ( payload_state == "CLOSED" ) or ( payload_state == "0" ) or ( payload_state == 0 ):
            bean.newKnownState( CLOSED )
        else:
            bean.newKnownState( INCONSISTENT )
        return

    # Parse a change in the bean
    def payloadFromBean( self, bean, newState ) :
        # sort out states      
        print "from Bean", bean
        print "with state", newState

        # JSON, if set to True
        if PUBLISHJSON:
            data = {}      
            if ( ( newState & jmri.Turnout.CLOSED ) != 0 ^ bean.getInverted( ) ):
                data['state'] = "CLOSED"                
            else:
                data['state'] = "THROWN"                
            json_data = json.dumps( data )
            return json_data

        # Without JSON - this is the same as the default behavior, here so you can modify it as desired
        else:
            if ( ( newState & jmri.Turnout.CLOSED ) != 0 ^ bean.getInverted( ) ):
                return "CLOSED"
            else :
                return "THROWN"

# Find the MqttTurnoutManager
m = jmri.InstanceManager.getNullableDefault( jmri.jmrix.mqtt.MqttTurnoutManager )
print "From InstanceManager as MqttTurnoutManager: ", m
if( m is None ):
    # Might only have one connection, 
    m = jmri.InstanceManager.getDefault( jmri.TurnoutManager ).getManagerList( )
    m = m[1]   # 1 is only appropriate if the MQTT connection is first configured in JMRI
    print "From InstanceManager as TurnoutManager: ", m

# Install the Parser
if( m is not None ):
    p = ParserReplacement( )
    
    # install in Manager for all future-defined turnouts
    m.setParser( p )
    print( "ParserReplacement installed" )

    # install in any turnouts that have already been created
    if RUNAFTERPANELS:
        for turnout in m.getNamedBeanSet( ):
            turnout.setParser( p )
else:
    print( "ParserReplacement not installed" )
