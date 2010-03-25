# Adapter to xAP automation protocol
#
# Uses xAPlib to listen to the network, creating and 
# maintaining internal Turnout and Sensor objects that 
# reflect what is seen.
#
# The Turnouts' commanded state is updated, not the 
# known state, so feedback needs to be considered in
# any more permanent implementation.  Note that 
# this does not yet send anything on modification,
# due to race conditions.
#
# Author: Bob Jacobsen, copyright 2010
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.3 $

import jarray
import jmri
import xAPlib

# create the network
print "opening "
myNetwork = xAPlib.xAPNetwork("listener.xap")

# display some info
properties = myNetwork.getProperties()
print "getBroadcastIP()", properties.getBroadcastIP() 
print "getHeartbeatInterval()", properties.getHeartbeatInterval()
print "getInstance() ", properties.getInstance() 
print "getPort() ", properties.getPort() 
print "getSource() ", properties.getSource() 
print "getUID() ", properties.getUID() 
print "getVendor() ", properties.getVendor() 
print "getxAPAddress() ", properties.getxAPAddress() 
print

# Define thexAPRxEventListener: Print some
# information when event arrives
class InputListener(xAPlib.xAPRxEventListener):
  def myEventOccurred(self, event, message):
    print "==== rcvd ===="
    print message
    print "--------------"
    # try parsing and printing
    fmtMsg = xAPlib.xAPParser(message)
    print "source: ", fmtMsg.getSource()
    print "target: ", fmtMsg.getTarget()
    print "class:  ", fmtMsg.getClassName()
    print "uid:    ", fmtMsg.getUID()
    if (fmtMsg.getClassName() == "xAPBSC.info" or fmtMsg.getClassName() == "xAPBSC.event") :
        print "    --- Acting on "+fmtMsg.getClassName()+" ---"
        if (fmtMsg.getNameValuePair("output.state","State") != None) :
            print "        --- Acting on output.state ---"
            self.processTurnout(fmtMsg, message)
        if (fmtMsg.getNameValuePair("input.state","State") != None) :
            print "        --- Acting on input.state ---"
            self.processSensor(fmtMsg, message)
    print "=============="
    return

  def processTurnout(self, fmtMsg, message) :
    pair = fmtMsg.getNameValuePair("output.state","Name")
    if (pair == None) :
        print "No Name"
        name = None
    else :
        name = pair.getValue()
        print "        Name:", name
    pair = fmtMsg.getNameValuePair("output.state","Location")
    if (pair == None) :
        print "No Location"
        location = None
    else :
        location = pair.getValue()
        print "        Location: ", location
    pair = fmtMsg.getNameValuePair("output.state","State")
    if (pair == None) :
        print "No State, ending"
        return
    state = pair.getValue().upper()
    print "        State: ", state
    # now create a Turnout and set
    value = CLOSED
    if (state == "ON") :
        value = THROWN
    turnout = turnouts.getTurnout("IT:xAP:xAPBSC:"+fmtMsg.getSource())
    if (turnout == None) :
        print "    create turnout IT:xAP:xAPBSC:"+fmtMsg.getSource()
        turnout = turnouts.provideTurnout("IT:xAP:xAPBSC:"+fmtMsg.getSource())
        if (name != None) :
            turnout.setUserName(name)
    turnout.setCommandedState(value)
    print "    set turnout IT:xAP:xAPBSC:"+fmtMsg.getSource()+" to", value
    return
    
  def processSensor(self, fmtMsg, message) :
    pair = fmtMsg.getNameValuePair("input.state","Name")
    if (pair == None) :
        print "no Name"
        return
    name = pair.getValue()
    print "        Name:", name
    pair = fmtMsg.getNameValuePair("input.state","Location")
    if (pair == None) :
        print "no Location"
        return
    location = pair.getValue()
    print "        Location: ", location
    pair = fmtMsg.getNameValuePair("input.state","State")
    if (pair == None) :
        print "no State"
        return
    state = pair.getValue().upper()
    print "        State: ", state
    # now create a Turnout and set
    value = INACTIVE
    if (state == "ON") :
        value = ACTIVE
    sensor = sensors.getSensor("IS:xAP:xAPBSC:"+fmtMsg.getSource())
    if (sensor == None) :
        print "    create sensor IS:xAP:xAPBSC:"+fmtMsg.getSource()
        sensor = sensors.provideSensor("IS:xAP:xAPBSC:"+fmtMsg.getSource())
        sensor.setUserName(name)
    sensor.setState(value)
    print "    set sensor IS:xAP:xAPBSC:"+fmtMsg.getSource()+" to ", value
    
    return
    
# register xAPRxEvents listener
print "register"
myNetwork.addMyEventListener(InputListener())

print "End of Script"
