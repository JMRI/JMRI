# This runs a sequence of LocoNet Transponding messages and records what happens to
# the associated reporter objects

import java
import jmri

connectionIndex = 0
shortDelay = 1500
longDelay = 3000

lr1002 = reporters.provide("LR1002")
lr216 = reporters.provide("LR216")
lr218 = reporters.provide("LR218")

class ReporterListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    print "Reporter",event.source, "property", event.propertyName, "changed, was:", event.oldValue, "now:", event.newValue
    if (event.source.getCurrentReport() == None) : show = "None"
    else: show = event.source.getCurrentReport().toReportString()
    print "             current toReportString ", show
    if (event.source.getLastReport() == None) : show = "None"
    else: show = event.source.getLastReport().toReportString()
    print "                last toReportString ", show
    print "tag LD2200 last seen", ld2200.getWhereLastSeen()

lr1002.addPropertyChangeListener(ReporterListener())
lr216.addPropertyChangeListener(ReporterListener())
lr218.addPropertyChangeListener(ReporterListener())


# tags have already been created and are in the tags XML file, so don't create here
idTagManager = jmri.InstanceManager.getDefault(jmri.IdTagManager)
ld2200 = idTagManager.provide("LD2200")

class TagListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    print "IdTag",event.source, "property", event.propertyName, "changed, was:", event.oldValue, "now:", event.newValue

ld2200.addPropertyChangeListener(TagListener())

class RunTest(jmri.jmrit.automat.AbstractAutomaton) :
    def init(self) :
        l = jmri.jmrix.loconet.LocoNetMessage([0xD0, 0x27, 0x69, 0x11, 0x18, 0x68])
        print "\nSending ",l, "Transponder address 2200 present at LR1002"
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)
        self.waitMsec(shortDelay)
        l = jmri.jmrix.loconet.LocoNetMessage([0xD0, 0x01, 0x59, 0x11, 0x18, 0x68])
        print "\nSending ",l, "Transponder address 2200 absent at LR218"
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)

        self.waitMsec(longDelay)
        print "\n"

        l = jmri.jmrix.loconet.LocoNetMessage([0xD0, 0x21, 0x57, 0x11, 0x18, 0x68])
        print "\nSending ",l, "Transponder address 2200 present at LR216"
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)
        self.waitMsec(shortDelay)
        l = jmri.jmrix.loconet.LocoNetMessage([0xD0, 0x07, 0x69, 0x11, 0x18, 0x68])
        print "\nSending ",l, "Transponder address 2200 absent at LR1002"
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)

        self.waitMsec(longDelay)
        print "\n"

        l = jmri.jmrix.loconet.LocoNetMessage([0xD0, 0x27, 0x69, 0x11, 0x18, 0x68])
        print "\nSending ",l, "Transponder address 2200 present at LR1002"
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)
        self.waitMsec(shortDelay)
        l = jmri.jmrix.loconet.LocoNetMessage([0xD0, 0x01, 0x57, 0x11, 0x18, 0x68])
        print "\nSending ",l, "Transponder address 2200 absent at LR216"
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)

        self.waitMsec(longDelay)
        print "\n"

        l = jmri.jmrix.loconet.LocoNetMessage([0xD0, 0x21, 0x59, 0x11, 0x18, 0x68])
        print "\nSending ",l, "Transponder address 2200 present at LR218"
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)
        self.waitMsec(shortDelay)
        l = jmri.jmrix.loconet.LocoNetMessage([0xD0, 0x07, 0x69, 0x11, 0x18, 0x68])
        print "\nSending ",l, "Transponder address 2200 absent at LR1002 "
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(l)

RunTest().start()
