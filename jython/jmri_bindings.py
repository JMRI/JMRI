# jmri_bindings.py
#
# Default objects for interaction with JMRI from a python script.
# This file may be needed only if jython.exec=true in python.properties
#
# These should be kept consistent with those in jmri.script.JmriScriptEngineManager
# 

# Default imports
import java
import jmri

# JMRI default managers
turnouts     = jmri.InstanceManager.getDefault(jmri.TurnoutManager)
sensors      = jmri.InstanceManager.getDefault(jmri.SensorManager)
signals      = jmri.InstanceManager.getDefault(jmri.SignalHeadManager)
masts        = jmri.InstanceManager.getDefault(jmri.SignalMastManager)
lights       = jmri.InstanceManager.getDefault(jmri.LightManager)
dcc          = jmri.InstanceManager.getDefault(jmri.CommandStation)
reporters    = jmri.InstanceManager.getDefault(jmri.ReporterManager)
memories     = jmri.InstanceManager.getDefault(jmri.MemoryManager)
routes       = jmri.InstanceManager.getDefault(jmri.RouteManager)
blocks       = jmri.InstanceManager.getDefault(jmri.BlockManager)
powermanager = jmri.InstanceManager.getDefault(jmri.PowerManager)
addressedProgrammers = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager)
globalProgrammers = jmri.InstanceManager.getDefault(jmri.GlobalProgrammerManager)
shutdown     = jmri.InstanceManager.getDefault(jmri.ShutDownManager)
audio        = jmri.InstanceManager.getDefault(jmri.AudioManager)
layoutblocks = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
warrants     = jmri.InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager)

# common JMRI constants
import jmri.Turnout.CLOSED         as CLOSED
import jmri.Turnout.THROWN         as THROWN
import jmri.Turnout.CABLOCKOUT         as CABLOCKOUT
import jmri.Turnout.PUSHBUTTONLOCKOUT  as PUSHBUTTONLOCKOUT
import jmri.Turnout.UNLOCKED       as UNLOCKED
import jmri.Turnout.LOCKED         as LOCKED

import jmri.Sensor.ACTIVE          as ACTIVE
import jmri.Sensor.INACTIVE        as INACTIVE

import jmri.Light.ON               as ON
import jmri.Light.OFF              as OFF

import jmri.NamedBean.UNKNOWN      as UNKNOWN
import jmri.NamedBean.INCONSISTENT as INCONSISTENT

import jmri.SignalHead.DARK        as DARK
import jmri.SignalHead.RED         as RED
import jmri.SignalHead.YELLOW      as YELLOW
import jmri.SignalHead.GREEN       as GREEN
import jmri.SignalHead.LUNAR       as LUNAR
import jmri.SignalHead.FLASHRED    as FLASHRED
import jmri.SignalHead.FLASHYELLOW as FLASHYELLOW
import jmri.SignalHead.FLASHGREEN  as FLASHGREEN
import jmri.SignalHead.FLASHLUNAR  as FLASHLUNAR
