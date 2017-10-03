# Test the SensorGroupAutoItem.py script
import jmri

left = sensors.provideSensor("IS101")
left.setUserName("left")
right = sensors.provideSensor("IS102")
right.setUserName("right")

auto = sensors.provideSensor("IS10000")
auto.setUserName("center")
auto.setKnownState(INACTIVE)

# confirm test implementation
left.setKnownState(INCONSISTENT)
if (left.getKnownState() != INCONSISTENT) : raise AssertionError('left known state not changed')

# prep initial state for tests
auto.setKnownState(INACTIVE)
left.setKnownState(ACTIVE)
right.setKnownState(INACTIVE)

# start actual test

execfile("jython/SensorGroupAutoItem.py")

SensorGroupAutoItem().set("IS10000",["IS101", "IS102"])

auto.setKnownState(INACTIVE)
left.setKnownState(ACTIVE)
right.setKnownState(INACTIVE)
if (auto.getKnownState() != INACTIVE) : raise AssertionError('test 1 failed')

left.setKnownState(INACTIVE)
if (auto.getKnownState() != ACTIVE) : raise AssertionError('test 2 failed')

right.setKnownState(ACTIVE)
if (auto.getKnownState() != INACTIVE) : raise AssertionError('test 3 failed')
