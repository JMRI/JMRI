print ("Python3Test start")

import jmri as jmri
import java

# print some status
import sys
print ("Python:", sys.version)
print ("java:", java.lang.System.getProperty("java.version"))
print ("GraalVM", java.lang.System.getProperty("org.graalvm.version"))

# access JMRI class constants
if (jmri.Turnout.THROWN != 4) : raise AssertionError('Constant THROWN not right')

# access InstanceManager to get manager
smc = java.type('jmri.SensorManager')
sm = jmri.InstanceManager.getNullableDefault(smc)

if (sm == None) : raise AssertionError('No instance manager access')

# use that manager to affect JMRI
IS1 = sm.provideSensor("IS1")

# load the shortcuts
exec( open("jython/jmri_bindings.py3").read() )

# check NamedBean (Sensor) state manipulation
if (sm.getSensor("IS1") == None) : raise AssertionError('Sensor not created')
IS1.setKnownState(ACTIVE)
if (sm.getSensor("IS1").getKnownState() != ACTIVE) : raise AssertionError('Sensor state not ACTIVE')
IS1.setKnownState(INACTIVE)
if (sm.getSensor("IS1").getKnownState() != INACTIVE) : raise AssertionError('Sensor state not INACTIVE')

# check against direct syntax
if (sm != sensors) : raise AssertionError('Not same SensorManager')

# extending a JMRI class
from jmri.jmrit.automat import AbstractAutomaton

class Automat(AbstractAutomaton) :
    def setup(self, value) :
        self.value = value
    def init(self) :
        print ("init in Python3Test")
        self.setup(3)
        if (self.value != 3) : raise AssertionError('Local value != 3')
    def handle(self) :
        print ("handle in Python3Test; you should see after-delay message later")
        print ("handle running on", java.lang.Thread.currentThread())
        self.__super__.waitMsec(100)  # note syntax
        print ("after delay in Python3Test")
        return False

a = Automat()
print ("core running on", java.lang.Thread.currentThread())
a.start()

# check for a listener being OK
IS1.setKnownState(UNKNOWN)
#! Rerunning the next doesn't seem to update the class
class MyListener(java.beans.PropertyChangeListener):
  localResult = False

  def propertyChange(self, event):
    print ("Listener fired - OK")
    if (event.getOldValue() != UNKNOWN or event.getNewValue() != INACTIVE) : raise AssertionError('Listener found wrong values')
    global listenerCheck
    listenerCheck = True
    # local variables seem to be available
    if (self.localResult) : raise AssertionError('localResult not set False')
    self.localResult = True
    if (not self.localResult) : raise AssertionError('localResult not set True')
    self.localResult = False

listenerCheck = False
m = MyListener()
IS1.addPropertyChangeListener(m)
print ("listener definition complete")

IS1.setKnownState(INACTIVE)
# call back should have been immediate
if (not listenerCheck) : raise AssertionError('listenerCheck not set True')

# local variables require .this. syntax
print ("local localResult:", m.this.localResult)

# allow test to be run more than once
IS1.removePropertyChangeListener(m)

# check handling Python Exception
try:
    x = 1/0
    print ("Expected Python exception didn't happen")
except Exception as e:
    print("Python exception happened as expected:", e)

# check handling Java Exception
v = java.util.Vector()
try:
    x = v.elementAt(7)
    print ("Expected Java exception didn't happen")
except java.lang.ArrayIndexOutOfBoundsException as e:
    # We don't seem to be able to access e here
    print("Java exception happened as expected")

print ("Python3Test main execution complete")


