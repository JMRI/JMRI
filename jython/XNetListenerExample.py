# This is an example showing how to create an XPressNet Listener
# in a script.
#
# Author: Paul Bender, Copyright 2009
#

import java
import jmri

# first, define the listener.  This one just prints some information
# when a message is received.
class xnetListener(jmri.jmrix.lenz.XNetListener):
    
        # an XNetListener must define the message
        # function, which takes a message or a reply as a parameter
    def message(self,m):
        if m.getClass()==java.lang.Class.forName("jmri.jmrix.lenz.XNetMessage"):
            # This is an outgoing message (to the command station)
            print "sending ",m.toString()
        else:
            # the type must be jmri.jmrix.lenz.XNetReply
            # so this is an incoming message (from the command station)
            print "received ",m.toString()
            # The jmri.jmrix.lenz.XNetReply class includes
            # functions to classify messages.  Here the 
            # "isOKMessage" function is used to see if a 
            # reply is the message 0x01 0x04 0x05
            if m.isOkMessage():
                print "message is OK Message"
    
    # an XNetListener must define the notifyTimeout
    # function, which takes a  message as a parameter
    def notifyTimeout(self,m):
        print "Timeout on Message ",m.toString()
    
#end of class definition

# we need to create a listener and register it.
xl=xnetListener()
# this requires gaining access to the traffic controller
tc = jmri.InstanceManager.getDefault(jmri.jmrix.lenz.XNetSystemConnectionMemo).getTrafficController()
# and registering as a listener.  The first parameter to this
# routine is a mask.  The mask used here returns all messages
# received to/from the command station.
tc.addXNetListener(jmri.jmrix.lenz.XNetInterface.ALL,xl)
