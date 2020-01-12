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
# Ver 1.2 01/11/2011 NW Changes to the input code
# Ver 1.3 07/11/2011 NW Added a "Type" to the BSC message format
# Ver 1.4 07/12/2011 NW Changes to xAP Tx Message area
#
#
#
#
# The next line is maintained by CVS, please don't change it

import jarray
import jmri
import xAPlib
import java
import java.beans

# create the network
print "opening "
global myNetwork
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

        if (fmtMsg.getNameValuePair("output.state","Type") != None) :
            print "        --- Acting on output.state ---"
            pair = fmtMsg.getNameValuePair("output.state","Type")
            if (pair == None) :
              print "No Type, ending"
              return
            type = pair.getValue().upper()
            print "NWE Type:", type,":"
            if (type == "TURNOUT" or type == "SIGNAL") :
                print "NWE Turnout/Signal"
                self.processTurnout(fmtMsg, message)

        if (fmtMsg.getNameValuePair("input.state","Type") != None) :
            pair = fmtMsg.getNameValuePair("input.state","Type")
            type = pair.getValue().upper()
            if (type == "SENSOR") :
                print "NWE Sensor"
                print "        --- Acting on input.state ---"
                self.processSensor(fmtMsg, message)
    print "=============="
    return


# Process Turnout
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
    turnout = turnouts.getTurnout("IT:XAP:XAPBSC:"+fmtMsg.getSource())
    if (turnout == None) :
        print "    create x turnout IT:XAP:XAPBSC:"+fmtMsg.getSource()
        turnout = turnouts.provideTurnout("IT:XAP:XAPBSC:"+fmtMsg.getSource())
        if (name != None) :
            turnout.setUserName(name)
    turnout.setCommandedState(value)
    print "    set turnout IT:XAP:XAPBSC:"+fmtMsg.getSource()+" to", value
    return

# Process Sensor
  def processSensor(self, fmtMsg, message) :
    pair = fmtMsg.getNameValuePair("input.state","Name")
    if (pair == None) :
        print "No Name"
        name = None
    else :
        name = pair.getValue()
        print "        Name:", name
    pair = fmtMsg.getNameValuePair("input.state","Location")
    if (pair == None) :
        print "No Location"
        location = None
    else :
        location = pair.getValue()
        print "        Location: ", location
    pair = fmtMsg.getNameValuePair("input.state","State")
    if (pair == None) :
        print "No State, ending"
        return
    state = pair.getValue().upper()
    print "        State: ", state
    # now create a Sensor and set
    value = INACTIVE
    if (state == "ON") :
        value = ACTIVE
    sensor = sensors.getSensor("IS:XAP:XAPBSC:"+fmtMsg.getSource())
    if (sensor == None) :
        print "    create x sensor IS:XAP:XAPBSC:"+fmtMsg.getSource()
        sensor = sensors.provideSensor("IS:XAP:XAPBSC:"+fmtMsg.getSource())
        if (name != None) :
            sensor.setUserName(name)
    sensor.setState(value)
    print "    set sensor IS:XAP:XAPBSC:"+fmtMsg.getSource()+" to ", value
    return


# Define the turnout listener class, which drives output messages
class TurnoutListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    global myNetwork
    print " ************** Sending xAP Message **************"
    print "change",event.propertyName
    print "from", event.oldValue, "to", event.newValue
    print "source systemName", event.source.systemName
    print "source userName", event.source.userName
    # format and send the message
    # the final message will look like this on the wire:
    #
    #                       xap-header
    #                       {
    #                       v=12
    #                       hop=1
    #                       uid=FFFF0000
    #                       class=xAPBSC.cmd
    #                       source=JMRI.DecoderPro.1
    #                       destination=NWE.EVA485.DEFAULT:08
    #                       }
    #                       output.state.1
    #                       {
    #                       ID=08
    #                       State=ON
    #                       }
    #                                                        *
    myProperties = myNetwork.getProperties()
    myMessage = xAPlib.xAPMessage("xAPBSC.cmd", myProperties.getxAPAddress())
    myMessage.setUID(self.uid)
    myMessage.setTarget(self.target)
    if (event.newValue == CLOSED) :
        myMessage.addNameValuePair( "output.state.1", "ID", self.id)
        myMessage.addNameValuePair( "output.state.1", "State", "OFF")
        myMessage.addNameValuePair( "output.state.1", "Text", "CLOSED")       # Optional
    else :
        myMessage.addNameValuePair( "output.state.1", "ID", self.id)
        myMessage.addNameValuePair( "output.state.1", "State", "ON")
        myMessage.addNameValuePair( "output.state.1", "Text", "THROWN")       # Optional
    myNetwork.sendMessage(myMessage)
    print myMessage.toString()
    return


def defineTurnout(name, uid, id, target) :
    t = turnouts.provideTurnout(name)
    m = TurnoutListener()
    m.uid = uid
    m.id = id
    m.target = target
    t.addPropertyChangeListener(m)
    return

# register xAPRxEvents listener
print "register"
myNetwork.addMyEventListener(InputListener())

# define the turnouts
defineTurnout("IT:XAP:XAPBSC:NWE.EVA485.DEFAULT:99", "FF010100", "99", "NWE.EVA485.DEFAULT")

print "End of Script"
