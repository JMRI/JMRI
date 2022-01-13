print ("Python3Test")

import jmri as jmri
import java

# access constants

if (jmri.Turnout.THROWN != 4) : raise AssertionError('Constant THROWN not right')

# access InstanceManager to get manager
smc = java.type('jmri.SensorManager')
sm = jmri.InstanceManager.getNullableDefault(smc)

if (sm == None) : raise AssertionError('No instance manager access')

# use that manager to affect JMRI
IS1 = sm.provideSensor("IS1")

if (sm.getSensor("IS1") == None) : raise AssertionError('Sensor not created')

# load the shortcuts
exec( open("jython/jmri_bindings.py3").read() )

# check against simpler syntax
if (sm != sensors) : raise AssertionError('Not same SensorManager')

# check for a listener being OK
class MyListener(java.beans.PropertyChangeListener):
  localResult = False

  def propertyChange(self, event):
    print ("Listener fired - OK")
    #if (event.getOldValue() != UNKNOWN || event.getNewValue() != 4) : raise AssertionError('Listener found wrong values')
    global listenerCheck
    listenerCheck = True
    # local variables seem to be available
    if (self.localResult) : raise AssertionError('localResult not set False')
    self.localResult = True
    if (not self.localResult) : raise AssertionError('localResult not set True')

listenerCheck = False
m = MyListener()
IS1.addPropertyChangeListener(m)
IS1.setKnownState(INACTIVE)
# call back should have been immediate
if (not listenerCheck) : raise AssertionError('listenerCheck not set True')
# local variables can't be accessed
#! print ("local localResult:", m.localResult)


# extending a class
from jmri.jmrit.automat import AbstractAutomaton

class Automat(AbstractAutomaton) :
    def setup(self, value) :
        self.value = value
    def init(self) :
        print ("init in Python 3")
        self.setup(3)
        if (self.value != 3) : raise AssertionError('Local value != 3')
    def handle(self) :
        print ("handle in Python 3")
        #! this wait compiles, but doesn't happen; execution seems to end here
        self.__super__.waitMsec(100000)
        print ("after delay in Python 3")
        return False

a = Automat()
a.start()


