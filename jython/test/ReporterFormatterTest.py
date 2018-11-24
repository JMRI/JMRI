# Test the ReporterFormatter.py script
import java
import java.beans

# First, check syntax
execfile("jython/ReporterFormatter.py")

# Create one of these
m = ReporterFormatter()

# Operating the script requires a configured ReporterManager and MemoryManager ito continue
if (reporters != None and memories != None ) : 

    # start script and check it creates sample objects
    m.start("IR146", "IM146")
    if (memories.getMemory('IM146') == None) : raise AssertionError('IM146 not created')
    if (reporters.getReporter('IR146') == None) : raise AssertionError('IR146 not created')

    # check formatting examples without a connection by directly triggeting a property change event
    import java.beans
    m.propertyChange(java.beans.PropertyChangeEvent(reporters.getReporter('IR146'), "currentReport", None, "3 enter"))
    if (not memories.getMemory('IM146').value == '3 ') : raise AssertionError('IM146 value incorrect: \"'+memories.getMemory('IM146').value+"\"")

    m.propertyChange(java.beans.PropertyChangeEvent(reporters.getReporter('IR146'), "currentReport", None, "257 enter"))
    if (not memories.getMemory('IM146').value == '3 257 ') : raise AssertionError('IM146 value incorrect: \"'+memories.getMemory('IM146').value+"\"")

    m.propertyChange(java.beans.PropertyChangeEvent(reporters.getReporter('IR146'), "currentReport", None, "3 exits"))
    if (not memories.getMemory('IM146').value == '257 ') : raise AssertionError('IM146 value incorrect: \"'+memories.getMemory('IM146').value+"\"")

    m.propertyChange(java.beans.PropertyChangeEvent(reporters.getReporter('IR146'), "currentReport", None, "257 exits"))
    if (not memories.getMemory('IM146').value == '') : raise AssertionError('IM146 value incorrect: \"'+memories.getMemory('IM146').value+"\"")

    # success!
    m.stop()

# Define some test routines. 
# These don't do anything until invoked by hand to test an instantiation of this script
# 
# Example msg content: D0 20 0B 7D 03 FF - lower byte 1 and byte 2 are reporter 12, 
# 3,4 are loco address (3=7D short)
# 20 vs 00 in 2nd byte shows enter/exit
# 
# To use these, open a script input window (with a LocoNet connection) and enter the following line by line while watching the Memory table:
#   execfile("jython/test/ReporterFormatterTest.py")
#   m = ReporterFormatter()
#   m.start("LR146", "IM146")
#   test3enter()
#   test257enter()
#   m.stop()
# 

def test3enter() :
    # [D0 21 11 7D 03 61]  Transponder address 3 (short) (or long address 16003) present at LR146 () (BDL16x Board ID 10 RX4 zone A).
    # 3 enter
    
    packet = jmri.jmrix.loconet.LocoNetMessage(6)
    packet.setElement(0, 0xD0)
    packet.setElement(1, 0x21)
    packet.setElement(2, 0x11)
    packet.setElement(3, 0x7D)
    packet.setElement(4, 0x03)
    connectionIndex = 0
    jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(packet)
    return
    
def test257enter() :
    # [D0 21 11 02 01 1C]  Transponder address 257 present at LR146 () (BDL16x Board ID 10 RX4 zone A).
    # 257 enter
    
    packet = jmri.jmrix.loconet.LocoNetMessage(6)
    packet.setElement(0, 0xD0)
    packet.setElement(1, 0x21)
    packet.setElement(2, 0x11)
    packet.setElement(3, 0x02)
    packet.setElement(4, 0x01)
    connectionIndex = 0
    jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(packet)
    return
    
def test257exit() :
    # [D0 01 11 02 01 3C]  Transponder address 257 absent at LR146 () (BDL16x Board ID 10 RX4 zone A).
    # 257 exits
    
    packet = jmri.jmrix.loconet.LocoNetMessage(6)
    packet.setElement(0, 0xD0)
    packet.setElement(1, 0x01)
    packet.setElement(2, 0x11)
    packet.setElement(3, 0x02)
    packet.setElement(4, 0x01)
    connectionIndex = 0
    jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(packet)
    return
    
def test3exit() :
    # [D0 01 11 7D 03 41]  Transponder address 3 (short) (or long address 16003) absent at LR146 () (BDL16x Board ID 10 RX4 zone A).
    # 3 exits
    
    packet = jmri.jmrix.loconet.LocoNetMessage(6)
    packet.setElement(0, 0xD0)
    packet.setElement(1, 0x01)
    packet.setElement(2, 0x11)
    packet.setElement(3, 0x7D)
    packet.setElement(4, 0x03)
    connectionIndex = 0
    jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex).getLnTrafficController().sendLocoNetMessage(packet)
    return

    