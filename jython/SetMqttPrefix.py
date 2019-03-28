# Example of how to define a new prefix for the MQTT Turnouts
#
# The script before should be run before loading the panels.
# It only sets the prefix for Turnouts created _after_ this is run.
# 
# Author: Bob Jacobsen, copyright 2019

import jmri
import java

# Find the MqttTurnoutManager
m = jmri.InstanceManager.getNullableDefault( jmri.jmrix.mqtt.MqttTurnoutManager )
print "From InstanceManager as MqttTurnoutManager: ", m
if( m is None ):
    # Might only have one connection, 
    m = jmri.InstanceManager.getDefault( jmri.TurnoutManager ).getManagerList( )
    m = m[1]   # 1 is only appropriate if the MQTT connection is first configured in JMRI
    print "From InstanceManager as TurnoutManager: ", m

# Now set a sample prefix
if( m is not None ):
    m.setPrefix(" /foo/bar/jmri-sample-prefix/")  # can be anything but probably should start and end with "/"
    print( "MQTT prefix updated" )
else:
    print( "MQTT prefix not updated" )
