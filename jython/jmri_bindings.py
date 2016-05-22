# jmri_bindings.py
#
# Default objects for interaction with JMRI from a python script.
# This file may be needed only if jython.exec=true in python.properties

# Default imports
import java
import jmri

# JMRI default managers
turnouts     = jmri.InstanceManager.turnoutManagerInstance()
sensors      = jmri.InstanceManager.sensorManagerInstance()
signals      = jmri.InstanceManager.signalHeadManagerInstance()
masts        = jmri.InstanceManager.signalMastManagerInstance()
lights       = jmri.InstanceManager.lightManagerInstance()
dcc          = jmri.InstanceManager.commandStationInstance()
reporters    = jmri.InstanceManager.reporterManagerInstance()
memories     = jmri.InstanceManager.memoryManagerInstance()
routes       = jmri.InstanceManager.routeManagerInstance()
blocks       = jmri.InstanceManager.blockManagerInstance()
powermanager = jmri.InstanceManager.powerManagerInstance()
programmers  = jmri.InstanceManager.programmerManagerInstance()
shutdown     = jmri.InstanceManager.shutDownManagerInstance()
audio        = jmri.InstanceManager.audioManagerInstance()
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
