# Test the CombineTurnouts.py script
import jmri

# create special turnouts that can have their KnownState directly changed
class TestTurnout(jmri.implementation.AbstractTurnout) :
    def forwardCommandChangeToLayout(self, s) :
        return
    def turnoutPushbuttonLockout(self, b) : 
        return 
    def setTestKnownState(self, state) :
        self.newKnownState(state)

follower1 = TestTurnout("IT101")
follower1.setUserName("follower1")
jmri.InstanceManager.getDefault(jmri.TurnoutManager).register(follower1)
follower2 = TestTurnout("IT102")
follower2.setUserName("follower2")
jmri.InstanceManager.getDefault(jmri.TurnoutManager).register(follower2)

master = turnouts.provideTurnout("IT10000")
master.setUserName("master")
master.setCommandedState(THROWN)

# confirm test implementation
follower1.setTestKnownState(INCONSISTENT)
if (follower1.getKnownState() != INCONSISTENT) : raise AssertionError('follower1 known state not changed')

# prep initial state for tests
follower1.setCommandedState(CLOSED)
follower2.setCommandedState(CLOSED)

# start actual test

execfile("jython/CombineTurnouts.py")

CombineTurnouts().set("IT10000",["IT101", "IT102"])

if (follower1.getCommandedState() != THROWN) : raise AssertionError('Initial state not set into follower1')
if (follower2.getCommandedState() != THROWN) : raise AssertionError('Initial state not set into follower2')

master.setCommandedState(CLOSED)
if (follower1.getCommandedState() != CLOSED) : raise AssertionError('follower1 didnt follow CLOSED')
if (follower2.getCommandedState() != CLOSED) : raise AssertionError('follower2 didnt follow CLOSED')

master.setCommandedState(THROWN)
if (follower1.getCommandedState() != THROWN) : raise AssertionError('follower1 didnt follow THROWN')
if (follower2.getCommandedState() != THROWN) : raise AssertionError('follower2 didnt follow THROWN')

master.setCommandedState(CLOSED)
if (follower1.getCommandedState() != CLOSED) : raise AssertionError('follower1 didnt follow 2nd CLOSED')
if (follower2.getCommandedState() != CLOSED) : raise AssertionError('follower2 didnt follow 2nd CLOSED')

follower1.setTestKnownState(INCONSISTENT)
if (master.getKnownState() != INCONSISTENT) : raise AssertionError('master didnt follow follower1 INCONSISTENT')

follower1.setTestKnownState(CLOSED)
if (master.getKnownState() != CLOSED) : raise AssertionError('master didnt follow follower1 CLOSED after INCONSISTENT')

follower1.setTestKnownState(THROWN)
follower2.setTestKnownState(THROWN)
if (master.getKnownState() != THROWN) : raise AssertionError('master didnt follow THROWN, THROWN')

follower2.setTestKnownState(UNKNOWN)
if (master.getKnownState() != UNKNOWN) : raise AssertionError('master didnt follow follower2 UNKNOWN')

follower1.setTestKnownState(CLOSED)
follower2.setTestKnownState(CLOSED)
if (master.getKnownState() != CLOSED) : raise AssertionError('master didnt follow CLOSED, CLOSED')

follower1.setTestKnownState(CLOSED)
follower2.setTestKnownState(THROWN)
if (master.getKnownState() != INCONSISTENT) : raise AssertionError('master didnt follow CLOSED, THROWN')
