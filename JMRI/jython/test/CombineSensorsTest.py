# Test the CombineSensors.py script
import jmri

follower1 = sensors.provideSensor("IS101")
follower1.setUserName("follower1")
follower2 = sensors.provideSensor("IS102")
follower2.setUserName("follower2")

master = sensors.provideSensor("IS10000")
master.setUserName("master")
master.setKnownState(INACTIVE)

# confirm test implementation
follower1.setKnownState(INCONSISTENT)
if (follower1.getKnownState() != INCONSISTENT) : raise AssertionError('follower1 known state not changed')

# prep initial state for tests
follower1.setKnownState(ACTIVE)
follower2.setKnownState(ACTIVE)

# start actual test

execfile("jython/CombineSensors.py")

CombineSensors().set("IS10000",["IS101", "IS102"])

if (master.getKnownState() != ACTIVE) : raise AssertionError('Initial state not set into master')

follower1.setKnownState(INCONSISTENT)
if (master.getKnownState() != INCONSISTENT) : raise AssertionError('master didnt follow follower1 INCONSISTENT')

follower1.setKnownState(ACTIVE)
if (master.getKnownState() != ACTIVE) : raise AssertionError('master didnt follow follower1 ACTIVE after INCONSISTENT')

follower1.setKnownState(INACTIVE)
follower2.setKnownState(INACTIVE)
if (master.getKnownState() != INACTIVE) : raise AssertionError('master didnt follow INACTIVE, INACTIVE')

follower2.setKnownState(UNKNOWN)
if (master.getKnownState() != UNKNOWN) : raise AssertionError('master didnt follow follower2 UNKNOWN')

follower1.setKnownState(INACTIVE)
follower2.setKnownState(ACTIVE)
if (master.getKnownState() != ACTIVE) : raise AssertionError('master didnt follow INACTIVE, ACTIVE')

follower1.setKnownState(ACTIVE)
follower2.setKnownState(INACTIVE)
if (master.getKnownState() != ACTIVE) : raise AssertionError('master didnt follow ACTIVE, INACTIVE')
