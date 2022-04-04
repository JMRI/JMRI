import jmri as jmri
import java

sensors     = jmri.InstanceManager.getNullableDefault(java.type('jmri.SensorManager'))
turnouts    = jmri.InstanceManager.getNullableDefault(java.type('jmri.TurnoutManager'))
lights      = jmri.InstanceManager.getNullableDefault(java.type('jmri.LightManager'))
signals     = jmri.InstanceManager.getNullableDefault(java.type('jmri.SignalHeadManager'))
masts       = jmri.InstanceManager.getNullableDefault(java.type('jmri.SignalMastManager'))
routes      = jmri.InstanceManager.getNullableDefault(java.type('jmri.RouteManager'))
blocks      = jmri.InstanceManager.getNullableDefault(java.type('jmri.BlockManager'))
reporters   = jmri.InstanceManager.getNullableDefault(java.type('jmri.ReporterManager'))
memories    = jmri.InstanceManager.getNullableDefault(java.type('jmri.MemoryManager'))
powermanager    = jmri.InstanceManager.getNullableDefault(java.type('jmri.PowerManager'))
addressedProgrammers    = jmri.InstanceManager.getNullableDefault(java.type('jmri.AddressedProgrammerManager'))
globalProgrammers       = jmri.InstanceManager.getNullableDefault(java.type('jmri.GlobalProgrammerManager'))
dcc         = jmri.InstanceManager.getNullableDefault(java.type('jmri.CommandStation'))
audio       = jmri.InstanceManager.getNullableDefault(java.type('jmri.AudioManager'))
shutdown    = jmri.InstanceManager.getNullableDefault(java.type('jmri.ShutDownManager'))
layoutblocks    = jmri.InstanceManager.getNullableDefault(java.type('jmri.jmrit.display.layoutEditor.LayoutBlockManager'))
warrants    = jmri.InstanceManager.getNullableDefault(java.type('jmri.jmrit.logix.WarrantManager'))

THROWN = jmri.Turnout.THROWN


CLOSED  = jmri.Turnout.CLOSED
THROWN  = jmri.Turnout.THROWN
CABLOCKOUT = jmri.Turnout.CABLOCKOUT
PUSHBUTTONLOCKOUT = jmri.Turnout.PUSHBUTTONLOCKOUT
UNLOCKED = jmri.Turnout.UNLOCKED
LOCKED  = jmri.Turnout.LOCKED
ACTIVE  = jmri.Sensor.ACTIVE
INACTIVE = jmri.Sensor.INACTIVE
ON      = jmri.DigitalIO.ON
OFF     = jmri.DigitalIO.OFF
UNKNOWN = jmri.NamedBean.UNKNOWN
INCONSISTENT = jmri.NamedBean.INCONSISTENT
DARK    = jmri.SignalHead.DARK
RED     = jmri.SignalHead.RED
YELLOW  = jmri.SignalHead.YELLOW
GREEN   = jmri.SignalHead.GREEN
LUNAR   = jmri.SignalHead.LUNAR
FLASHRED = jmri.SignalHead.FLASHRED
FLASHYELLOW = jmri.SignalHead.FLASHYELLOW
FLASHGREEN = jmri.SignalHead.FLASHGREEN
FLASHLUNAR = jmri.SignalHead.FLASHLUNAR
FileUtil = jmri.util.FileUtilSupport.getDefault()
