# basic setup for JavaOne demo

import java
import java.beans
import java.io

# define constants

directoryname = "javaone"+java.io.File.separator
fileprefix = jmri.util.FileUtil.getUserFilesPath()+directoryname

addressA = 4802
longA = True
nameA = "black SP 4802"

addressB = 164
longB = True
nameB = "red Digitrax 164"

slow = 0.3
fast = 0.6

initSensorA = "LS160"
initSensorB = "LS159"
# initBlockA, initBlockB are defined below

# Load the layout configuration
jmri.InstanceManager.getDefault(jmri.ConfigureManager).load(java.io.File(fileprefix+"Configuration.xml"))

# define the utility for doing warnings
execfile(fileprefix+"warn.py")

# set initial layout state during layout-less debugging
execfile(fileprefix+"clear.py")

# capture throttle objects
throttleA = None
throttleB = None
execfile(fileprefix+"throttles.py") # this may take a while

# load block configuration (not yet persisted in XML config file)
execfile(fileprefix+"blocksetup.py")
initBlockA = IB160
initBlockB = IB159

# configure stop blocks (not yet persisted in XML config file)
execfile(fileprefix+"signalstop.py")

# configure memory tracker (not yet persisted in XML config file)
execfile(fileprefix+"memorytracker.py")

# power on to start setting sensors
jmri.InstanceManager.getDefault(jmri.PowerManager).setPower(jmri.PowerManager.ON)

# open scripting windows & position
a = jmri.jmrit.jython.InputWindowAction("")
a.actionPerformed(None)
f = a.getFrame();
f.setLocation(0,900)
f.setSize(500, 200)

a = jmri.jmrit.jython.JythonWindow("")
a.actionPerformed(None)
f = a.getFrame();
f.setLocation(500,900)
f.setSize(800, 200)

# open the speedometer 
execfile(fileprefix+"speedo.py")

# open throttle windows
jmri.jmrit.throttle.LoadXmlThrottleAction().loadThrottles(java.io.File(fileprefix+"Throttles.xml"))

# show the panels
jmri.InstanceManager.getDefault(jmri.ConfigureManager).load(java.io.File(fileprefix+"MainPanel.xml"))
jmri.InstanceManager.getDefault(jmri.ConfigureManager).load(java.io.File(fileprefix+"MonitorPanel.xml"))

# now enough configured to be able to force stop of layout
skipSetup = False
# if throttleA == None or throttleB == None : 
#    warn().display("throttles did not acquire, reset LocoBuffer and restart program")
#    skipSetup = True
execfile(fileprefix+"startstop.py")
stop()

# put some automation in effect
execfile(fileprefix+"doublestoprule.py")
execfile(fileprefix+"pulloverrule.py")
execfile(fileprefix+"passingrule.py")

# get the start/stop sensor and act on it
startstopsensor = sensors.getSensor("Start/Stop")

class StartSensorListener(java.beans.PropertyChangeListener):
  def propertyChange(self, event):
    if startstopsensor.state==ACTIVE : start()
    else : stop()
    return
startstopsensor.addPropertyChangeListener(StartSensorListener())

# run at start without waiting if switch already set
if startstopsensor.state == ACTIVE and not skipSetup : start()
