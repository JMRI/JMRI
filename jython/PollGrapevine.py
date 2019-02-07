# Poll all grapevine nodes, looking for which are present.
#
# If a grapevine node is present, it _removes_ messages addressed
# to it.  We'll report as possibly present all nodes for whom messages
# are absorbed.  Of course, a disconnected grapevine will behave the 
# same way, but it will have lots of nodes reporting present...
#
# Author: Bob Jacobsen, copyright 2008
# JMRI
import jmri
import jmri.jmrix
import jmri.jmrix.grapevine

# Define Listener to make a note if an answer is received
class NodeListener(jmri.jmrix.grapevine.SerialListener):
  def message(self, m):
    return
  def reply(self, r):
    global result
    result = False
    return
 
jmri.InstanceManager.getDefault(jmri.jmrix.grapevine.GrapevineSystemConnectionMemo).getTrafficController().addSerialListener(NodeListener())

# now an Automat to loop over all possible nodes and report those that
# don't reply after a short wait

class TestLooper(jmri.jmrit.automat.AbstractAutomaton) :
    def init(self) :
        return
    def handle(self) :
        global result
        self.message = jmri.jmrix.grapevine.SerialMessage()
        self.message.setElement(1,119)
        self.message.setElement(3,119)
        for i in range(1,127+1) :
            result = True
            self.message.setElement(0,i+128)
            self.message.setElement(2,i+128)
            jmri.InstanceManager.getDefault(jmri.jmrix.grapevine.GrapevineSystemConnectionMemo).getTrafficController().sendSerialMessage(self.message, None)
            self.waitMsec(200)
            if (result) :
                print "node",i,"may be present"
        return False

TestLooper().start()

